package com.radyfy.common.service.crm;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.crm.model.CrmModelType;
import com.radyfy.common.model.crm.model.DataType;
import com.radyfy.common.model.crm.model.ModelProperty;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.model.CrmModelConfig;
import com.radyfy.common.service.crm.config.model.CrmModelConsumerProps;
import com.radyfy.common.utils.Utils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CrmModelService {

    private static final Logger logger = LoggerFactory.getLogger(CrmModelService.class);

    private final MetaOrmDao metaOrmDao;
    private final CrmModelConfig crmModelConfig;
    private final CurrentUserSession currentUserSession;

    public CrmModelService(MetaOrmDao metaOrmDao, ConfigBuilder configBuilder,
            CurrentUserSession currentUserSession) {
        this.metaOrmDao = metaOrmDao;
        this.crmModelConfig = configBuilder.getModelConfig();
        this.currentUserSession = currentUserSession;
    }

    public List<BaseCrmModel> getBaseModels() {

        return metaOrmDao.getBaseModels();
    }
    public List<BaseCrmModel> getBaseModels(String accountId) {

        return metaOrmDao.getBaseModels(accountId);
    }

    public void forEachBaseModels(BaseModalConsumer consumer, CrmModel model) {

        metaOrmDao.forEachBaseModels(consumer, model, false);
    }

    public void forEachBaseModels(BaseModalConsumer consumer, CrmModel model,
            boolean includeCurrentModel) {

        metaOrmDao.forEachBaseModels(consumer, model, includeCurrentModel);
    }

    public CrmModel getModel(String modelId, boolean isCommonApi) {
        CrmModel model = null;
        if (isCommonApi) {
            model = metaOrmDao.getById(modelId, CrmModel.class,
                    DaoQuery.keyValue(Constants.ACCOUNT_ID, Constants.RADYFY_ACCOUNT_ID));
        } else {
            model = metaOrmDao.getById(modelId, CrmModel.class, null);
        }
        appendScopeProperties(model);
        return model;
    }

    public CrmModel getBaseModel(String modelId) {
        return metaOrmDao.getById(modelId, CrmModel.class,
                DaoQuery.keyValue("modelType", CrmModelType.BASE));
    }

    public CrmModel getModelByCollectionName(String collectionName) {

        CrmModel model = null;
        if (CollectionNames.radyfyCommonCollections.contains(collectionName)) {
            model = metaOrmDao.findOneByQuery(
                    DaoQuery.builder()
                            .criteriaList(Arrays.asList(
                                    Criteria.where("collectionName").is(collectionName),
                                    Criteria.where(Constants.ACCOUNT_ID)
                                            .is(Constants.RADYFY_ACCOUNT_ID)))
                            .build(),
                    CrmModel.class);
        } else {
            model = metaOrmDao.findOneByQuery(
                    DaoQuery.builder()
                            .criteriaList(Collections.singletonList(
                                    Criteria.where("collectionName").is(collectionName)))
                            .build(),
                    CrmModel.class);
        }
        appendScopeProperties(model);
        return model;
    }

    public boolean isValidHardAccountValue(String value) {
        if (currentUserSession.getAccount().getId().equals(Constants.RADYFY_ACCOUNT_ID)) {
            return Constants.ACCOUNT.equals(value) || ("User".equals(value));
        }
        return false;
    }

    public void runEventListener(CrmModelConfig.Event event, CrmModel crmModel,
            GridRequestParams params) {
        runEventListener(event, crmModel, params, null);
    }

    public void runEventListener(CrmModelConfig.Event event, CrmModel crmModel,
            GridRequestParams params, Document entity) {
        crmModelConfig.runConsumer(event, new CrmModelConsumerProps(crmModel, params, entity));
    }

    private void appendScopeProperties(CrmModel model) {
        if (model != null) {
            Boolean isOrg = model.getIsOrg();
            Boolean isOrgScopeApplicable = model.getIsOrgScopeApplicable();
            Boolean isUserAccount = model.getIsUserAccount();
            if (Utils.isTrue(isOrg)) {

                if (Constants.ACCOUNT.equals(model.getParent())) {
                    ModelProperty property = new ModelProperty();
                    property.setKey("account");
                    property.setName("Account");
                    property.setIsOrg(true);
                    model.getProperties().add(property);
                } else {
                    forEachBaseModels(baseModel -> {
                        String fieldName = baseModel.getFieldName();
                        ModelProperty property = new ModelProperty();
                        property.setKey(fieldName);
                        property.setName(fieldName);
                        property.setDataType(DataType.REFERENCE);
                        property.setModelId(baseModel.getModelId());
                        property.setIsOrg(true);
                        model.getProperties().add(property);
                    }, model);
                }


            } else if (Utils.isTrue(isUserAccount)) {

                appendOrgScopeProperties(model);
                appendAccessProperties(model);

            }

            else if (Utils.isTrue(isOrgScopeApplicable)) {

                appendOrgScopeProperties(model);
            }
        }
    }

    private void appendOrgScopeProperties(CrmModel model) {

        appendProperties(model, "");
    }

    private void appendAccessProperties(CrmModel model) {

        appendProperties(model, "_access");

        if (Utils.isTrue(model.getIsUserAccount())) {
            model.getProperties().forEach(m -> {
                if (m.getKey().equals("email")) {
                    m.setUnique(true);
                }
            });
        }
    }


    private void appendProperties(CrmModel model, String scopeType) {


        List<BaseCrmModel> baseModels = getBaseModels();

        if (Utils.isNotEmpty(baseModels)) {
            for (BaseCrmModel baseModel : baseModels) {
                String fieldName = baseModel.getFieldName();
                ModelProperty property = new ModelProperty();
                property.setKey(scopeType + fieldName);
                property.setName(baseModel.getName());
                property.setDataType(DataType.LIST_OF);
                property.setListType(DataType.REFERENCE);
                property.setModelId(baseModel.getModelId());
                property.setIsOrg(true);
                model.getProperties().add(property);
            }
        }
    }

}

