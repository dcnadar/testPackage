package com.radyfy.common.service.crm.grid;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.Regex;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.form.FormField;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.dynamic.form.FormItem;
import com.radyfy.common.model.enums.grid.GridType;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.config.*;
import com.radyfy.common.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CrmFormService {

    private static final Logger logger = LoggerFactory.getLogger(CrmFormService.class);

    private final MetaOrmDao metaOrmDao;
    private final CrmFormConfig crmFormConfig;
    private final CurrentUserSession currentUserSession;
    private final CrmModelService crmModelService;

    @Autowired
    public CrmFormService(MetaOrmDao metaOrmDao, ConfigBuilder configBuilder,
            CurrentUserSession currentUserSession, CrmModelService crmModelService) {
        this.metaOrmDao = metaOrmDao;
        this.crmFormConfig = configBuilder.getFormConfig();
        this.currentUserSession = currentUserSession;
        this.crmModelService = crmModelService;
    }

    public CrmForm getAccountFilterForm() {
        DaoQuery daoQuery = DaoQuery.builder()
                .criteriaList(Arrays.asList(Criteria.where("gridType").is(GridType.account_filter)))
                .build();
        return metaOrmDao.findOneByQuery(daoQuery, CrmForm.class);
    }

    public CrmForm getById(String id, boolean isCommonApi) {
        CrmForm crmForm = null;
        if (isCommonApi) {
            crmForm = metaOrmDao.getById(id, CrmForm.class,
                    DaoQuery.keyValue(Constants.ACCOUNT_ID, Constants.RADYFY_ACCOUNT_ID));
        } else {
            crmForm = metaOrmDao.getById(id, CrmForm.class, null);
        }
        appendScopeFields(crmForm, isCommonApi);
        return crmForm;
    }


    public CrmForm getByModelId(String modelId, boolean isCommonApi) {
        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        if (isCommonApi) {
            criteriaList.add(Criteria.where(Constants.ACCOUNT_ID).is(Constants.RADYFY_ACCOUNT_ID));
        }
        criteriaList.add(Criteria.where("crmModelId").is(modelId));
        criteriaList.add(Criteria.where("gridType").is(GridType.form));
        DaoQuery daoQuery = DaoQuery.builder().criteriaList(criteriaList).build();
        CrmForm crmForm = metaOrmDao.findOneByQuery(daoQuery, CrmForm.class);
        appendScopeFields(crmForm, isCommonApi);
        return crmForm;
    }

    private void appendScopeFields(CrmForm crmForm, boolean isCommonApi) {
        CrmModel formModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);
        if (formModel != null) {
            Boolean isOrg = formModel.getIsOrg();
            Boolean isOrgScopeApplicable = formModel.getIsOrgScopeApplicable();
            Boolean isUserAccount = formModel.getIsUserAccount();
            if (Utils.isTrue(isOrg)) {

                List<FormGroup> formGroups = new ArrayList<>();
                formGroups.addAll(Arrays.asList(crmForm.getRows()));
                List<FormField> fields = new ArrayList<>();

                if (!Constants.ACCOUNT.equals(formModel.getParent())) {
                    List<BaseCrmModel> baseModels = crmModelService.getBaseModels();

                    if (Utils.isNotEmpty(baseModels)) {

                        AtomicInteger index = new AtomicInteger(0);

                        crmModelService.forEachBaseModels(baseModel -> {
                            String queryParam = null;

                            List<String> resetNextFieldKey = null;
                            // check not last index
                            if (baseModels.size() > index.get() + 1) {
                                resetNextFieldKey = Arrays
                                        .asList(baseModels.get(index.get() + 1).getFieldName());
                            }

                            if (baseModel.getOrder() > 1) {
                                List<String> fieldNames = baseModels.stream()
                                        .filter(b -> b.getOrder() == baseModel.getOrder() - 1)
                                        .map(BaseCrmModel::getFieldName).toList();

                                int i = 0;
                                queryParam = "";
                                for (String fieldName : fieldNames) {
                                    if (i > 0) {
                                        queryParam += "&";
                                    }
                                    queryParam += fieldName + "=:" + fieldName;
                                    i++;
                                }
                                queryParam += " " + String.join(" ", fieldNames);
                            }

                            CrmModel entity =
                                    crmModelService.getModel(baseModel.getModelId(), false);

                            FormField field = FormField.builder().id(baseModel.getFieldName())
                                    .title(entity.getName()).type(FormItem.Type.list).span(8)
                                    .nullIds(resetNextFieldKey)
                                    .meta(queryParam != null ? Map.of("queryParams", queryParam)
                                            : null)
                                    .build();
                            fields.add(field);

                            index.incrementAndGet();
                        }, formModel);
                    }
                }

                FormGroup row = new FormGroup();
                row.setLabel("Organization");
                row.setIsOrg(true);
                row.setFields(fields);

                formGroups.add(row);
                crmForm.setRows(formGroups.toArray(new FormGroup[0]));
            } else if (Utils.isTrue(isUserAccount)) {
                // Both org scope and access fields when isUserAccount is true
                appendOrgScopeFields(crmForm);
                appendAccessFields(crmForm);

            } else if (Utils.isTrue(isOrgScopeApplicable)) {
                // Only org scope fields when isOrgScopeApplicable is true
                appendOrgScopeFields(crmForm);

            }
        }
    }

    private void appendOrgScopeFields(CrmForm crmForm) {
        appendFields(crmForm, "");
    }

    private void appendAccessFields(CrmForm crmForm) {
        appendFields(crmForm, "_access");
    }

    private void appendFields(CrmForm crmForm, String scopeType) {
        List<FormGroup> formGroups = new ArrayList<>();
        formGroups.addAll(Arrays.asList(crmForm.getRows()));

        List<FormField> fields = new ArrayList<>();
        List<BaseCrmModel> baseModels = crmModelService.getBaseModels();


        if (Utils.isNotEmpty(baseModels)) {
            Document userDocument = new Document();

            if(currentUserSession.getUser() != null) {
                userDocument = currentUserSession.getUser().getDocument();
            }
            int index = 0;
            for (BaseCrmModel model : baseModels) {
                String queryParam = null;

                List<String> resetNextFieldKey = null;
                // check not last index
                if (baseModels.size() > index + 1) {
                    resetNextFieldKey =
                            Arrays.asList(scopeType + baseModels.get(index + 1).getFieldName());
                }

                List<String> currentUserAccess =
                    userDocument.getList(model.getFieldName(), String.class);
                boolean allAllowed = currentUserAccess == null || currentUserAccess.contains("All");

                if (model.getOrder() > 1) {
                    List<String> fieldNames = baseModels.stream()
                            .filter(baseModel -> baseModel.getOrder() < model.getOrder())
                            .map(BaseCrmModel::getFieldName).toList();

                    int i = 0;
                    queryParam = "";
                    List<String> scopedFieldNames =
                            fieldNames.stream().map(fieldName -> scopeType + fieldName).toList();
                    for (String fieldName : fieldNames) {
                        if (i > 0) {
                            queryParam += "&";
                        }
                        queryParam += fieldName + "=:" + (scopeType + fieldName);
                        i++;
                    }
                    queryParam += " " + String.join(" ", scopedFieldNames);
                }

                CrmModel entity = crmModelService.getModel(model.getModelId(), false);

                Object value = allAllowed ? List.of("All") : List.of();
                Document meta = new Document();
                if (queryParam != null) {
                    meta.put("queryParams", queryParam);
                }
                if (allAllowed) {
                    meta.put("absentValue", "All");
                }

                FormField field = FormField.builder().id(scopeType + model.getFieldName())
                        .title(entity.getName()).type(FormItem.Type.list).span(8)
                        .nullIds(resetNextFieldKey).value(value).multiple(true).meta(meta).build();
                fields.add(field);

                index++;
            }
        }

        FormGroup row = new FormGroup();
        row.setLabel(scopeType.isEmpty() ? "Organization Scope" : "Access Scope");
        row.setFields(fields);
        row.setIsOrg(true);
        formGroups.add(row);
        crmForm.setRows(formGroups.toArray(new FormGroup[0]));
    }


    public void appendOrgAccountFields(CrmForm crmForm, CrmModel formModel) {
        Arrays.asList(crmForm.getRows()).forEach(row -> {
            if (Utils.isTrue(row.getIsOrg())) {
                FormField accountField = FormField.builder().id("account").title("Account")
                        .type(FormItem.Type.list).span(8).disableCondition("return true;")
                        .value("account")
                        .options(Arrays.asList(
                                new Option("account", currentUserSession.getAccount().getName())))
                        .build();
                row.getFields().add(0, accountField);
            }
        });
        Boolean isUserAccount = formModel.getIsUserAccount();
        if (Utils.isTrue(isUserAccount)) {
            Arrays.asList(crmForm.getRows()).forEach(row -> {
                if (Utils.isNotEmpty(row.getFields())) {
                    row.getFields().forEach(field -> {
                        if (field.getId().equals("email")) {
                            field.setType(FormItem.Type.search);

                            field.setApiUrl(
                                    "/api/io/crm/account/user/search/emails?email=:$meta.validEmail $meta.validEmail");
                            field.setSelectApi("/api/io/crm/account/user/form/values");


                            Map<String, Object> meta = field.getMeta();
                            if (meta == null) {
                                meta = new HashMap<>();
                                field.setMeta(meta);
                            }
                            meta.put("autoSelect", true);


                            List<Option> onChangeCondition = field.getOnChangeCondition();
                            if (onChangeCondition == null) {
                                onChangeCondition = new ArrayList<>();
                                field.setOnChangeCondition(onChangeCondition);
                            }
                            Option changeCondition = new Option("$meta.validEmail", "return /"
                                    + Regex.email + "/.test(this.email) ? this.email : '';");
                            changeCondition.setMeta(Map.of("type", "function"));
                            onChangeCondition.add(changeCondition);
                        }
                    });
                }
            });
        }
    }

    public void runEventListener(CrmFormConfig.Event event, CrmForm crmForm, CrmModel crmModel,
            GridRequestParams gridRequestParams) {
        runEventListener(event, crmForm, crmModel, gridRequestParams, null, null, null);
    }

    public Object runEventListener(CrmFormConfig.Event event, CrmForm crmForm, CrmModel crmModel,
            GridRequestParams gridRequestParams, Document document, DaoQuery daoQuery,
            Update update) {
        return crmFormConfig.runConsumer(event, new CrmFormConsumerProps(crmForm, crmModel,
                gridRequestParams, document, daoQuery, update, null));
    }
}
