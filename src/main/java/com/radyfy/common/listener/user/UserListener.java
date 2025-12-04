package com.radyfy.common.listener.user;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.radyfy.common.auth.PasswordHash;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.CrmForms;
import com.radyfy.common.commons.CrmTables;
import com.radyfy.common.commons.Errors;
import com.radyfy.common.commons.Regex;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.CrmFormConfig;
import com.radyfy.common.service.user.UserEmailService;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.crm.CrmModelService;

@Component
public class UserListener {

    private final PasswordHash passwordHash;
    private final EntityOrmDao entityOrmDao;
    private final CrmModelService crmModelService;
    private final UserEmailService userEmailService;
    private final CurrentUserSession currentUserSession;

    public UserListener(PasswordHash passwordHash, EntityOrmDao entityOrmDao,
            CrmModelService crmModelService, UserEmailService userEmailService,
            CurrentUserSession currentUserSession) {
        this.passwordHash = passwordHash;
        this.entityOrmDao = entityOrmDao;
        this.crmModelService = crmModelService;
        this.userEmailService = userEmailService;
        this.currentUserSession = currentUserSession;
    }

    @Autowired
    public void beforeCreateUser(ConfigBuilder configBuilder) {
        configBuilder.getFormConfig().addEventListener(CrmFormConfig.Event.BEFORE_CREATE,
                CrmForms.RADYFY_USER_FORM, props -> {
                    String randomPassword = generatePassword(10);
                    props.getFinalDoc().put("ecomAccountId",
                            currentUserSession.getEcomAccount().getId());
                    props.getFinalDoc().put("originalPassword", randomPassword);
                    props.getFinalDoc().put("password", passwordHash.hashPassword(randomPassword));
                    props.getFinalDoc().put("isFirstLogin", true);
                });
    }

    @Autowired
    public void afterUserCreate(ConfigBuilder configBuilder) {
        configBuilder.getFormConfig().addEventListener(CrmFormConfig.Event.AFTER_CREATE,
                CrmForms.RADYFY_USER_FORM, props -> {
                    Document user = props.getFinalDoc();
                    String name = user.getString("firstName") + " "
                            + (user.getString("lastName") != null ? user.getString("lastName")
                                    : "");
                    String originalPassword = user.getString("originalPassword");
                    userEmailService.sendUserWelcomeEmail(name, user.getString("email"),
                            originalPassword);

                    CrmModel userModel =
                            crmModelService.getModelByCollectionName(CollectionNames.user);
                    Update update = new Update();
                    update.unset("originalPassword");

                    DaoQuery daoQuery =
                            DaoQuery.fromCriteria(Criteria.where("_id").is(user.getString("_id")));
                    entityOrmDao.updateByQuery(daoQuery, update, userModel);

                });
    }

    @Autowired
    public void onUserTableLoad(ConfigBuilder configBuilder) {
        configBuilder.getTableConfig().onTableLoad(

                CrmTables.USER_TABLE, props -> {

                    CrmTable userTable = props.getCrmTable();
                    List<Document> userData = userTable.getData();

                    String accountId = currentUserSession.getAccount().getId();
                    String userRoleId = currentUserSession.getUser().getAppRoleId();

                    if (Constants.RADYFY_ACCOUNT_ID.equals(accountId)) {
                        for (Document user : userData) {
                            String roleId = user.getString("appRoleId");
                            boolean isEditable = false;

                            if (RoleType.SUPER_ADMIN.equals(userRoleId)) {

                                isEditable = true;
                            } else if (RoleType.SYSTEM_ADMIN.equals(userRoleId)) {

                                if (!RoleType.SUPER_ADMIN.equals(roleId)) {
                                    isEditable = true;
                                }
                            } else {
                                if (!RoleType.SUPER_ADMIN.equals(roleId)
                                        && !RoleType.SYSTEM_ADMIN.equals(roleId)) {
                                    isEditable = true;
                                }
                            }
                            user.append("isEditable", isEditable);
                        }
                        userTable.setData(userData);
                    } else {
                        for (Document user : userData) {
                            String roleId = user.getString("appRoleId");
                            boolean isEditable = false;

                            if (RoleType.ACCOUNT_ADMIN.equals(userRoleId)) {
                                isEditable = true;
                            } else {
                                if (!RoleType.ACCOUNT_ADMIN.equals(roleId)) {
                                    isEditable = true;
                                }
                            }
                            user.append("isEditable", isEditable);
                        }
                        userTable.setData(userData);
                    }
                });
    }

    @Autowired
    public void beforeUserUpdate(ConfigBuilder configBuilder) {
        configBuilder.getFormConfig().addEventListener(CrmFormConfig.Event.BEFORE_UPDATE,
                CrmForms.RADYFY_USER_FORM, props -> {

                    Document userDoc = props.getFinalDoc();
                    String roleId = userDoc.getString("appRoleId");

                    String accountId = currentUserSession.getAccount().getId();
                    boolean isRadyfyAccount = Constants.RADYFY_ACCOUNT_ID.equals(accountId);

                    String userRoleId = currentUserSession.getUser().getAppRoleId();

                    if (isRadyfyAccount) {

                        if (RoleType.SUPER_ADMIN.equals(roleId)) {
                            return;
                        } else if (RoleType.SYSTEM_ADMIN.equals(roleId)) {

                            if (!RoleType.SUPER_ADMIN.equals(roleId)) {
                                return;
                            }
                        } else {
                            if (!RoleType.SUPER_ADMIN.equals(roleId)
                                    && !RoleType.SYSTEM_ADMIN.equals(roleId)) {
                                return;
                            }
                        }
                    } else {
                        if (RoleType.ACCOUNT_ADMIN.equals(userRoleId)) {
                            return;
                        } else {
                            if (!RoleType.ACCOUNT_ADMIN.equals(roleId)) {
                                return;
                            }
                        }

                    }
                    throw new RuntimeException(Errors.NOT_ALLOWED_TO_UPDATE);
                });
    }


    private static String generatePassword(int length) {
        return java.util.stream.IntStream.range(0, length)
                .mapToObj(i -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@"
                        .charAt(new java.util.Random().nextInt(63)))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

}
