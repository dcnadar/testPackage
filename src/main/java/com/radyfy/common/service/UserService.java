package com.radyfy.common.service;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.radyfy.common.auth.PasswordHash;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.CrmForms;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.config.mongo.UserReadConverter;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.crm.api.CrmApi;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.dynamic.form.FormField;
import com.radyfy.common.model.enums.UserStatus;
import com.radyfy.common.model.user.User;
import com.radyfy.common.service.crm.CrmDynamicService;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.crm.grid.CrmFormService;
import com.radyfy.common.service.email.SendGridService;
import com.radyfy.common.service.user.RoleService;
import com.radyfy.common.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserService {

	@Value("${app.admin.fromEmail}")
	private String FROM_EMAIL;

	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;

	@Value("${app.sendgrid.user_welcome_template_id}")
	private String SENDGRID_USER_WELCOME_TEMPLATE_ID;
	private String radyfyUserForm = CrmForms.RADYFY_USER_FORM;
	private final MetaOrmDao metaOrmDao;
	private final EntityOrmDao entityOrmDao;
	private final CrmModelService crmModelService;
	private final CrmFormService crmFormService;
	private final CurrentUserSession currentUserSession;
	private final RoleService roleService;
	private final CrmDynamicService crmDynamicService;
	private final PasswordHash passwordHash;

	private final SendGridService sendGridService;

	public UserService(CrmModelService crmModelService, CrmFormService crmFormService,
			EntityOrmDao crmAccountOrmDao, CurrentUserSession currentUserSession,
			RoleService roleService, CrmDynamicService crmDynamicService, PasswordHash passwordHash,
			SendGridService sendGridService, MetaOrmDao metaOrmDao) {
		this.crmModelService = crmModelService;
		this.crmFormService = crmFormService;
		this.entityOrmDao = crmAccountOrmDao;
		this.currentUserSession = currentUserSession;
		this.roleService = roleService;
		this.crmDynamicService = crmDynamicService;
		this.passwordHash = passwordHash;
		this.sendGridService = sendGridService;
		this.metaOrmDao = metaOrmDao;
	}

	public Optional<User> getUserByUserName(String userName) {
		CrmModel crmModel = crmModelService.getModelByCollectionName(CollectionNames.user);
		Document user = entityOrmDao.findOneByQuery(
				DaoQuery.fromCriteria(Criteria.where("userName").regex(userName, "i")), crmModel);

		if (user == null) {
			return Optional.empty();
		}
		return Optional.of(new UserReadConverter().convert(user));
	}

	private void updateById(String id, Update update) {
		CrmModel crmModel = crmModelService.getModelByCollectionName(CollectionNames.user);
		entityOrmDao.updateByQuery(DaoQuery.fromId(id), update, crmModel);
	}

	public void setAdminFilters(User user, Map<String, String> feFilters) {
		// if (Utils.isTrue(user.getAdmin())) {

		CrmForm accountFilterForm = crmFormService.getAccountFilterForm();
		if (accountFilterForm == null || accountFilterForm.getRows() == null
				|| accountFilterForm.getRows().length == 0) {
			return;
		}

		if (feFilters == null) {
			feFilters = new HashMap<>();
		}
		boolean filterChanged = false;
		if (!Utils.isNotEmpty(user.getCrmLastFilter())) {
			filterChanged = true;
		}

		/**
		 * 
		 * Set default filters for admin
		 */
		List<BaseCrmModel> baseModels = crmModelService.getBaseModels();
		for (BaseCrmModel baseModel : baseModels) {

			// Check if the field exists in the account filter form
			if (doAccountFilterFieldExist(accountFilterForm, baseModel.getFieldName())) {
				String filterValue = feFilters.get(baseModel.getFieldName());

				// Check if the filter value is not empty
				if (Utils.isNotEmpty(filterValue)) {
					// Check if the filter value is not equal to the last filter value
					if (!filterValue.equals(user.getCrmLastFilter().get(baseModel.getFieldName()))) {
						// Check if filter is related to the current account model
						CrmModel crmModel = crmModelService.getModel(baseModel.getModelId(), false);
						Document data =
								entityOrmDao.getById(feFilters.get(baseModel.getFieldName()), crmModel, null);
						feFilters.put(baseModel.getFieldName(), data.getString("_id"));
						user.setCrmLastFilter(feFilters);
						currentUserSession.getUserSession().setFeFilters(feFilters);
						if (!Utils.isNotEmpty(data)) {
							throw new com.radyfy.common.exception.AuthException();
						}
						filterChanged = true;
					}
				} else {
					String baseModelId = baseModel.getModelId();
					CrmModel crmModel = crmModelService.getModel(baseModelId, false);
					Document data = entityOrmDao.findOneByQuery(null, crmModel);
					String value = data.getString("_id");
					feFilters.put(baseModel.getFieldName(), value);
					currentUserSession.getUserSession().setFeFilters(feFilters);
					filterChanged = true;
					user.setCrmLastFilter(feFilters);
				}
			}
		}
		if (filterChanged) {
			Update update = new Update();
			update.set("crmLastFilter", feFilters);
			updateById(user.getId(), update);
		}
		// }
	}

	public void activateUser(String userId) {
		Update update = new Update();
		update.set("status", UserStatus.ACTIVE.toString());
		updateById(userId, update);
	}

	private boolean doAccountFilterFieldExist(CrmForm accountFilterForm, String filedName) {
		for (FormField formField : accountFilterForm.getRows()[0].getFields()) {
			if (formField.getId().equals(filedName)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, String> validate_Sync_GetCrmFeFilters(String filter) {
		Map<String, String> feFilters = null;
		Map<String, Document> filterDocuments = null;
		if (Utils.isNotEmpty(filter)) {
			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, String>>() {}.getType();
			feFilters = gson.fromJson(filter, type);
			if (Utils.isNotEmpty(feFilters)) {
				filterDocuments = new HashMap<>();
				List<BaseCrmModel> baseModels = crmModelService.getBaseModels();
				CrmForm accountFilterForm = crmFormService.getAccountFilterForm();
				for (BaseCrmModel baseModel : baseModels) {
					// Check if the field exists in the account filter form
					if (doAccountFilterFieldExist(accountFilterForm, baseModel.getFieldName())) {
						if (feFilters.containsKey(baseModel.getFieldName())) {
							CrmModel crmModel = crmModelService.getModel(baseModel.getModelId(), false);
							try {
								Document data =
										entityOrmDao.getById(feFilters.get(baseModel.getFieldName()), crmModel, null);
								if (!Utils.isNotEmpty(data)) {
									feFilters.remove(baseModel.getFieldName());
								}
								filterDocuments.put(baseModel.getFieldName(), data);
							} catch (Exception e) {
								feFilters.remove(baseModel.getFieldName());
							}
							currentUserSession.getUserSession().setFeFilters(feFilters);
							currentUserSession.getUserSession().setFilterDocuments(filterDocuments);
						}
					}
				}
			}
		}
		return feFilters;
	}

	public boolean doCurrentUserHasAccess(CrmApi crmApi) {
		if (crmApi.getPath().startsWith("/admin/public/")) {
			return true;
		}
		User user = currentUserSession.getUserSession().getUser();
		// if (Utils.isTrue(user.getAdmin())) {
		// 	return true;
		// }
		String crmApiId = crmApi.getId();
		List<CrmApi> permittedApis = roleService.getPermittedApis(user);
		for (CrmApi permittedApi : permittedApis) {
			if (permittedApi.getId().equals(crmApiId)) {
				return true;
			}
		}
		return false;
	}

	public void createUser(Document user) {
		String ecomId = currentUserSession.getEcomAccount().getId();
		String userformId = radyfyUserForm;
		CrmForm userForm = crmFormService.getById(userformId, true);
		String newPassword = generatePassword(8);
		user.append("password", passwordHash.hashPassword(newPassword));
		user.putIfAbsent("userName", user.getString("email"));
		user.putIfAbsent("ecomAccountId", ecomId);
		user.putIfAbsent("isFirstLogin", true);
		user.putIfAbsent("appRoleId", RoleType.ACCOUNT_ADMIN);
		List<BaseCrmModel> baseModels = crmModelService.getBaseModels();
		for (BaseCrmModel baseModel : baseModels) {
			String fieldName = baseModel.getFieldName();
			user.put(fieldName, List.of("All"));
		}
		crmDynamicService.createDocumentForCrmForm(user, new GridRequestParams(), userForm, true);
	}

	public ResponseEntity<String> sendPasswordEmail(String email) {
		DaoQuery query = DaoQuery.fromCriteria(Criteria.where("email").is(email));
		CrmModel user = crmModelService.getModelByCollectionName(CollectionNames.user);
		Document userDocument = entityOrmDao.findOneByQuery(query, user);
		if (!"INVITED".equalsIgnoreCase(userDocument.getString("status"))) {
			log.info("User is not in INVITED status");
			return ResponseEntity.badRequest().body("User is not in INVITED status");
		}
		String newPassword = generatePassword(8);
		Update update = new Update();
		update.set("password", passwordHash.hashPassword(newPassword));
		DaoQuery query2 = DaoQuery.fromCriteria(Criteria.where("email").is(email));
		entityOrmDao.updateByQuery(query2, update, user);
		userDocument.put("password", newPassword);
		String accountId = userDocument.getString("accountId");
		mailSend(accountId, userDocument);
		log.info("Invitation processed and password sent");
		return ResponseEntity.ok("Invitation processed and password sent");
	}

	private void mailSend(String accountId, Document userDocument) {
		Document ecomAccount = getEcomAccountDoc(accountId);
		String name = userDocument.getString("firstName") + " "
				+ (userDocument.getString("lastName") != null ? userDocument.getString("lastName")
						: "");
		String toEmail = userDocument.getString("email");
		String environment = currentUserSession.getRequestSession().getEnvironment().toString();
		String password = userDocument.getString("password");
		String accountName = ecomAccount.getString("name");
		String logoUrl = ecomAccount.getString("logo");
		String domain = ecomAccount.getString("domain");
		String loginUrl = "https://" + environment.toLowerCase() + "." + domain + "/admin/login";
		Document dynamicData = new Document();
		dynamicData.put("name", name);
		dynamicData.put("email", toEmail);
		dynamicData.put("password", password);
		dynamicData.put("accountName", accountName);
		dynamicData.put("logoUrl", logoUrl);
		dynamicData.put("loginUrl", loginUrl);
		sendGridService.sendEmailAsync(FROM_EMAIL, toEmail, SENDGRID_USER_WELCOME_TEMPLATE_ID,
				dynamicData);
	}

	private Document getEcomAccountDoc(String accountId) {
		DaoQuery daoQuery = DaoQuery.fromCriteria(Criteria.where("accountId").is(accountId));
		EcomAccount ecomAccount = metaOrmDao.findOneByQuery(daoQuery, EcomAccount.class);
		Document ecomAccountDoc = new Document();
		List<String> domain = ecomAccount.getDomain();
		ecomAccountDoc.put("domain", domain.get(0));
		ecomAccountDoc.put("name", ecomAccount.getName());
		ecomAccountDoc.put("logo", ecomAccount.getLogo());
		return ecomAccountDoc;
	}

	private static String generatePassword(int length) {
		return java.util.stream.IntStream.range(0, length)
				.mapToObj(i -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@"
						.charAt(new java.util.Random().nextInt(63)))
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
				.toString();
	}

    public void deleteUser(String email) {
		DaoQuery deleteUserQuery = DaoQuery.fromCriteria(Criteria.where("email").is(email));
		CrmModel accountUserAccessModel = crmModelService.getModelByCollectionName(CollectionNames.user);
		entityOrmDao.delete(deleteUserQuery, accountUserAccessModel);
    }
}
