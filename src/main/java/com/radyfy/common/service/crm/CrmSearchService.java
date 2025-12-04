package com.radyfy.common.service.crm;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.stereotype.Component;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.CrmModels;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.crm.model.CrmModelType;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.search.CrmSearchConfig;
import com.radyfy.common.service.crm.config.search.CrmSearchConsumerProps;
import com.radyfy.common.service.crm.config.search.CrmSearchConfig.Event;
import com.radyfy.common.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class CrmSearchService {
    private static final Logger logger = LoggerFactory.getLogger(CrmSearchService.class);

    private final CrmDynamicService crmDynamicService;
    private final CrmSearchConfig crmSearchConfig;
    private final CrmModelService crmModelService;
    private final CurrentUserSession currentUserSession;

    @Autowired
    public CrmSearchService(CrmDynamicService crmDynamicService, ConfigBuilder configBuilder,
            CrmModelService crmModelService, CurrentUserSession currentUserSession) {
        this.crmDynamicService = crmDynamicService;
        this.crmSearchConfig = configBuilder.getSearchConfig();
        this.crmModelService = crmModelService;
        this.currentUserSession = currentUserSession;
    }

    public List<Option> searchModelData(HttpServletRequest httpServletRequest, String modelId) {

        // TODO add requiredParams in crm-api
        GridRequestParams params = Utils.getGridParams(httpServletRequest);
        String query = params.getOrDefault("query", "");
        params.remove("query");
        String absentValue = params.getOrDefault("absentValue", "");
        params.remove("absentValue");
        String modelUniqueKey = params.getOrDefault("modelUniqueKey", "");
        params.remove("modelUniqueKey");
        String searchAdditionalKeys = params.getOrDefault("searchAdditionalKeys", "");
        params.remove("searchAdditionalKeys");

        // remove params which have value 'All'
        params.entrySet().removeIf(entry -> "All".equals(entry.getValue()));

        List<String> additionalKeys = null;
        if (Utils.isNotEmpty(searchAdditionalKeys)) {
            additionalKeys = Arrays.asList(searchAdditionalKeys.split(","));
        }

        boolean isCommonModel = CrmModels.RADYDY_COMMON_MODELS.contains(modelId);

        CrmModel crmModel = crmModelService.getModel(modelId, isCommonModel);

        runEventListener(Event.BEFORE_SEARCH, crmModel, query, absentValue, modelUniqueKey,
                additionalKeys, null);

        List<Option> result = crmDynamicService.search(crmModel, query, params, modelUniqueKey,
                additionalKeys, isCommonModel);

        if (Utils.isNotEmpty(absentValue)) {
            result.add(0, new Option(absentValue, absentValue));
        }

        if (Constants.RADYFY_ACCOUNT_ID.equals(currentUserSession.getAccount().getId())) {

            if (CrmModels.RADYFY_ROLE_MODEL.equals(modelId)) {

                if (RoleType.SUPER_ADMIN.equals(currentUserSession.getUser().getAppRoleId())) {
                    result.add(0, new Option(RoleType.SUPER_ADMIN, "Super Admin"));
                    result.add(0, new Option(RoleType.SYSTEM_ADMIN, "System Admin"));
                } else if (RoleType.SYSTEM_ADMIN
                        .equals(currentUserSession.getUser().getAppRoleId())) {
                    result.add(0, new Option(RoleType.SYSTEM_ADMIN, "System Admin"));
                }
            }

            else if (CrmModels.RADYFY_ENTITY_MODEL.equals(modelId)
                    && CrmModelType.BASE.toString().equals(params.get("modelType"))) {

                result.add(0, new Option(Constants.ACCOUNT, "Account"));

            }

        }

        else {
            if (CrmModels.RADYFY_ROLE_MODEL.equals(modelId)) {
                if (RoleType.ACCOUNT_ADMIN.equals(currentUserSession.getUser().getAppRoleId())) {
                    result.add(0, new Option(RoleType.ACCOUNT_ADMIN, "Account Admin"));
                }
            }
        }

        if (modelId.equals(CrmModels.RADYFY_PAGE_MODEL)
                || modelId.equals(CrmModels.RADYFY_ENTITY_MODEL)) {

            if (currentUserSession.getUserSession() != null
                    && currentUserSession.getUserSession().getFilterDocuments() != null) {
                Document account = currentUserSession.getUserSession().getFilterDocuments()
                        .get(Constants.ACCOUNT_ID);
                if (account != null
                        && !account.getString("_id").equals(Constants.RADYFY_ACCOUNT_ID)) {
                    List<CriteriaDefinition> criteriaList = params.getGridFiltersCriteria();
                    criteriaList.add(Criteria.where("accountId").is(Constants.RADYFY_ACCOUNT_ID));
                    if (modelId.equals(CrmModels.RADYFY_PAGE_MODEL)) {
                        criteriaList.add(Criteria.where("crmModelId")
                                .in(CrmModels.RADYDY_COMMON_MODELS.toArray(new String[0])));
                    } else if (modelId.equals(CrmModels.RADYFY_ENTITY_MODEL)) {
                        criteriaList.add(Criteria.where("_id")
                                .in(CrmModels.RADYDY_COMMON_MODELS.stream()
                                        .map(id -> new ObjectId(id)).collect(Collectors.toList())
                                        .toArray(new ObjectId[0])));
                    }
                    List<Option> commonResult = crmDynamicService.search(crmModel, query,
                            criteriaList, false, modelUniqueKey, additionalKeys, isCommonModel);
                    if (Utils.isNotEmpty(commonResult)) {
                        result.addAll(commonResult);
                    }
                }
            }
        }

        runEventListener(Event.AFTER_SEARCH, crmModel, query, absentValue, modelUniqueKey,
                additionalKeys, result);

        return result;
    }

    public void runEventListener(CrmSearchConfig.Event event, CrmModel crmModel, String query,
            String absentValue, String modelUniqueKey, List<String> additionalKeys,
            List<Option> result) {
        crmSearchConfig.runConsumer(event, new CrmSearchConsumerProps(crmModel, query, absentValue,
                modelUniqueKey, additionalKeys, result));
    }
}
