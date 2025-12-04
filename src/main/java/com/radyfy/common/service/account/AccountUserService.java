package com.radyfy.common.service.account;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.stereotype.Service;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.CrmForms;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.service.crm.CrmDynamicService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.grid.CrmFormService;
import com.radyfy.common.utils.Utils;

@Service
public class AccountUserService {

  private final CrmDynamicService crmDynamicService;
  private final CrmModelService crmModelService;
  private final CrmFormService crmFormService;
  private final EntityOrmDao entityOrmDao;

  public AccountUserService(CrmDynamicService crmDynamicService, CrmModelService crmModelService,
      CrmFormService crmFormService, EntityOrmDao entityOrmDao) {
    this.crmDynamicService = crmDynamicService;
    this.crmModelService = crmModelService;
    this.crmFormService = crmFormService;
    this.entityOrmDao = entityOrmDao;
  }

  public List<Option> searchUserEmails(String email) {

    CrmModel userModel = crmModelService.getModelByCollectionName(CollectionNames.user);
    // Pass required parameters to search method
    List<CriteriaDefinition> criteriaList = new ArrayList<>();
    criteriaList.add(Criteria.where("email").is(email));
    List<Option> values =
        crmDynamicService.search(userModel, "", criteriaList, true);
    if (Utils.isNotEmpty(values)) {
      values.forEach(option -> {
        String value = (String) option.getValue();
        String description = option.getDescription();
        option.setDescription(value);
        option.setValue(description);
      });
    }
    return values;
  }

  public List<Option> userFormValues(String userId) {


    // Validate the user access scope
    List<BaseCrmModel> baseModels = crmModelService.getBaseModels();

    CrmModel userModel = crmModelService.getModelByCollectionName(CollectionNames.user);
    Document user = entityOrmDao.getById(userId, userModel, null);

    crmDynamicService.validateUserAccessScope(baseModels, user, "");

    // Get the visit form using the correct form ID
    CrmForm form = crmFormService.getById(CrmForms.RADYFY_USER_FORM, true);
    GridRequestParams params = new GridRequestParams();
    params.put("id", userId);
    List<Option> options = crmDynamicService.formValues(params, form, true);
    List<BaseCrmModel> baseCrmModels = crmModelService.getBaseModels();
    List<Option> result = new ArrayList<>();
    baseCrmModels.forEach(baseCrmModel -> {
      Option baseOption =
          options.stream().filter(option -> baseCrmModel.getFieldName().equals(option.getKey()))
              .findFirst().orElse(new Option(baseCrmModel.getFieldName(), List.of("All")));
      baseOption.setKey("_access" + baseOption.getKey());
      result.add(baseOption);
    });
    result.add(options.stream().filter(option -> "firstName".equals(option.getKey())).findFirst()
        .orElse(new Option("firstName", "")));
    result.add(options.stream().filter(option -> "lastName".equals(option.getKey())).findFirst()
        .orElse(new Option("lastName", "")));
    result.add(options.stream().filter(option -> "middleName".equals(option.getKey())).findFirst()
        .orElse(new Option("middleName", "")));
    return result;
  }

}
