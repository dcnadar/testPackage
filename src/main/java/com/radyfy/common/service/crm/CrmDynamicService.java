package com.radyfy.common.service.crm;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import com.radyfy.common.auth.PasswordHash;
import com.radyfy.common.commons.Api;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.CrmForms;
import com.radyfy.common.commons.CrmModels;
import com.radyfy.common.commons.Errors;
import com.radyfy.common.commons.Regex;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.AccountTag;
import com.radyfy.common.model.BaseEntityModel;
import com.radyfy.common.model.commons.ExportForm;
import com.radyfy.common.model.crm.api.CrmApi;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.crm.model.CrmModelType;
import com.radyfy.common.model.crm.model.DataType;
import com.radyfy.common.model.crm.model.ModelProperty;
import com.radyfy.common.model.crm.model.Searchable;
import com.radyfy.common.model.crm.page.EntityActionMeta;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.dynamic.DocCreateResult;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.card.CardData;
import com.radyfy.common.model.dynamic.form.*;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.dynamic.table.Column;
import com.radyfy.common.model.dynamic.table.TableColumn;
import com.radyfy.common.model.enums.UserStatus;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.response.CheckboxGroup;
import com.radyfy.common.response.TableResult;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.config.CrmFormConfig;
import com.radyfy.common.service.crm.config.common_grids.CrmGridConfig;
import com.radyfy.common.service.crm.config.model.CrmModelConfig;
import com.radyfy.common.utils.CrmUtils;
import com.radyfy.common.service.crm.grid.CrmFormService;
import com.radyfy.common.service.crm.grid.CrmGridService;
import com.radyfy.common.service.crm.grid.CrmTableService;
import com.radyfy.common.service.crm.permission.CrmPermissionService;
import com.radyfy.common.service.filestore.FileStorageService;
import com.radyfy.common.utils.*;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
interface SearchFieldConsumer {
    void run(ModelProperty field, Searchable searchable, String prefix);
}


class ValidateResult {
    String error;
    boolean saveToDB;

    public ValidateResult(String error) {
        this.error = error;
        this.saveToDB = !Utils.isNotEmpty(error);
    }

    public ValidateResult(boolean saveToDB) {
        this.saveToDB = saveToDB;
    }
}


class TypeConvertResult {
    Object value;
    boolean converted;

    public TypeConvertResult(Object value) {
        this.value = value;
    }

    public TypeConvertResult(Object value, boolean converted) {
        this.value = value;
        this.converted = converted;
    }
}


record ParentModel(String key, CrmModel value) {
}


@FunctionalInterface
interface CrmFormFieldConsumer {
    void run(FormField field, ModelProperty modelProperty, String prefix, Integer topContextGroup);
}


@Component
public class CrmDynamicService {
    private static final Logger logger = LoggerFactory.getLogger(CrmDynamicService.class);
    private final EntityOrmDao crmAccountOrmDao;
    private final MetaOrmDao metaOrmDao;
    private final CrmModelService crmModelService;
    private final CrmFormService crmFormService;
    private final CrmTableService crmTableService;
    private final CrmGridService crmGridService;
    private final FileStorageService fileStorageService;
    private final CrmPermissionService crmPermissionService;
    private final EntityOrmDao entityOrmDao;
    private final PasswordHash passwordHash;
    private final CurrentUserSession currentUserSession;


    @Autowired
    public CrmDynamicService(EntityOrmDao crmAccountOrmDao, CrmModelService crmModelService,
            CrmFormService crmFormService, CrmTableService crmTableService, MetaOrmDao metaOrmDao,
            CrmGridService crmGridService, FileStorageService fileStorageService,
            CrmPermissionService crmPermissionService, EntityOrmDao entityOrmDao,
            PasswordHash passwordHash, CurrentUserSession currentUserSession) {
        this.crmAccountOrmDao = crmAccountOrmDao;
        this.crmModelService = crmModelService;
        this.crmFormService = crmFormService;
        this.crmTableService = crmTableService;
        this.metaOrmDao = metaOrmDao;
        this.crmGridService = crmGridService;
        this.fileStorageService = fileStorageService;
        this.crmPermissionService = crmPermissionService;
        this.entityOrmDao = entityOrmDao;
        this.passwordHash = passwordHash;
        this.currentUserSession = currentUserSession;
    }

    public CrmTable table(String tableId, boolean isCommonApi) {
        return table(crmTableService.getById(tableId, isCommonApi), new GridRequestParams(),
                isCommonApi, new TableRequest());
    }

    public CrmTable table(CrmTable crmTable, GridRequestParams filters, boolean isCommonApi,
            TableRequest tableRequest) {
        CrmModel tableModel = crmModelService.getModel(crmTable.getCrmModelId(), isCommonApi);

        if (tableModel.getModelType() == CrmModelType.COLLECTION) {

            List<String> feColumns = getColumnsFe(tableRequest);

            if (crmTable.getTableMeta() != null) {
                if (crmTable.getTableMeta().getDefaultSort() != null) {
                    tableRequest.setSort(crmTable.getTableMeta().getDefaultSort());
                }
            }

            /*
             * Setting Criteria
             */
            if (tableRequest.getAdditionalCriterias() == null) {
                tableRequest.setAdditionalCriterias(new ArrayList<>());
            }

            if (crmTable.getTableMeta() != null) {
                List<Criteria> defaultFilterCriteria = CrmUtils.buildCriteriaFromDefaultFilters(
                        crmTable.getTableMeta().getDefaultFilters());
                tableRequest.getAdditionalCriterias().addAll(defaultFilterCriteria);
            }

            tableRequest.getAdditionalCriterias()
                    .addAll(filters.getGridFiltersCriteria(crmTable.getGridParams()));
            if (Utils.isNotEmpty(tableRequest.getQ())) {
                List<CriteriaDefinition> orCriteria = new ArrayList<>();
                forEachSearchable(tableModel, "",
                        ((field, searchable, prefix) -> orCriteria.add(
                                buildOrCriteria(tableRequest.getQ(), prefix + field.getKey()))),
                        isCommonApi);
                if (!orCriteria.isEmpty()) {
                    tableRequest.getAdditionalCriterias()
                            .add(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
                }
            }

            /*
             * Setting Fields to fetch
             */
            Set<String> fields = new HashSet<>(filters.keySet());
            for (TableColumn tableColumn : crmTable.getColumns()) {
                if (tableColumn.getType() != Column.Type.actions
                        && tableColumn.getType() != Column.Type.options) {
                    setShowTableColumnFe(feColumns, tableColumn);
                    fields.add(tableColumn.getKey());
                }
            }
            if (Utils.isNotEmpty(crmTable.getFetchAdditionalKeys())) {
                fields.addAll(crmTable.getFetchAdditionalKeys());
            }
            if (Utils.isNotEmpty(crmTable.getGridParams())) {
                for (GridParam gridParam : crmTable.getGridParams()) {
                    fields.add(gridParam.getFilterKey());
                }
            }
            tableRequest.setFields(new ArrayList<>(fields));

            /*
             * Running before load crm table consumer written by User
             */
            crmTableService.runBeforeLoadConsumer(crmTable, tableModel, filters, tableRequest);

            /*
             * Fetching data
             */
            TableResult<Document> tableResult = crmAccountOrmDao.table(tableRequest, tableModel);
            crmTable.setData(tableResult.getData());
            crmTable.setHm(tableResult.isHm());
            crmTable.setTotal(tableResult.getTotal());
        } else {
            /*
             * Running before load crm table consumer written by User
             */
            crmTableService.runBeforeLoadConsumer(crmTable, tableModel, filters, tableRequest);
        }

        /*
         * Normalizing data
         */
        resolveColumnsData(crmTable, tableModel, filters, isCommonApi);
        if (Utils.isNotEmpty(crmTable.getGridParams())) {
            updateButtonSlug(crmTable.getCreateButton(), filters, crmTable.getGridParams());
            if (Utils.isNotEmpty(crmTable.getActions())) {
                for (Button action : crmTable.getActions()) {
                    updateButtonSlug(action, filters, crmTable.getGridParams());
                }
            }
        }

        if (Utils.isNotEmpty(crmTable.getLeftOpenFilters())) {
            syncValuesCrmForm(crmTable.getLeftOpenFilters(), tableModel,
                    new Document((Map) filters), isCommonApi);
        }

        if (Utils.isNotEmpty(crmTable.getCustomFilters())) {
            syncValuesCrmForm(crmTable.getCustomFilters(), tableModel, new Document((Map) filters),
                    isCommonApi);
        }

        /*
         * Checking for formItems
         */
        // if (crmTable.getSelectable() != null &&
        // Utils.isNotEmpty(crmTable.getSelectable().getCrmFormId())) {
        // CrmForm crmForm =
        // crmFormService.getById(crmTable.getSelectable().getCrmFormId());
        // crmTable.setMetaValue("formItem", crmForm.getRows());
        // crmTable.setMetaValue("add", Utils.isTrue(crmForm.getAdd()));
        // }

        /*
         * Running on load crm table consumer written by User
         */
        crmTableService.runOnLoadConsumer(crmTable, tableModel, filters, tableRequest);

        return crmTable;
    }

    private boolean fetchDocWithoutId(CrmForm crmForm) {

        // boolean anyNonIdParam = false;
        // if(Utils.isNotEmpty(crmForm.getGridParams())){
        // anyNonIdParam = crmForm.getGridParams().stream().anyMatch(g ->
        // !g.getFilterKey().equals("id") && g.isRequired());
        // }
        // return Utils.isTrue(crmForm.getFetchDocWithoutId()) || anyNonIdParam;

        return Utils.isTrue(crmForm.getFetchDocWithoutId());
    }

    private boolean hasDocIdParam(List<GridParam> gridParams, GridRequestParams params) {

        if (params.containsKey("id")) {
            if (Utils.isNotEmpty(gridParams)
                    && gridParams.stream().anyMatch(g -> "id".equals(g.getKey())
                            && (g.getDocumentKey() != null && !"id".equals(g.getDocumentKey())))) {
                return false;
            }
            return true;
        }
        if (Utils.isNotEmpty(gridParams)) {
            return gridParams.stream().anyMatch(g -> "id".equals(g.getFilterKey()));
        }

        return false;
    }

    public CrmGrid getCrmGrid(CrmGrid crmGrid, GridRequestParams params, boolean isCommonApi) {
        CrmModel crmModel = null;
        if (crmGrid.getCrmModelId() != null) {
            crmModel = crmModelService.getModel(crmGrid.getCrmModelId(), isCommonApi);
        }
        return crmGridService.runEventListener(CrmGridConfig.Event.ON_LOAD, crmGrid, crmModel,
                params);
    }

    public CrmForm crmForm(CrmForm crmForm, GridRequestParams params, boolean isCommonApi) {
        CrmModel formModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);

        /*
         * Fetching crm form document
         */
        Document document = null;
        if (formModel.getModelType() == CrmModelType.COLLECTION) {
            if (Utils.isNotEmpty(params)) {
                if (hasDocIdParam(crmForm.getGridParams(), params)) {

                    // TODO add fields in daoQuery
                    DaoQuery daoQuery = DaoQuery.builder()
                            .criteriaList(
                                    params.getGridFiltersCriteriaWithId(crmForm.getGridParams()))
                            .build();

                    /*
                     * Running crm form on load consumer written by User
                     */
                    crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_LOAD, crmForm,
                            formModel, params, null, daoQuery, null);

                    document = crmAccountOrmDao.findOneByQuery(daoQuery, formModel);
                    if (document == null) {
                        throw new RuntimeException("Not Found");
                    }
                } else {
                    /*
                     * fetching document if 'fetchDocWithoutId' flag is enabled OR assuming
                     * additional filters we have added gridParams in crm form
                     */
                    if (fetchDocWithoutId(crmForm)) {
                        DaoQuery daoQuery = DaoQuery.builder()
                                .criteriaList(
                                        params.getGridFiltersCriteria(crmForm.getGridParams()))
                                .build();

                        /*
                         * Running crm form before load consumer written by User
                         */
                        crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_LOAD, crmForm,
                                formModel, params, null, daoQuery, null);

                        document = crmAccountOrmDao.findOneByQuery(daoQuery, formModel);
                    } else {

                        /*
                         * Running crm form before load consumer written by User
                         */
                        crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_LOAD, crmForm,
                                formModel, params);

                        document = new Document();
                        params.setFiltersToDoc(crmForm.getGridParams(), document);
                    }
                }
            } else if (fetchDocWithoutId(crmForm)) {

                DaoQuery daoQuery = DaoQuery.builder().build();

                /*
                 * Running crm form before load consumer written by User
                 */
                crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_LOAD, crmForm, formModel,
                        params, null, daoQuery, null);

                document = crmAccountOrmDao.findOneByQuery(daoQuery, formModel);
            }
            /*
             * Setting id param in api url
             */
            if (document != null && document.containsKey("_id")) {
                params.put(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()),
                        document.getString("_id"));
                crmForm.setApiUrl(
                        params.queryStringWithId(crmForm.getApiUrl(), crmForm.getGridParams()));
            }
        } else {
            if (Utils.isNotEmpty(params)) {
                document = new Document();
                params.setFiltersToDoc(crmForm.getGridParams(), document);
            }
            /*
             * Running crm form before load consumer written by User
             */
            crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_LOAD, crmForm, formModel,
                    params, document, null, null);
        }


        applyUserAccessScopeData(document, formModel);
        syncValuesCrmForm(crmForm.getRows(), formModel, document, isCommonApi);
        crmFormService.appendOrgAccountFields(crmForm, formModel);

        /*
         * Running crm form on load consumer written by User
         */
        crmFormService.runEventListener(CrmFormConfig.Event.ON_LOAD, crmForm, formModel, params,
                document, null, null);

        return crmForm;
    }

    public void applyUserAccessScopeData(Document document, CrmModel crmModel) {


        if (Utils.isTrue(crmModel.getIsUserAccount())) {
            if (document != null && document.containsKey("userId")) {
                CrmModel userCrmModel = crmModelService.getModel(CrmModels.RADYFY_USER_MODEL, true);
                Document userDoc =
                        entityOrmDao.getById(document.getString("userId"), userCrmModel, null);
                List<BaseCrmModel> baseModels = crmModelService.getBaseModels();

                for (BaseCrmModel baseModel : baseModels) {
                    String fieldName = baseModel.getFieldName();
                    if (userDoc.containsKey(fieldName)) {
                        document.put("_access" + fieldName, userDoc.get(fieldName));
                    } else {
                        document.put("_access" + fieldName, List.of("All"));
                    }
                }
            }
        }
    }

    public Object getCrmModelData(CrmApi ecomApi, GridRequestParams params, boolean isCommonApi) {
        CrmModel formModel = crmModelService.getModel(ecomApi.getModelId(), isCommonApi);

        /*
         * Fetching crm form document
         */
        Document document = null;
        if (Utils.isNotEmpty(params)) {
            if (hasDocIdParam(ecomApi.getGridParams(), params)) {

                // TODO add fields in daoQuery
                DaoQuery daoQuery = DaoQuery.builder()
                        .criteriaList(params.getGridFiltersCriteriaWithId(ecomApi.getGridParams()))
                        .build();

                document = crmAccountOrmDao.findOneByQuery(daoQuery, formModel);
                if (document == null) {
                    throw new RuntimeException("Not Found");
                }
            } else {
                /*
                 * fetching document if 'fetchDocWithoutId' flag is enabled OR assuming additional
                 * filters we have added gridParams in crm form
                 */
                if (Utils.isTrue(ecomApi.getFetchDocWithoutId())) {
                    DaoQuery daoQuery = DaoQuery.builder()
                            .criteriaList(params.getGridFiltersCriteria(ecomApi.getGridParams()))
                            .build();

                    document = crmAccountOrmDao.findOneByQuery(daoQuery, formModel);
                } else {
                    if (document == null) {
                        throw new RuntimeException("Not valid request");
                    }
                }
            }
        } else if (Utils.isTrue(ecomApi.getFetchDocWithoutId())) {

            DaoQuery daoQuery = DaoQuery.builder().build();

            document = crmAccountOrmDao.findOneByQuery(daoQuery, formModel);
        }
        /*
         * Setting id param in path
         */
        if (document != null && document.containsKey("_id")) {
            params.put(CrmUtils.getCrmFormIdGridKey(ecomApi.getGridParams()),
                    document.getString("_id"));
            ecomApi.setPath(params.queryStringWithId(ecomApi.getPath(), ecomApi.getGridParams()));
        }

        return document;
    }

    public void syncValuesCrmForm(FormGroup[] formGroups, CrmModel crmModel, Document currentDoc,
            boolean isCommonApi) {
        syncValuesCrmForm(formGroups, crmModel, currentDoc, currentDoc, "", isCommonApi);
    }

    public void syncValuesCrmForm(FormGroup[] formGroups, CrmModel crmModel, Document currentDoc,
            Document mainDoc, String parentPath, boolean isCommonApi) {

        for (FormGroup formGroup : formGroups) {
            for (FormField formField : formGroup.getFields()) {
                ModelProperty modelProperty =
                        getModelProperty(crmModel, formField.getId(), isCommonApi);
                syncFormField(formField, modelProperty, currentDoc, mainDoc, parentPath,
                        isCommonApi);
            }
        }
    }

    @Transactional
    public DocCreateResult createDocumentForCrmForm(Document document, CrmForm crmForm,
            boolean isCommonApi) {
        return createDocumentForCrmForm(document, new GridRequestParams(), crmForm, isCommonApi);
    }

    @Transactional
    public DocCreateResult createDocumentForCrmForm(Document document, GridRequestParams params,
            CrmForm crmForm, boolean isCommonApi) {
        CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);

        return createDocumentForCrmForm(document, params, crmForm, crmModel, isCommonApi);
    }

    private DocCreateResult createDocumentForCrmForm(Document document, GridRequestParams params,
            CrmForm crmForm, CrmModel crmModel, boolean isCommonApi) {
        Document finalDoc = generateNewDocWithAvailableFields(crmForm, document);
        params.setFiltersToDoc(crmForm.getGridParams(), finalDoc);

        convertDataType(finalDoc, crmForm, crmModel, isCommonApi);

        validateToSaveCrmForm(null, crmForm, crmModel, finalDoc, null, null, isCommonApi);

        /*
         * Creating user when isUserAccount is true
         */
        createUser(crmModel, finalDoc);
        /*
         * Running crm form before_create consumer written by User
         */
        crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_CREATE, crmForm, crmModel,
                params, finalDoc, null, null);

        Document savedDoc = crmAccountOrmDao.create(finalDoc, crmModel);

        /*
         * Setting id param in api url
         */
        params.put(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()),
                savedDoc.getString("_id"));
        crmForm.setApiUrl(params.queryStringWithId(crmForm.getApiUrl(), crmForm.getGridParams()));

        /*
         * Running crm form after_create consumer written by User
         */
        Object returnValue = crmFormService.runEventListener(CrmFormConfig.Event.AFTER_CREATE,
                crmForm, crmModel, params, savedDoc, null, null);

        return new DocCreateResult(savedDoc, returnValue);
    }

    @Transactional
    public Object updateDocumentForCrmForm(Document document, GridRequestParams params,
            CrmForm crmForm, boolean isCommonApi) {
        if (!hasDocIdParam(crmForm.getGridParams(), params)) {
            throw new AuthException();
        }
        return saveDocumentForCrmForm(document, params, crmForm, false, isCommonApi);
    }

    @Transactional
    public Object upsertDocumentForCrmForm(Document document, GridRequestParams params,
            CrmForm crmForm, boolean isCommonApi) {
        return saveDocumentForCrmForm(document, params, crmForm, true, isCommonApi);
    }

    private Object saveDocumentForCrmForm(Document document, GridRequestParams params,
            CrmForm crmForm, boolean upsert, boolean isCommonApi) {

        CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);

        Document finalDoc = generateNewDocWithAvailableFields(crmForm, document);
        params.setFiltersToDoc(crmForm.getGridParams(), finalDoc);
        String id = params.get(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()));
        if (!upsert) {
            finalDoc.put(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()), id);
        }

        convertDataType(finalDoc, crmForm, crmModel, isCommonApi);

        /*
         * Validation and creating update map
         */
        Update update = new Update();
        validateToSaveCrmForm(id, crmForm, crmModel, finalDoc, null, update, isCommonApi);

        /*
         * Creating user when isUserAccount is true
         */
        createUser(crmModel, finalDoc);

        /*
         * Update Criteria
         */
        DaoQuery daoQuery = DaoQuery.builder()
                .criteriaList(upsert ? params.getGridFiltersCriteria(crmForm.getGridParams())
                        : params.getGridFiltersCriteriaWithId(crmForm.getGridParams()))
                .build();

        /*
         * Running crm form before_update consumer written by User
         */
        crmFormService.runEventListener(CrmFormConfig.Event.BEFORE_UPDATE, crmForm, crmModel,
                params, finalDoc, daoQuery, update);

        if (upsert) {
            crmAccountOrmDao.upsert(daoQuery, update, crmModel);
        } else {
            crmAccountOrmDao.updateByQuery(daoQuery, update, crmModel);
        }

        /*
         * Running crm form after_update consumer written by User
         */
        return crmFormService.runEventListener(CrmFormConfig.Event.AFTER_UPDATE, crmForm, crmModel,
                params, finalDoc, daoQuery, update);

    }

    public List<Option> search(CrmModel crmModel, String query, GridRequestParams gridRequestParams,
            String modelUniqueKey, List<String> additionalKeys, boolean isCommonApi) {

        return search(crmModel, query, gridRequestParams.getGridFiltersCriteria(), false,
                modelUniqueKey, additionalKeys, isCommonApi);
    }

    public List<Option> search(CrmModel crmModel, String query,
            List<CriteriaDefinition> criteriaList, boolean isCommonApi) {
        return search(crmModel, query, criteriaList, false, null, null, isCommonApi);
    }

    public List<Option> search(CrmModel crmModel, String query,
            List<CriteriaDefinition> criteriaList, boolean primary, String modelUniqueKey,
            List<String> additionalKeys, boolean isCommonApi) {

        return search(crmModel, query, criteriaList, primary, additionalKeys, 100, modelUniqueKey,
                isCommonApi);
    }

    public List<Option> search(CrmModel crmModel, String query,
            List<CriteriaDefinition> criteriaList, boolean primary, List<String> additionalKeys,
            int limit, String modelUniqueKey, boolean isCommonApi) {

        Map<String, Searchable> fieldList = new LinkedHashMap<>();
        List<CriteriaDefinition> orCriteria = new ArrayList<>();

        forEachSearchable(crmModel, "", ((field, searchable, prefix) -> {
            String key = prefix + field.getKey();
            // to find only on primary key
            if (primary) {
                if (Utils.isTrue(searchable.getPrimary())) {
                    if (Utils.isNotEmpty(query)) {
                        String regex = ".*" + query + ".*";
                        orCriteria.add(Criteria.where(key).regex(regex, "si"));
                    }
                    fieldList.put(key, searchable);
                }
            } else {
                if (Utils.isTrue(searchable.getThumbColor())) {
                    fieldList.put(key, searchable);
                } else if (Utils.isTrue(searchable.getThumbInitials())) {
                    if (Utils.isNotEmpty(query)) {
                        orCriteria.add(buildOrCriteria(query, key));
                    }
                    fieldList.put(key, searchable);
                } else if (Utils.isTrue(searchable.getThumb())) {
                    fieldList.put(key, searchable);
                } else if (Utils.isTrue(searchable.getDescription())) {
                    if (Utils.isNotEmpty(query)) {
                        orCriteria.add(buildOrCriteria(query, key));
                    }
                    fieldList.put(key, searchable);
                } else {
                    if (Utils.isNotEmpty(query)) {
                        orCriteria.add(buildOrCriteria(query, key));
                    }

                    if (Utils.isTrue(searchable.getValue())) {
                        fieldList.put(key, searchable);
                    }
                }
            }
        }), false);
        if (Utils.isNotEmpty(orCriteria)) {
            criteriaList.add(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
        }
        Set<String> finalList = new HashSet<>(fieldList.keySet());
        if (Utils.isNotEmpty(additionalKeys)) {
            finalList.addAll(additionalKeys);
        }
        if (Utils.isNotEmpty(modelUniqueKey)) {
            finalList.add(modelUniqueKey);
        }
        List<Document> response =
                crmAccountOrmDao.findByQuery(DaoQuery.builder().criteriaList(criteriaList)
                        .fields(new ArrayList<>(finalList)).limit(limit).build(), crmModel);
        List<Option> result = new ArrayList<>();
        if (Utils.isNotEmpty(response)) {
            for (Document o : response) {
                try {
                    Option option = new Option();
                    StringBuilder value = new StringBuilder();
                    String optionKey = o.getString("_id");

                    for (String key : finalList) {

                        String v = String.valueOf(BsonDocumentUtils.getDataValue(o, key));
                        if (Utils.isNotEmptyOrNull(v)) {
                            if (key.equals(modelUniqueKey)) {
                                optionKey = v;
                            }
                            Searchable searchable = fieldList.get(key);
                            if (searchable != null) {
                                if (Utils.isTrue(searchable.getPrimary())) {
                                    value.append(v);
                                } else if (Utils.isTrue(searchable.getThumbColor())) {
                                    option.setThumbColor(v);
                                } else if (Utils.isTrue(searchable.getThumbInitials())) {
                                    option.setThumbInitials(v);
                                } else if (Utils.isTrue(searchable.getThumb())) {
                                    Object thumb = BsonDocumentUtils.getDataValue(o, key);
                                    if (thumb instanceof List) {
                                        option.setThumb(((List<String>) thumb).get(0));
                                    } else {
                                        option.setThumb(v);
                                    }
                                } else if (Utils.isTrue(searchable.getDescription())) {
                                    // append to description
                                    if (Utils.isNotEmpty(option.getDescription())) {
                                        option.setDescription(option.getDescription() + " " + v);
                                    } else {
                                        option.setDescription(v);
                                    }
                                } else {
                                    value.append(v).append(" ");
                                }
                            }
                            if (additionalKeys != null && additionalKeys.contains(key)) {
                                if (option.getMeta() == null) {
                                    option.setMeta(new HashMap<>());
                                }
                                MapUtils.setDataValue(option.getMeta(), key,
                                        BsonDocumentUtils.getDataValue(o, key));
                            }
                        }
                    }
                    option.setValue(value.toString().trim());
                    option.setKey(optionKey);
                    result.add(option);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Get the list og tags.
     *
     * @param category tag category
     * @return List<Option>
     */
    public List<Option> getAccountTags(String category) {
        List<Option> tags = new ArrayList<>();
        List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
        criteriaDefinitions.add(Criteria.where("category").is(category));
        List<AccountTag> accountTags = metaOrmDao.findByQuery(
                DaoQuery.builder().criteriaList(criteriaDefinitions).build(), AccountTag.class);
        if (Utils.isNotEmpty(accountTags)) {
            AccountTag tag = accountTags.get(0);
            List<String> optionTags = tag.getName();
            tags.add(new Option("tags", optionTags));
        }
        return tags;
    }

    private <T> void forEachSearchable(CrmModel crmModel, String prefix,
            SearchFieldConsumer consumer, boolean isCommonApi) {
        List<ModelProperty> properties = crmModel.getProperties();
        for (ModelProperty property : properties) {
            // if (property.getDataType() == DataType.INNER_MODEL
            // || (property.getDataType() == DataType.LIST_OF && property.getListType() ==
            // DataType.INNER_MODEL)) {
            // forEachSearchable(
            // crmModelService.getModel(property.getModelId(), isCommonApi),
            // prefix + property.getKey() + ".", consumer, isCommonApi);
            // } else {
            if (isSearchable(property.getSearchable())) {
                consumer.run(property, property.getSearchable(), prefix);
            }
            // }
        }
    }

    private boolean isSearchable(Searchable searchable) {
        if (searchable == null) {
            return false;
        }
        return Utils.isTrue(searchable.getValue()) || Utils.isTrue(searchable.getThumbColor())
                || Utils.isTrue(searchable.getThumbInitials())
                || Utils.isTrue(searchable.getThumb()) || Utils.isTrue(searchable.getDescription())
                || Utils.isTrue(searchable.getPrimary());
    }

    private Document generateNewDocWithAvailableFields(CrmForm crmForm, Document obj) {
        Document newDoc = new Document();
        FormGroup[] formGroups = crmForm.getRows();
        for (FormGroup formGroup : formGroups) {
            for (FormField field : formGroup.getFields()) {
                String key = field.getId();
                Object value = BsonDocumentUtils.getDataValue(obj, key);
                if (value != null) {
                    BsonDocumentUtils.setDataValue(newDoc, key, value);
                }
            }
        }
        return newDoc;
    }

    private void convertDataType(Document document, CrmForm crmForm, CrmModel crmModel,
            boolean isCommonApi) {

        for (FormGroup formGroup : crmForm.getRows()) {
            for (FormField field : formGroup.getFields()) {
                String key = field.getId();
                Object value = BsonDocumentUtils.getDataValue(document, key);

                if (value != null) {
                    ModelProperty modelProperty = getModelProperty(crmModel, key, isCommonApi);

                    if (modelProperty.getDataType() == DataType.LIST_OF
                            && modelProperty.getListType() == DataType.INNER_MODEL) {

                        if (field.getType() == FormItem.Type.form_table
                                || field.getType() == FormItem.Type.input_table
                                || field.getType() == FormItem.Type.form_array) {

                            CrmForm innerCrmForm =
                                    getInnerCrmForm(field, modelProperty, document, isCommonApi);
                            if (innerCrmForm != null) {
                                List<Map<String, Object>> innerFormDataList =
                                        (List<Map<String, Object>>) value;
                                List<Document> convertedList = new ArrayList<>();
                                for (Map<String, Object> stringObjectMap : innerFormDataList) {
                                    Document innerDoc = new Document(stringObjectMap);
                                    convertedList.add(innerDoc);
                                    convertDataType(innerDoc, innerCrmForm, crmModelService
                                            .getModel(innerCrmForm.getCrmModelId(), isCommonApi),
                                            isCommonApi);
                                }
                                BsonDocumentUtils.setDataValue(document, key, convertedList);
                            }
                        }
                    } else if (modelProperty.getDataType() == DataType.INNER_MODEL
                            && field.getType() == FormItem.Type.inner_form) {

                        CrmForm innerCrmForm =
                                getInnerCrmForm(field, modelProperty, document, isCommonApi);
                        if (innerCrmForm != null) {
                            Document innerDoc = new Document((Map<String, Object>) value);
                            convertDataType(
                                    innerDoc, innerCrmForm, crmModelService
                                            .getModel(innerCrmForm.getCrmModelId(), isCommonApi),
                                    isCommonApi);
                            BsonDocumentUtils.setDataValue(document, key, innerDoc);
                        }
                    } else {

                        // converting value to correct data type
                        TypeConvertResult convertedValue = convertDataType(field,
                                modelProperty.getDataType(), value, modelProperty);

                        if (convertedValue != null) {
                            value = convertedValue.value;

                            if (convertedValue.converted) {
                                BsonDocumentUtils.setDataValue(document, key, value);
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateToSaveCrmForm(String id, CrmForm crmForm, CrmModel crmModel, Document obj,
            Map<String, Object> errors, Update update, boolean isCommonApi) {

        // first validate compound unique
        if (Utils.isNotEmpty(crmModel.getCompoundUniqueFields())) {
            for (List<String> unique : crmModel.getCompoundUniqueFields()) {
                String error = null;
                List<CriteriaDefinition> criteriaList = new ArrayList<>();
                StringBuilder nameCombination = new StringBuilder();
                int index = 0;
                for (String field : unique) {
                    FormField formField = getFormField(crmForm, field);
                    if (index == unique.size() - 1) {
                        nameCombination.append(" and ").append(formField.getTitle());
                    } else if (index == 0) {
                        nameCombination.append(formField.getTitle());
                    } else {
                        nameCombination.append(", ").append(formField.getTitle());
                    }
                    criteriaList.add(
                            Criteria.where(field).is(BsonDocumentUtils.getDataValue(obj, field)));
                    index++;
                }
                List<Document> results =
                        crmAccountOrmDao.findByQuery(
                                DaoQuery.builder().criteriaList(criteriaList)
                                        .fields(Collections.singletonList("_id")).build(),
                                crmModel);
                if (Utils.isNotEmpty(results)) {
                    if (Utils.isNotEmpty(id)) {
                        if (!id.equals(results.get(0).getString("_id"))) {
                            error = nameCombination + " is not unique";
                        }
                    } else {
                        error = nameCombination + " is not unique";
                    }
                }
                if (error != null) {
                    if (errors != null) {
                        MapUtils.setDataValue(errors, unique.get(0), error);
                        return;
                    }
                    throw new RuntimeException(error);
                }
            }
        }

        for (FormGroup formGroup : crmForm.getRows()) {
            for (FormField field : formGroup.getFields()) {
                String key = field.getId();
                Object value = BsonDocumentUtils.getDataValue(obj, key);
                if (Utils.isNotEmpty(id) && (field.getUpdateType() == FormItem.Visibility.hidden
                        || field.getUpdateType() == FormItem.Visibility.disabled)) {

                    continue;
                }

                if (!Utils.isNotEmpty(id) && (field.getCreateType() == FormItem.Visibility.hidden
                        || field.getCreateType() == FormItem.Visibility.disabled)) {

                    continue;
                }
                ValidateResult result = validateToSaveFormField(field, obj, id, crmModel, obj, "",
                        crmModel, errors, isCommonApi);
                if (result.error != null) {
                    if (errors != null) {
                        MapUtils.setDataValue(errors, key, result.error);
                        continue;
                    }
                    throw new RuntimeException(result.error);
                }
                if (result.saveToDB) {
                    if (update != null) {
                        if (value != null) {
                            // TODO add base model fieldNames
                            if (!(key.equals("_id") || key.equals(Constants.ACCOUNT_ID)
                                    || key.equals(Constants.ECOM_ACCOUNT_ID))) {
                                update.set(key, value);
                            }
                        } else {
                            update.unset(key);
                        }
                    }
                } else {
                    if (update != null) {
                        update.unset(key);
                    }
                }
            }
        }
    }

    private void validateToSaveInnerCrmForm(CrmForm crmForm, CrmModel crmModel, String rootDocId,
            Map<String, Object> thisobj, Document rootObj, String parentPath, CrmModel rootCrmModel,
            Map<String, Object> errors, boolean isCommonApi) {
        for (FormGroup formGroup : crmForm.getRows()) {
            for (FormField field : formGroup.getFields()) {
                ValidateResult result =
                        validateToSaveFormField(field, new Document(thisobj), rootDocId, crmModel,
                                rootObj, parentPath, rootCrmModel, errors, isCommonApi);
                // if errors exist then we don't need to hande the result, it will be handled by
                // the caller
                if (errors == null) {
                    if (result.error != null) {
                        throw new RuntimeException(result.error);
                    } else {
                        if (!result.saveToDB) {
                            BsonDocumentUtils.removeField(thisobj, field.getId());
                        }
                    }
                }
            }
        }
        // TODO remove extra fields from thisobj
    }

    private boolean isReferenceTypeModel(ModelProperty modelProperty) {
        return Utils.isNotEmpty(modelProperty.getModelId())
                && ((modelProperty.getDataType() == DataType.LIST_OF
                        && modelProperty.getListType() == DataType.REFERENCE)
                        || modelProperty.getDataType() == DataType.REFERENCE);
    }

    private boolean isOptionsField(FormField field) {
        return (field.getType() == FormItem.Type.list || field.getType() == FormItem.Type.radio
                || field.getType() == FormItem.Type.checkbox_group
                || field.getType() == FormItem.Type.card || field.getType() == FormItem.Type.tree);
    }

    private boolean isReferenceTypeField(FormField field, ModelProperty modelProperty) {
        return isOptionsField(field) && isReferenceTypeModel(modelProperty);
    }

    // private boolean isReferenceTypeColumn(TableColumn column, ModelProperty
    // modelProperty) {
    // return column.getType() == Column.Type.card
    // && isReferenceTypeModel(modelProperty);
    // }

    private boolean isModelDataNotExist(CrmModel crmModel, String value, String modelUniqueKey,
            Document finalDoc) {
        DaoQuery daoQuery = null;
        if (Utils.isNotEmpty(modelUniqueKey)) {
            daoQuery = DaoQuery.fromCriteria(Criteria.where(modelUniqueKey).is(value));
        } else {
            if ("All".equals(value)) {
                return false;
            }

            if (CrmModels.RADYFY_ROLE_MODEL.equals(crmModel.getId())) {
                if (crmPermissionService.isValidHardRole(value)) {
                    return false;
                }
            }

            if (CrmModels.RADYFY_ENTITY_MODEL.equals(crmModel.getId())) {
                if (crmModelService.isValidHardAccountValue(value)) {
                    return false;
                }
            }

            if (CrmModels.RADYFY_PAGE_MODEL.equals(crmModel.getId())) {
                Document account = currentUserSession.getUserSession().getFilterDocuments()
                        .get(Constants.ACCOUNT_ID);
                if (account != null
                        && !account.getString("_id").equals(Constants.RADYFY_ACCOUNT_ID)) {
                    daoQuery = DaoQuery.builder()
                            .criteriaList(List.of(Criteria.where("_id").is(new ObjectId(value)),
                                    Criteria.where("accountId").in(Constants.RADYFY_ACCOUNT_ID,
                                            account.getString("_id"))))
                            .build();
                }
            }

            if (CrmModels.RADYFY_PAGE_MODEL.equals(crmModel.getId()) || CrmModels.RADYFY_ENTITY_MODEL.equals(crmModel.getId())) {
                Document account = currentUserSession.getUserSession().getFilterDocuments()
                        .get(Constants.ACCOUNT_ID);
                if (account != null
                        && !account.getString("_id").equals(Constants.RADYFY_ACCOUNT_ID)) {
                    daoQuery = DaoQuery.builder()
                            .criteriaList(List.of(
                                new Criteria().orOperator(
                                Criteria.where("_id").is(new ObjectId(value)).where("accountId").is(account.getString("_id")),
                                Criteria.where("_id").in(
                                    CrmModels.RADYDY_COMMON_MODELS.stream()
                                    .map(id -> new ObjectId(id)).collect(Collectors.toList())
                                    .toArray(new ObjectId[0])
                                ).where("accountId").in(Constants.RADYFY_ACCOUNT_ID)))
                            )
                            .excludeFilters(List.of("accountId"))
                            .build();
                }
            }

            if (daoQuery == null) {
                daoQuery = DaoQuery.fromCriteria(Criteria.where("_id").is(new ObjectId(value)));
            }
        }
        // appendBaseFilters(crmModel, daoQuery, finalDoc);
        return crmAccountOrmDao.getCount(daoQuery, crmModel) == 0;

    }

    public static boolean validStringToSave(Object input, ModelProperty modelProperty) {

        boolean valid = false;

        if (input instanceof String) {

            if (containsScriptTag((String) input, modelProperty)) {
                return false;
            }

            if (containsEscapeSequences((String) input, modelProperty)) {
                return false;
            }
            return true;
        }
        return valid;
    }

    public static boolean containsScriptTag(String input, ModelProperty modelProperty) {


        if (Utils.isTrue(modelProperty.getScript())) {

            return false;

        } else {
            // Define a regular expression pattern to match the <script> tag
            String scriptPattern = "<script[^>]*>.*?</script>";

            // Create a Pattern object
            Pattern pattern = Pattern.compile(scriptPattern, Pattern.CASE_INSENSITIVE);

            // Create a Matcher object
            Matcher matcher = pattern.matcher(input);

            // Check if the <script> tag is found
            return matcher.find();
        }


    }

    public static boolean containsEscapeSequences(String input, ModelProperty modelProperty) {

        if (Utils.isTrue(modelProperty.getEscapeChar())) {

            return false;

        } else {
            // Check for common escape sequences that could be malicious
            String[] dangerousEscapes = {"\\x", "\\u", "\\0", "\\n", "\\r", "\\t", "\\v", "\\f",
                    "\\a", "\\b", "\\\\", "\\'", "\\\"", "\\?", "\\e", "\\033", "\\x1b", "\\u001b"};

            for (String escape : dangerousEscapes) {
                if (input.contains(escape)) {
                    return true;
                }
            }

            // Check for octal escape sequences (\\ followed by 1-3 octal digits)
            Pattern octalPattern = Pattern.compile("\\\\[0-7]{1,3}");
            if (octalPattern.matcher(input).find()) {
                return true;
            }

            // Check for hex escape sequences (\\x followed by hex digits)
            Pattern hexPattern = Pattern.compile("\\\\x[0-9a-fA-F]{1,2}");
            if (hexPattern.matcher(input).find()) {
                return true;
            }

            // Check for unicode escape sequences (\\u followed by 4 hex digits)
            Pattern unicodePattern = Pattern.compile("\\\\u[0-9a-fA-F]{4}");
            if (unicodePattern.matcher(input).find()) {
                return true;
            }

            return false;
        }

    }

    private ValidateResult validateFormTypeValue(FormField field, Object value) {

        switch (field.getType()) {
            case email:
                if (!ValidationUtils.isValidEmail((String) value)) {
                    return new ValidateResult(field.getTitle() + " is not valid");
                }
                break;
            case upload_v2: {
                if (Utils.isTrue(field.getMultiple())) {
                    if (value instanceof List) {
                        List<Map<String, Object>> files = (List<Map<String, Object>>) value;
                        for (Map<String, Object> file : files) {
                            if (!(Utils.isNotEmpty(file.get("path"))
                                    && Utils.isNotEmpty(file.get("size"))
                                    && Utils.isNotEmpty(file.get("type")))) {
                                return new ValidateResult("Please upload file properly");
                            }
                        }
                    } else {
                        return new ValidateResult("Please upload file properly");
                    }
                } else {
                    if (value instanceof Map) {
                        Map<String, Object> file = (Map<String, Object>) value;
                        if (!(Utils.isNotEmpty(file.get("path"))
                                && Utils.isNotEmpty(file.get("size"))
                                && Utils.isNotEmpty(file.get("type")))) {
                            return new ValidateResult("Please upload file properly");
                        }
                    } else {
                        return new ValidateResult("Please upload file properly");
                    }
                }
                break;
            }
            default:
                break;
        }
        return null;
    }

    private boolean validValueTypeDataType(FormField formField, DataType dataType, Object value,
            ModelProperty modelProperty) {

        switch (dataType) {

            case REFERENCE:
            case STRING:

                if (validStringToSave(value, modelProperty)) {
                    return true;
                }
                break;
            case INTEGER:
                if (value instanceof Integer) {
                    return true;
                }
                break;
            case LONG:
                if (value instanceof Integer || value instanceof Long) {
                    return true;
                }
                break;
            case DOUBLE:
                if (value instanceof Integer || value instanceof Long || value instanceof Float
                        || value instanceof Double) {
                    return true;
                }
                break;
            case DATE:
                if (Utils.isNotEmpty(formField.getFormat())) {
                    if (value instanceof String) {
                        return true;
                    }
                }
                if (value instanceof Date) {
                    return true;
                }
                break;
            case BOOLEAN:
                if (value instanceof Boolean) {
                    return true;
                }
                break;
            case LIST_OF:
                if (value instanceof List) {
                    if (modelProperty.getListType().equals(DataType.STRING)) {
                        for (String item : (List<String>) value) {
                            if (!validStringToSave(item, modelProperty)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                break;
            default:
                return true;
        }
        return false;
    }

    private TypeConvertResult convertDataType(FormField formField, DataType dataType, Object value,
            ModelProperty modelProperty) {

        if (value != null && dataType != null) {
            switch (dataType) {

                case REFERENCE:
                case STRING:
                    String stringValue = String.valueOf(value);
                    // Apply trimming if trim flag is true in ModelProperty
                    if (Utils.isTrue(modelProperty.getTrim())) {
                        stringValue = stringValue.trim();
                    }
                    return new TypeConvertResult(stringValue, true);
                case INTEGER:
                    if (value instanceof Double) {
                        return new TypeConvertResult(((Double) value).intValue(), true);
                    }

                    if (value instanceof Long) {
                        return new TypeConvertResult(((Long) value).intValue(), true);
                    }

                    return new TypeConvertResult((Integer) value);
                case LONG:

                    if (value instanceof Double) {
                        return new TypeConvertResult(
                                Long.valueOf((long) ((Double) value).doubleValue()), true);
                    }
                    if (value instanceof Integer) {
                        return new TypeConvertResult(((Integer) value).longValue(), true);
                    }

                    return new TypeConvertResult((Long) value);
                case DOUBLE:

                    if (value instanceof Long) {
                        return new TypeConvertResult(((Long) value).doubleValue(), true);
                    }
                    if (value instanceof Integer) {
                        return new TypeConvertResult(((Integer) value).doubleValue(), true);
                    }

                    return new TypeConvertResult((Double) value);
                case DATE:

                    if (value instanceof String) {
                        if (Utils.isNotEmpty(formField.getFormat())) {
                            return new TypeConvertResult(value);
                        } else {
                            SimpleDateFormat formatter =
                                    new SimpleDateFormat(Constants.dateFormatNative);
                            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                            try {
                                return new TypeConvertResult(formatter.parse((String) value), true);
                            } catch (Exception e) {
                                logger.error(
                                        "Unable to parse date: " + value + ", " + e.getMessage(),
                                        e);
                                return null;
                            }
                        }
                    }
                    return new TypeConvertResult((Date) value);
                case BOOLEAN:

                    return new TypeConvertResult((Boolean) value);
                default:
                    return new TypeConvertResult(value);
            }
        }
        return new TypeConvertResult(value);
    }

    private <T extends BaseEntityModel> ValidateResult validateToSaveFormField(FormField field,
            Document obj, String rootDocId, CrmModel crmModel, Document rootDoc, String parentPath,
            CrmModel rootCrmModel, Map<String, Object> errors, boolean isCommonApi) {
        String error = null;
        String key = field.getId();
        Object value = BsonDocumentUtils.getDataValue(obj, key);

        /*
         * Checking field is available to show
         */
        Boolean show = true;
        if (Utils.isNotEmpty(field.getShowCondition())) {
            show = Utils.getScriptBooleanValue(field.getShowCondition(), obj, rootDoc, parentPath);
        }
        if (!Utils.isTrue(show)) {
            return new ValidateResult(false);
        }
        if (Utils.isNotEmpty(field.getDisableCondition())) {
            show = Utils.getScriptBooleanValue(field.getDisableCondition(), obj, rootDoc,
                    parentPath);
        }
        if (!Utils.isTrue(show)) {
            return new ValidateResult(false);
        }

        if (Utils.isNotEmpty(field.getValidate())) {
            String validateError =
                    Utils.getScriptStringValue(field.getValidate(), obj, rootDoc, parentPath);
            if (Utils.isNotEmpty(validateError)) {
                return new ValidateResult(validateError);
            }
        }

        /*
         * Value should present if not optional
         */
        if (!Utils.isTrue(field.getO())) {
            if (!Utils.isNotEmpty(value)) {
                return new ValidateResult(field.getTitle() + " is required");
            }
        }

        /*
         * Value is valid
         */
        if (value != null) {
            ModelProperty modelProperty = getModelProperty(crmModel, key, isCommonApi);

            // checking whether value is of correct data type
            if (validValueTypeDataType(field, modelProperty.getDataType(), value, modelProperty)) {

                ValidateResult validateResult = validateFormTypeValue(field, value);

                if (validateResult != null) {
                    return validateResult;
                }

                if (isReferenceTypeField(field, modelProperty)) {

                    final String modelUniqueKey =
                            (String) MapUtils.getDataValue(field.getMeta(), "modelUniqueKey");
                    CrmModel referenceCrmModel =
                            crmModelService.getModel(modelProperty.getModelId(),
                                    Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi);
                    if (modelProperty.getDataType() == DataType.LIST_OF) {
                        List<String> references = (List<String>) value;
                        for (String reference : references) {
                            if (isModelDataNotExist(referenceCrmModel, reference, modelUniqueKey,
                                    rootDoc)) {
                                throw new AuthException();
                            }
                        }
                    } else {
                        if (isModelDataNotExist(referenceCrmModel, (String) value, modelUniqueKey,
                                rootDoc)) {
                            throw new AuthException();
                        }
                    }
                }

                if (modelProperty.getDataType() == DataType.LIST_OF
                        && modelProperty.getListType() == DataType.INNER_MODEL) {

                    String tempParentPath =
                            parentPath + (Utils.isNotEmpty(parentPath) ? "." : "") + key;

                    if (field.getType() == FormItem.Type.form_table
                            || field.getType() == FormItem.Type.input_table
                            || field.getType() == FormItem.Type.form_array) {

                        CrmForm crmForm = getInnerCrmForm(field, modelProperty, obj, isCommonApi);
                        if (crmForm != null) {
                            List<Map<String, Object>> innerFormDataList =
                                    (List<Map<String, Object>>) value;
                            for (int i = 0; i < innerFormDataList.size(); i++) {
                                validateToSaveInnerCrmForm(crmForm,
                                        crmModelService.getModel(crmForm.getCrmModelId(),
                                                Utils.isTrue(modelProperty.getIsOrg()) ? false
                                                        : isCommonApi),
                                        rootDocId, innerFormDataList.get(i), rootDoc,
                                        tempParentPath + "." + i, rootCrmModel, errors,
                                        isCommonApi);
                            }
                        }
                    }
                } else if (modelProperty.getDataType() == DataType.INNER_MODEL
                        && field.getType() == FormItem.Type.inner_form) {

                    String tempParentPath =
                            parentPath + (Utils.isNotEmpty(parentPath) ? "." : "") + key;

                    CrmForm crmForm = getInnerCrmForm(field, modelProperty, obj, isCommonApi);
                    if (crmForm != null) {
                        validateToSaveInnerCrmForm(crmForm,
                                crmModelService.getModel(crmForm.getCrmModelId(),
                                        Utils.isTrue(modelProperty.getIsOrg()) ? false
                                                : isCommonApi),
                                rootDocId, new Document((Map<String, Object>) value), rootDoc,
                                tempParentPath, rootCrmModel, errors, isCommonApi);
                    }
                }

                if (field.getType() == FormItem.Type.tag
                        && Utils.isNotEmpty(modelProperty.getData().getTagCategory())
                        && Utils.isTrue(modelProperty.getData().getAllowAddTags())) {

                    if (modelProperty.getDataType() == DataType.STRING) {
                        createAccountTag((String) value,
                                (String) modelProperty.getData().getTagCategory());
                    } else if (modelProperty.getDataType() == DataType.LIST_OF
                            && modelProperty.getListType() == DataType.STRING) {
                        ((List<String>) value).forEach(v -> {
                            createAccountTag(v, (String) modelProperty.getData().getTagCategory());
                        });
                    }
                }

                if ((field.getType() == FormItem.Type.list
                        || field.getType() == FormItem.Type.checkbox_group
                        || field.getType() == FormItem.Type.radio)
                        && (modelProperty.getData() != null
                                && Utils.isNotEmpty(modelProperty.getData().getOptions())
                                && (modelProperty.getDataType() == DataType.STRING || (modelProperty
                                        .getDataType() == DataType.LIST_OF
                                        && modelProperty.getListType() == DataType.STRING)))) {

                    List<Option> options = modelProperty.getData().getOptions();
                    // check value is present in options
                    if (!options.stream().anyMatch(o -> o.getKey().equals(value))) {
                        return new ValidateResult(field.getTitle() + " is not valid");
                    }
                }

                if (Utils.isTrue(modelProperty.getUnique())) {
                    String fieldKey = parentPath + (Utils.isNotEmpty(parentPath) ? "." : "") + key;
                    DaoQuery daoQuery = DaoQuery.builder()
                            .criteriaList(new ArrayList<>(
                                    Collections.singletonList(Criteria.where(fieldKey).is(value))))
                            .fields(Collections.singletonList("_id")).build();
                    // appendBaseFilters(rootCrmModel, daoQuery, rootDoc);
                    List<Document> results = crmAccountOrmDao.findByQuery(daoQuery, rootCrmModel);
                    if (Utils.isNotEmpty(results)) {
                        if (Utils.isNotEmpty(rootDocId)) {
                            if (!rootDocId.equals(results.get(0).getString("_id"))) {
                                error = field.getTitle() + " is not unique";
                            }
                        } else {
                            error = field.getTitle() + " is not unique";
                        }
                    }
                }
            } else {
                error = field.getTitle() + " is not valid";
            }
        }
        return new ValidateResult(error);
    }

    /**
     * Create the Tags based on the category.
     *
     * @param tag
     * @param category
     */
    public void createAccountTag(String tag, String category) {
        List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
        criteriaDefinitions.add(Criteria.where("category").is(category));
        Update update = new Update();
        update.addToSet("name", tag);
        metaOrmDao.upsert(DaoQuery.builder().criteriaList(criteriaDefinitions).build(), update,
                AccountTag.class);
    }

    private ModelProperty getModelProperty(CrmModel crmModel, String key, boolean isCommonApi) {
        ModelProperty modelProperty = optModelProperty(crmModel, key, isCommonApi);
        if (modelProperty == null)
            throw new RuntimeException(
                    "Form Model property not found key: " + key + ", model: " + crmModel.getName());
        return modelProperty;
    }

    private ModelProperty optModelProperty(CrmModel crmModel, String key, boolean isCommonApi) {
        String suffix = null;
        if (key.contains(".")) {
            int dotIndex = key.indexOf(".");
            suffix = key.substring(dotIndex + 1);
            key = key.substring(0, dotIndex);
        }
        for (ModelProperty modelProperty : crmModel.getProperties()) {
            if (key.equals(modelProperty.getKey())) {
                if (suffix != null) {
                    if (modelProperty.getDataType() == DataType.INNER_MODEL
                            || (modelProperty.getDataType() == DataType.LIST_OF
                                    && modelProperty.getListType() == DataType.INNER_MODEL)) {
                        return optModelProperty(crmModelService.getModel(modelProperty.getModelId(),
                                Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi),
                                suffix, isCommonApi);
                    } else {
                        throw new RuntimeException("Invalid inner model key : " + key + " model: "
                                + crmModel.getName());
                    }
                } else {
                    return modelProperty;
                }
            }
        }
        return null;
    }

    private FormField getFormField(CrmForm crmForm, String key) {
        FormField formField = optFormField(crmForm, key);
        if (formField == null)
            throw new RuntimeException("Form field not found");
        return formField;
    }

    private FormField optFormField(CrmForm crmForm, String key) {
        for (FormGroup formGroup : crmForm.getRows()) {
            for (FormField formField : formGroup.getFields()) {
                if (key.equals(formField.getId())) {
                    return formField;
                }
            }
        }
        return null;
    }

    private void syncFormField(FormField formField, ModelProperty modelProperty,
            Document currentDoc, Document mainDoc, String parentPath, boolean isCommonApi) {

        Object value = null;
        if (currentDoc != null) {
            value = BsonDocumentUtils.getDataValue(currentDoc, formField.getId());
        }

        if (Utils.isNotEmpty(formField.getUpdateType()) && mainDoc != null
                && Utils.isNotEmpty(mainDoc.getString("_id"))) {

            if (formField.getUpdateType() == FormItem.Visibility.disabled
                    || formField.getUpdateType() == FormItem.Visibility.disabled_fe) {
                formField.setDisableCondition("return true;");
            } else if (formField.getUpdateType() == FormItem.Visibility.hidden
                    || formField.getUpdateType() == FormItem.Visibility.hidden_fe) {
                formField.setShowCondition("return false;");
                if (formField.getUpdateType() == FormItem.Visibility.hidden) {
                    value = null;
                }
            }
        }

        if (Utils.isNotEmpty(formField.getCreateType())
                && (mainDoc == null || !Utils.isNotEmpty(mainDoc.getString("_id")))) {

            if (formField.getCreateType() == FormItem.Visibility.disabled
                    || formField.getCreateType() == FormItem.Visibility.disabled_fe) {
                formField.setDisableCondition("return true;");
            } else if (formField.getCreateType() == FormItem.Visibility.hidden
                    || formField.getCreateType() == FormItem.Visibility.hidden_fe) {
                formField.setShowCondition("return false;");
            }
        }

        if (isReferenceTypeField(formField, modelProperty) || formField.getApiUrl() != null) {
            CrmModel crmModel = null;
            final String modelUniqueKey =
                    (String) MapUtils.getDataValue(formField.getMeta(), "modelUniqueKey");
            String absentValue = (String) MapUtils.getDataValue(formField.getMeta(), "absentValue");
            List<String> additionalKeys =
                    MapUtils.getListValue(formField.getMeta(), "searchAdditionalKeys");
            if (modelProperty.getModelId() != null) {
                crmModel = crmModelService.getModel(modelProperty.getModelId(),
                        Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi);
            }
            if (!Utils.isNotEmpty(formField.getApiUrl()) && crmModel != null) {
                if (isReferenceTypeModel(modelProperty)) {
                    List<String> apiUrlParams = new ArrayList<>();
                    if (Utils.isNotEmpty(modelUniqueKey)) {
                        apiUrlParams.add("modelUniqueKey=" + modelUniqueKey);
                    }
                    if (Utils.isNotEmpty(absentValue)) {
                        apiUrlParams.add("absentValue=" + absentValue);
                    }
                    if (Utils.isNotEmpty(additionalKeys)) {
                        apiUrlParams
                                .add("searchAdditionalKeys=" + String.join(",", additionalKeys));
                    }
                    // TODO this is temp queryParams,
                    // need to add in first word before first space
                    String queryString =
                            (String) MapUtils.getDataValue(formField.getMeta(), "queryParams");
                    if (Utils.isNotEmpty(queryString)) {
                        if (queryString.startsWith("?")) {
                            queryString = queryString.substring(1);
                        }
                        apiUrlParams.add(queryString);
                    }
                    formField.setApiUrl(Api.crmModelSearchBaseApi + "/" + crmModel.getId() + "?"
                            + String.join("&", apiUrlParams));
                } else {
                    throw new RuntimeException("Api url not found for options field");
                }
            }

            if (value != null && crmModel != null) {

                /*
                 * Setting target data for _id or modelUniqueKey
                 *
                 */

                String queryKey = "_id";
                if (Utils.isNotEmpty(modelUniqueKey)) {
                    queryKey = modelUniqueKey;
                }

                List<CriteriaDefinition> criteriaList = new ArrayList<>();
                if (modelProperty.getDataType() == DataType.LIST_OF) {
                    formField.setMultiple(true);
                    criteriaList.add(Criteria.where(queryKey)
                            .in(((List<String>) value).stream().filter(Utils::isNotEmpty)
                                    .map(v -> "_id".equals(modelUniqueKey) ? new ObjectId(v) : v)
                                    .collect(Collectors.toList())));
                } else {
                    if (Utils.isNotEmptyOrNull((String) value)) {
                        criteriaList.add(Criteria.where(queryKey)
                                .is("_id".equals(modelUniqueKey) ? new ObjectId((String) value)
                                        : (String) value));
                    }
                }

                if (!criteriaList.isEmpty()) {

                    if (CrmModels.RADYFY_PAGE_MODEL.equals(crmModel.getId())) {
                        Document account = currentUserSession.getUserSession().getFilterDocuments()
                                .get(Constants.ACCOUNT_ID);
                        if (account != null
                                && !account.getString("_id").equals(Constants.RADYFY_ACCOUNT_ID)) {
                            criteriaList.add(Criteria.where("accountId")
                                    .in(Constants.RADYFY_ACCOUNT_ID, account.getString("_id")));
                        }
                    }

                    // appendBaseFilters(crmModel, criteriaList, mainDoc);

                    List<Option> searchResult = search(crmModel, null, criteriaList, false,
                            additionalKeys, 100, modelUniqueKey, isCommonApi);
                    if (formField.getOptions() == null) {
                        formField.setOptions(new ArrayList<>());
                    }
                    if (Utils.isNotEmpty(searchResult)) {
                        formField.getOptions().addAll(searchResult);
                    }
                }
            }
        } else if (isOptionsField(formField)) {
            if (Utils.isNotEmpty(modelProperty.getData())) {
                if (Utils.isNotEmpty(modelProperty.getData().getOptions())) {
                    List<Option> options = modelProperty.getData().getOptions();
                    if (formField.getType() == FormItem.Type.checkbox_group) {
                        CheckboxGroup[] result = new CheckboxGroup[1];
                        CheckboxGroup group = new CheckboxGroup();
                        group.setName("All");
                        group.setOptions(options);
                        result[0] = group;
                        formField.setCheckboxGroups(result);
                    } else {
                        formField.setOptions(options);
                    }
                }
            }
        }

        if (modelProperty.getDataType() == DataType.LIST_OF
                && modelProperty.getListType() == DataType.INNER_MODEL) {

            CrmForm crmForm = null;
            CrmTable crmTable = null;
            if (formField.getType() == FormItem.Type.form_table
                    || formField.getType() == FormItem.Type.input_table) {

                crmForm = getInnerCrmForm(formField, modelProperty, currentDoc, isCommonApi);
                crmTable = crmTableService.getByModelId(modelProperty.getModelId(),
                        Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi);
                if (crmTable == null) {
                    crmTable = new CrmTable();
                    List<TableColumn> columns = new ArrayList<>();
                    for (FormGroup innerFormGroup : crmForm.getRows()) {
                        for (FormField innerFormField : innerFormGroup.getFields()) {
                            columns.add(TableColumn.builder().show(true).type(Column.Type.text)
                                    .name(innerFormField.getTitle()).key(innerFormField.getId())
                                    .sort(false).build());
                        }
                    }
                    crmTable.setColumns(columns);
                }
            } else if (formField.getType() == FormItem.Type.form_array) {
                crmForm = getInnerCrmForm(formField, modelProperty, currentDoc, isCommonApi);
            }
            if (crmForm != null) {
                CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(),
                        Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi);
                String tempParentPath =
                        parentPath + (Utils.isNotEmpty(parentPath) ? "." : "") + formField.getId();
                if (value != null && value instanceof List) {
                    int index = 0;
                    List<Map<String, Object>> listValue = (List<Map<String, Object>>) value;
                    if (Utils.isNotEmpty(listValue)) {
                        for (Map<String, Object> data : listValue) {
                            syncFieldFormRow(formField, crmForm, crmModel, new Document(data),
                                    mainDoc, tempParentPath + "." + index++, isCommonApi);
                        }
                    } else {
                        syncFieldFormRow(formField, crmForm, crmModel, new Document(), mainDoc,
                                tempParentPath + ".0", isCommonApi);
                    }
                } else {
                    syncFieldFormRow(formField, crmForm, crmModel, new Document(), mainDoc,
                            tempParentPath + ".0", isCommonApi);
                }
                // formField.setFormRows(crmForm.getRows());
            }
            if (crmTable != null) {
                formField.setColumns(crmTable.getColumns());
            }
        } else if (modelProperty.getDataType() == DataType.INNER_MODEL
                && formField.getType() == FormItem.Type.inner_form) {

            CrmForm crmForm = getInnerCrmForm(formField, modelProperty, currentDoc, isCommonApi);
            if (crmForm != null) {
                CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(),
                        Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi);
                syncFieldFormRow(formField, crmForm, crmModel, (Document) value, mainDoc,
                        parentPath + (Utils.isNotEmpty(parentPath) ? "." : "") + formField.getId(),
                        isCommonApi);
            }
        }

        if (formField.getType() == FormItem.Type.tag) {
            if (Utils.isNotEmpty(modelProperty.getData().getTagCategory())) {
                List<Option> searchResult =
                        getAccountTags((String) modelProperty.getData().getTagCategory());
                if (formField.getOptions() == null) {
                    formField.setOptions(new ArrayList<>());
                }
                if (Utils.isNotEmpty(searchResult)) {
                    formField.getOptions().add(searchResult.get(0));
                }
            }
        }

        // setting value to form field if value is not null and root path is empty
        // parentPath is empty means it is root form field
        if (value != null && "".equals(parentPath)) {
            formField.setValue(value);
        }
    }

    private void syncFieldFormRow(FormField formField, CrmForm crmForm, CrmModel crmModel,
            Document value, Document mainDoc, String parentPath, boolean isCommonApi) {
        if (Utils.isNotEmpty(formField.getFormRows())) {
            syncValuesCrmForm(formField.getFormRows(), crmModel, value, mainDoc, parentPath,
                    isCommonApi);
        } else {
            syncValuesCrmForm(crmForm.getRows(), crmModel, value, mainDoc, parentPath, isCommonApi);
            formField.setFormRows(crmForm.getRows());
        }
    }

    private CrmForm getInnerCrmForm(FormField formField, ModelProperty modelProperty,
            Document currentData, boolean isCommonApi) {
        CrmForm crmForm = null;
        if (Utils.isNotEmpty(formField.getMeta())) {
            String crmFormId = (String) MapUtils.getDataValue(formField.getMeta(), "crmFormId");
            if (ValidationUtils.isValidHexID(crmFormId)) {
                crmForm = crmFormService.getById(crmFormId, isCommonApi);
            } else {
                String crmFormIdField =
                        (String) MapUtils.getDataValue(formField.getMeta(), "crmFormIdField");
                if (Utils.isNotEmpty(crmFormIdField)) {
                    crmFormId =
                            (String) BsonDocumentUtils.getDataValue(currentData, crmFormIdField);
                    if (Utils.isNotEmpty(crmFormId)) {
                        crmForm = crmFormService.getById(crmFormId, isCommonApi);
                    }
                }
            }
        }
        if (crmForm == null && Utils.isNotEmpty(modelProperty.getModelId())) {
            crmForm = crmFormService.getByModelId(modelProperty.getModelId(),
                    Utils.isTrue(modelProperty.getIsOrg()) ? false : isCommonApi);
        }
        return crmForm;
    }

    private <T> Criteria buildOrCriteria(String query, String key) {

        List<CriteriaDefinition> orCriteria = new ArrayList<>();
        orCriteria.add(Criteria.where(key).regex(".*" + query + ".*", "si"));
        // for (String q : query.split(" ")) {
        // String regex = ".*" + q + ".*";
        // orCriteria.add(Criteria.where(key)
        // .regex(regex, "si"));
        // }
        return new Criteria().orOperator(orCriteria.toArray(new Criteria[0]));
    }

    private List<String> getColumnsFe(TableRequest tableRequest) {
        List<String> columns = null;
        if (tableRequest != null && Utils.isNotEmpty(tableRequest.getMeta())) {
            Object cols = MapUtils.getDataValue(tableRequest.getMeta(), "columns");
            if (cols != null) {
                columns = (List<String>) cols;
            }
        }
        return columns;
    }

    private void setShowTableColumnFe(List<String> feColumns, TableColumn tableColumn) {
        if (Utils.isNotEmpty(feColumns)) {
            if (!feColumns.contains(tableColumn.getKey())) {
                tableColumn.setShow(false);
            }
        }
    }

    // private void appendBaseFilters(CrmModel crmModel, DaoQuery daoQuery, Document filters) {
    // GridRequestParams gridFilters = new GridRequestParams();
    // if (filters.containsKey(Constants.ACCOUNT_ID)) {
    // gridFilters.put(Constants.ACCOUNT_ID, filters.getString(Constants.ACCOUNT_ID));
    // }
    // if (filters.containsKey(Constants.ECOM_ACCOUNT_ID)) {
    // gridFilters.put(Constants.ECOM_ACCOUNT_ID,
    // filters.getString(Constants.ECOM_ACCOUNT_ID));
    // }
    // appendBaseFilters(crmModel, daoQuery, gridFilters);
    // }

    // private void appendBaseFilters(CrmModel crmModel, List<CriteriaDefinition> criterias,
    // Document filters) {
    // appendBaseFilters(crmModel, DaoQuery.builder().criteriaList(criterias).build(), filters);
    // }

    // private void appendBaseFilters(CrmModel crmModel, DaoQuery daoQuery,
    // GridRequestParams filters) {
    // if (Utils.isNotEmpty(filters)) {
    // crmModelService.forEachBaseModels(bm -> {
    // String fieldName = bm.getFieldName();
    // if (fieldName.equals(Constants.ACCOUNT_ID)
    // && filters.containsKey(Constants.ACCOUNT_ID)) {
    // daoQuery.addCriteria(Criteria.where(Constants.ACCOUNT_ID)
    // .is(filters.get(Constants.ACCOUNT_ID)));
    // }
    // if (fieldName.equals(Constants.ECOM_ACCOUNT_ID)
    // && filters.containsKey(Constants.ECOM_ACCOUNT_ID)) {
    // daoQuery.addCriteria(Criteria.where(Constants.ECOM_ACCOUNT_ID)
    // .is(filters.get(Constants.ECOM_ACCOUNT_ID)));
    // }
    // }, crmModel);
    // }
    // }

    private <T extends BaseEntityModel> void resolveColumnsData(CrmTable crmTable,
            CrmModel crmModel, GridRequestParams filters, boolean isCommonApi) {

        if (crmTable.getData() == null || crmTable.getColumns() == null) {
            return;
        }
        for (TableColumn tableColumn : crmTable.getColumns()) {
            if (Utils.isNotEmpty(tableColumn.getActions())) {
                tableColumn.getActions()
                        .forEach(a -> updateButtonSlug(a, filters, crmTable.getGridParams()));
            }
            if (tableColumn.getType() == Column.Type.card) {
                ModelProperty modelProperty =
                        optModelProperty(crmModel, tableColumn.getKey(), isCommonApi);
                if (modelProperty != null && isReferenceTypeModel(modelProperty)) {
                    CrmModel referenceCrmModel =
                            crmModelService.getModel(modelProperty.getModelId(), isCommonApi);
                    if (referenceCrmModel.getCardDataTargets() != null) {
                        CardData cardTargets = referenceCrmModel.getCardDataTargets();
                        DaoQuery daoQuery =
                                DaoQuery.builder().fields(cardTargets.getFetchableFields()).build();
                        // appendBaseFilters(referenceCrmModel, daoQuery, filters);
                        String cardDataPath = "$card_data." + tableColumn.getKey();
                        tableColumn.setHandleKey(cardDataPath);
                        for (Document rowData : crmTable.getData()) {
                            Object value =
                                    BsonDocumentUtils.getDataValue(rowData, tableColumn.getKey());
                            if (value != null) {
                                List<CardData> cardData = new ArrayList<>();

                                if (modelProperty.getDataType() == DataType.LIST_OF) {
                                    List<String> references = (List<String>) value;
                                    for (String reference : references) {
                                        if (CrmModels.RADYFY_ROLE_MODEL
                                                .equals(referenceCrmModel.getId())) {

                                            if (Arrays.asList(RoleType.SUPER_ADMIN,
                                                    RoleType.SYSTEM_ADMIN, RoleType.ACCOUNT_ADMIN)
                                                    .contains(reference)) {
                                                CardData c = new CardData();
                                                c.setTitle(reference);
                                                cardData.add(c);
                                            }
                                         else if (ValidationUtils.isValidHexID(reference)) {
                                            cardData.add(cardTargets
                                                    .generate(crmAccountOrmDao.getById(reference,
                                                            referenceCrmModel, daoQuery)));
                                        } 
                                    }else if (ValidationUtils.isValidHexID(reference)) {
                                            cardData.add(cardTargets
                                                    .generate(crmAccountOrmDao.getById(reference,
                                                            referenceCrmModel, daoQuery)));
                                        }
                                    }
                                } else {
                                    if (CrmModels.RADYFY_ROLE_MODEL
                                            .equals(referenceCrmModel.getId())) {

                                        if (Arrays.asList(RoleType.SUPER_ADMIN,
                                                RoleType.SYSTEM_ADMIN, RoleType.ACCOUNT_ADMIN)
                                                .contains(value)) {
                                            CardData c = new CardData();
                                            c.setTitle(value.toString());
                                            cardData.add(c);
                                        } else if (ValidationUtils.isValidHexID((String) value)) {
                                            cardData.add(cardTargets.generate(
                                                    crmAccountOrmDao.getById((String) value,
                                                            referenceCrmModel, daoQuery)));
                                        }

                                    } else if (ValidationUtils.isValidHexID((String) value)) {
                                        cardData.add(cardTargets.generate(crmAccountOrmDao.getById(
                                                (String) value, referenceCrmModel, daoQuery)));
                                    }
                                }

                                BsonDocumentUtils.setDataValue(rowData, cardDataPath, cardData);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateButtonSlug(Button button, GridRequestParams params,
            List<GridParam> requiredParams) {
        if (button != null) {
            if (Utils.isNotEmpty(button.getSlug())) {
                button.setSlug(getApiSlug(button.getSlug(), params, requiredParams));
            }
            if (Utils.isNotEmpty(button.getApiUrl())) {
                button.setApiUrl(getApiSlug(button.getApiUrl(), params, requiredParams));
            }
        }
    }

    private String getApiSlug(String api, GridRequestParams params,
            List<GridParam> requiredParams) {
        String[] slugData = api.split(" ");
        StringBuilder slugBuilder = new StringBuilder();
        for (String d : slugData) {
            if (slugBuilder.length() == 0) {
                slugBuilder.append(params.queryString(d, requiredParams));
            } else {
                slugBuilder.append(" ").append(d);
            }
        }
        return slugBuilder.toString();
    }

    public <T extends BaseEntityModel> List<Option> formValues(GridRequestParams gridRequestParams,
            CrmForm crmForm, boolean isCommonApi) {
        CrmForm updatedForm = crmForm(crmForm, gridRequestParams, isCommonApi);
        FormGroup[] groups = updatedForm.getRows();
        List<Option> values = new ArrayList<>();
        for (FormGroup group : groups) {
            if (Utils.isNotEmpty(group.getFields())) {
                group.getFields().forEach(f -> {
                    if (!f.getId().equals("id")) {
                        if (Utils.isNotEmpty(f.getOptions())) {
                            values.add(new Option(f.getId(),
                                    Utils.isTrue(f.getMultiple()) ? f.getOptions()
                                            : f.getOptions().get(0)));
                        } else {
                            values.add(new Option(f.getId(), f.getValue()));
                        }
                    }
                });
            }
        }
        return values;
    }

    @Transactional
    public void deleteEntity(GridRequestParams requestParams, CrmApi crmApi) {
        deleteEntity(requestParams, crmApi.getModelId(), crmApi.getGridParams());
    }

    @Transactional
    public void deleteEntity(EntityActionMeta deleteActionMeta) {
        deleteEntity(deleteActionMeta.getParams(), deleteActionMeta.getEntityId(), null);
    }

    private void deleteEntity(GridRequestParams requestParams, String modelId,
            List<GridParam> gridParam) {

        CrmModel crmModel = crmModelService.getModel(modelId, false);

        if (!requestParams.containsKey("id")) {
            throw new RuntimeException("id is required");
        }

        crmModelService.runEventListener(CrmModelConfig.Event.BEFORE_DELETE, crmModel,
                requestParams, null);

        String entityId = requestParams.get("id");

        StringBuilder dependentModelNames = new StringBuilder();

        List<ParentModel> parentModels = getParentModels(crmModel, null);

        for (ParentModel parentModel : parentModels) {
            long count = crmAccountOrmDao.getCount(DaoQuery.builder()
                    .criteriaList(Collections
                            .singletonList(Criteria.where(parentModel.key()).is(entityId)))
                    .build(), parentModel.value());
            if (count > 0) {
                /*
                 * Dependent data found
                 */
                dependentModelNames.append(parentModel.value().getName()).append(", ");
            }
        }

        if (dependentModelNames.length() > 0) {
            throw new RuntimeException(
                    "Data found in " + dependentModelNames + " which are dependent of this.");
        }

        /*
         * Deleting data data found
         */
        DaoQuery daoQuery = DaoQuery.builder()
                .criteriaList(requestParams.getGridFiltersCriteriaWithId(gridParam)).build();
        crmAccountOrmDao.delete(daoQuery, crmModel);

        crmModelService.runEventListener(CrmModelConfig.Event.AFTER_DELETE, crmModel, requestParams,
                null);

    }

    private List<ParentModel> getParentModels(CrmModel crmModel, String childKey) {

        List<ParentModel> parentModels = new ArrayList<>();

        List<CrmModel> models = metaOrmDao.findByQuery(
                DaoQuery.builder()
                        .criteriaList(Collections.singletonList(
                                Criteria.where("properties.modelId").is(crmModel.getId())))
                        .build(),
                CrmModel.class);

        DataType checkDataType =
                Utils.isNotEmpty(childKey) ? DataType.INNER_MODEL : DataType.REFERENCE;

        for (CrmModel model : models) {
            for (ModelProperty modelProperty : model.getProperties()) {
                if (modelProperty.getDataType() == checkDataType
                        && modelProperty.getModelId().equals(crmModel.getId())) {
                    String propertyKey =
                            Utils.isNotEmpty(childKey) ? modelProperty.getKey() + "." + childKey
                                    : modelProperty.getKey();
                    if (model.getModelType() == CrmModelType.INNER) {
                        parentModels.addAll(getParentModels(model, propertyKey));
                    } else {
                        parentModels.add(new ParentModel(propertyKey, model));
                    }
                }
            }
        }
        return parentModels;

    }

    // private void setReferenceCardData(
    // String reference,
    // Document rowData,
    // CardData cardTargets,
    // CrmModel referenceCrmModel,
    // DaoQuery daoQuery,
    // String cardDataPath) {
    // try {
    // if (reference != null) {
    // Document refData = crmAccountOrmDao.getById(reference, referenceCrmModel,
    // daoQuery);
    // if (refData != null) {
    // BsonDocumentUtils.setDataValue(
    // rowData,
    // cardDataPath,
    // Collections.singletonList(cardTargets.generate(refData)));
    // }
    // }
    // } catch (Exception e) {
    // logger.error(e.getMessage(), e);
    // }
    // }

    public CheckboxGroup[] buildExportFields(CrmForm crmForm, boolean isCommonApi) {
        Map<Integer, CheckboxGroup> groupMap = new HashMap<>();

        forEachCrmFormField(crmForm, (field, modelProperty, prefix, topContextGroup) -> {
            // Get or create checkbox group for this group index
            CheckboxGroup checkboxGroup = groupMap.computeIfAbsent(topContextGroup, k -> {
                FormGroup formGroup = crmForm.getRows()[k];
                CheckboxGroup newGroup = new CheckboxGroup();
                newGroup.setName(
                        Utils.isNotEmpty(formGroup.getLabel()) ? formGroup.getLabel() : "Fields");
                newGroup.setOptions(new ArrayList<>());
                return newGroup;
            });

            // Add field to group
            String fieldKey = prefix + (Utils.isNotEmpty(prefix) ? "." : "") + field.getId();
            checkboxGroup.getOptions().add(new Option(fieldKey, field.getTitle()));
        }, isCommonApi);

        // Convert map to array and filter empty groups
        return groupMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue).filter(group -> !group.getOptions().isEmpty())
                .toArray(CheckboxGroup[]::new);
    }

    private void forEachCrmFormField(CrmForm crmForm, CrmFormFieldConsumer consumer,
            boolean isCommonApi) {
        forEachCrmFormField(crmForm, consumer, "", null, isCommonApi);
    }

    private void forEachCrmFormField(CrmForm crmForm, CrmFormFieldConsumer consumer, String prefix,
            Integer topContextGroup, boolean isCommonApi) {
        CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);

        for (int i = 0; i < crmForm.getRows().length; i++) {
            FormGroup formGroup = crmForm.getRows()[i];
            int fcg = i;
            if (topContextGroup != null) {
                fcg = topContextGroup;
            }
            for (FormField field : formGroup.getFields()) {
                if (!field.getId().equals("id")) {
                    ModelProperty modelProperty =
                            getModelProperty(crmModel, field.getId(), isCommonApi);

                    if (modelProperty != null) {
                        boolean isInnerForm = modelProperty.getDataType() == DataType.INNER_MODEL
                                && field.getType() == FormItem.Type.inner_form;
                        boolean isListOfInnerForm = modelProperty.getDataType() == DataType.LIST_OF
                                && modelProperty.getListType() == DataType.INNER_MODEL
                                && (field.getType() == FormItem.Type.form_table
                                        || field.getType() == FormItem.Type.input_table
                                        || field.getType() == FormItem.Type.form_array);
                        if (isInnerForm || isListOfInnerForm) {

                            // Handle inner form
                            CrmForm innerCrmForm =
                                    getInnerCrmForm(field, modelProperty, null, isCommonApi);
                            if (innerCrmForm != null) {
                                String innerPrefix = prefix + (Utils.isNotEmpty(prefix) ? "." : "")
                                        + field.getId();
                                forEachCrmFormField(innerCrmForm, consumer, innerPrefix, fcg,
                                        isCommonApi);
                            }

                        } else {
                            consumer.run(field, modelProperty, prefix, fcg);
                        }
                    } else {
                        throw new RuntimeException(
                                "Model property not found for field: " + field.getId());
                    }
                }
            }
        }
    }

    public ResponseEntity<Resource> exportExcel(ExportForm exportForm, CrmModel crmModel,
            List<CriteriaDefinition> criteriaDefinitions, CrmForm crmForm, Boolean getAllRecords,
            Integer limit, boolean isCommonApi) {
        TableRequest tableRequest = new TableRequest();
        if (Utils.isTrue(getAllRecords)) {
            tableRequest.setS(0);
            tableRequest.setP(-1);
        } else {
            tableRequest.setS(limit != null ? limit : 1);
            tableRequest.setP(0);
        }
        if (Utils.isNotEmpty(exportForm.getFields())) {
            tableRequest.setFields(exportForm.getFields());
        }
        tableRequest.setAdditionalCriterias(criteriaDefinitions);

        if (Utils.isNotEmpty(exportForm.getFilters())) {
            tableRequest.setFilters(exportForm.getFilters());
        }

        TableResult<Document> tableResult = crmAccountOrmDao.table(tableRequest, crmModel);

        String SHEET = "Sheet1";

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);
            CreationHelper createHelper = workbook.getCreationHelper();

            // Header
            Row headerRow = sheet.createRow(0);
            Map<String, ModelProperty> fieldMap = new LinkedHashMap<>();
            Map<String, String> fieldLabelMap = new LinkedHashMap<>();

            forEachCrmFormField(crmForm, (field, modelProperty, prefix, topContextGroup) -> {
                String fieldKey = prefix + (Utils.isNotEmpty(prefix) ? "." : "") + field.getId();
                if (exportForm.getFields().contains(fieldKey)) {
                    fieldMap.put(fieldKey, modelProperty);
                    fieldLabelMap.put(fieldKey, field.getTitle());
                }
            }, isCommonApi);
            int rowIdx = 1;
            int cellId = 0;
            for (Map.Entry<String, ModelProperty> entry : fieldMap.entrySet()) {
                Cell cell = headerRow.createCell(cellId++);
                // Use the field label from the form if available
                cell.setCellValue(
                        fieldLabelMap.getOrDefault(entry.getKey(), entry.getValue().getName()));
            }
            for (Document doc : tableResult.getData()) {
                Row row = sheet.createRow(rowIdx++);
                cellId = 0;
                for (Map.Entry<String, ModelProperty> entry : fieldMap.entrySet()) {
                    try {
                        Object value = BsonDocumentUtils.getDataValue(doc, entry.getKey());
                        Cell cell = row.createCell(cellId);

                        if (value != null) {
                            // Handle different data types
                            switch (entry.getValue().getDataType()) {
                                case INTEGER:
                                    if (value instanceof Integer) {
                                        cell.setCellValue((Integer) value);
                                    } else if (value instanceof Long) {
                                        cell.setCellValue(((Long) value).intValue());
                                    }
                                    break;
                                case DOUBLE:
                                    if (value instanceof Number) {
                                        cell.setCellValue(((Number) value).doubleValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    if (value instanceof Boolean) {
                                        cell.setCellValue((Boolean) value);
                                    }
                                    break;
                                case DATE:
                                    if (value instanceof Date) {
                                        CellStyle cellStyle = workbook.createCellStyle();
                                        cellStyle.setDataFormat(createHelper.createDataFormat()
                                                .getFormat("d/m/yyyy"));
                                        cell.setCellValue((Date) value);
                                        cell.setCellStyle(cellStyle);
                                    }
                                    break;
                                case REFERENCE:
                                    if (value instanceof String) {
                                        String referenceValue = (String) value;
                                        if (Utils.isNotEmpty(referenceValue)) {
                                            // Get the referenced model
                                            CrmModel referencedModel = crmModelService.getModel(
                                                    entry.getValue().getModelId(), isCommonApi);
                                            if (referencedModel != null) {
                                                // Search for the reference value
                                                List<CriteriaDefinition> searchCriteria =
                                                        new ArrayList<>();
                                                searchCriteria.add(Criteria.where("_id")
                                                        .is(new ObjectId(referenceValue)));
                                                List<Option> searchResult = search(referencedModel,
                                                        null, searchCriteria, true, null, null,
                                                        isCommonApi);
                                                if (Utils.isNotEmpty(searchResult)) {
                                                    String displayValue =
                                                            (String) searchResult.get(0).getValue();
                                                    cell.setCellValue(
                                                            displayValue != null ? displayValue
                                                                    : "");
                                                } else {
                                                    cell.setCellValue(referenceValue); // Fallback
                                                                                       // to ID if
                                                                                       // not found
                                                }
                                            } else {
                                                cell.setCellValue(referenceValue);
                                            }
                                        } else {
                                            cell.setCellValue("");
                                        }
                                    } else {
                                        cell.setCellValue(String.valueOf(value));
                                    }
                                    break;
                                case LIST_OF:
                                    if (value instanceof List) {
                                        @SuppressWarnings("unchecked")
                                        List<Object> listValue = (List<Object>) value;
                                        if (entry.getValue().getListType() == DataType.REFERENCE
                                                && !listValue.isEmpty()) {
                                            // Handle list of references
                                            CrmModel referencedModel = crmModelService.getModel(
                                                    entry.getValue().getModelId(), isCommonApi);
                                            if (referencedModel != null) {
                                                List<String> referenceIds =
                                                        listValue.stream().map(Object::toString)
                                                                .collect(Collectors.toList());

                                                List<CriteriaDefinition> searchCriteria =
                                                        new ArrayList<>();
                                                searchCriteria.add(Criteria.where("_id")
                                                        .in(referenceIds.stream().map(ObjectId::new)
                                                                .collect(Collectors.toList())));

                                                List<Option> searchResults = search(referencedModel,
                                                        null, searchCriteria, true, null, null,
                                                        isCommonApi);
                                                if (Utils.isNotEmpty(searchResults)) {
                                                    String displayValues = searchResults.stream()
                                                            .map(opt -> String
                                                                    .valueOf(opt.getValue()))
                                                            .collect(Collectors.joining(", "));
                                                    cell.setCellValue(displayValues);
                                                } else {
                                                    cell.setCellValue(
                                                            String.join(", ", referenceIds));
                                                }
                                            } else {
                                                cell.setCellValue(String.join(", ",
                                                        listValue.stream().map(String::valueOf)
                                                                .collect(Collectors.toList())));
                                            }
                                        } else {
                                            // Handle other list types
                                            cell.setCellValue(String.join(", ",
                                                    listValue.stream().map(String::valueOf)
                                                            .collect(Collectors.toList())));
                                        }
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                default:
                                    cell.setCellValue(String.valueOf(value));
                            }
                        } else {
                            cell.setCellValue("");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to export cell value: " + e.getMessage(), e);
                        // Create empty cell on error
                        Cell cell = row.createCell(cellId);
                        cell.setCellValue("");
                    }
                    cellId++;
                }
            }

            workbook.write(out);
            String filename = exportForm.getFileName();
            ByteArrayResource file = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);

        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }

    /**
     * Imports data from an Excel file and returns a TableResult with the imported data
     * 
     * @param fileUpload The uploaded Excel file
     * @param crmModel The CRM model to import data into
     * @param crmForm The form definition for the data
     * @param isCreate Whether to create new records or update existing ones
     * @param updateAction Optional action to perform on each record before saving
     * @return TableResult containing the imported data with validation errors if any
     */
    public CrmTable initImportFromExcel(com.radyfy.common.model.commons.FileUpload fileUpload,
            CrmModel crmModel, CrmForm crmForm, Boolean isCreate,
            GridRequestParams gridRequestParams,
            com.radyfy.common.commons.UpdateAction updateAction, boolean isCommonApi) {
        CrmTable tableResult = new CrmTable();
        tableResult.setGridType(com.radyfy.common.model.enums.grid.GridType.table);
        tableResult.setColumns(new ArrayList<>());
        tableResult.setData(new ArrayList<>());
        try {
            String fileName = fileUpload.getFile();
            byte[] fileBytes = fileStorageService.downloadS3File(fileName);
            if (fileBytes == null || fileBytes.length == 0) {
                throw new RuntimeException("File not found");
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes);
            Workbook workbook = new XSSFWorkbook(bis);

            Sheet sheet = workbook.getSheet("Sheet1");
            Iterator<Row> rows = sheet.iterator();
            Map<String, Integer> fieldIndex = new HashMap<>();

            AtomicInteger rowNumber = new AtomicInteger(0);
            boolean hasError = false;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber.get() == 0) {
                    Iterator<Cell> cellsInRow = currentRow.iterator();
                    int cellIdx = 0;
                    while (cellsInRow.hasNext()) {
                        Cell currentCell = cellsInRow.next();
                        fieldIndex.put(currentCell.getStringCellValue(), cellIdx);
                        cellIdx++;
                    }
                    rowNumber.incrementAndGet();
                    continue;
                }

                Document instance = new Document();
                forEachCrmFormField(crmForm, (field, modelProperty, prefix, contextGroup) -> {
                    Integer cellId = fieldIndex.get(field.getTitle());
                    if (cellId == null && !Boolean.TRUE.equals(field.getO())) {
                        throw new RuntimeException(field.getTitle() + " row not present");
                    }
                    if (cellId != null) {
                        String key = prefix + (Utils.isNotEmpty(prefix) ? "." : "") + field.getId();
                        if (rowNumber.get() == 1) {
                            TableColumn tableColumn = TableColumn.builder().key(key)
                                    .name(field.getTitle()).sort(false).show(true)
                                    .type(Column.Type.errors).handleKey("data").build();
                            // Set index manually since it's not in the builder
                            // try {
                            // java.lang.reflect.Field indexField =
                            // TableColumn.class.getDeclaredField("index");
                            // indexField.setAccessible(true);
                            // indexField.set(tableColumn, cellId);
                            // } catch (Exception e) {
                            // logger.error("Failed to set index on TableColumn", e);
                            // }
                            tableResult.getColumns().add(tableColumn);
                        }
                        Cell currentCell = currentRow.getCell(cellId);
                        logger.info("Import Row: " + rowNumber + " Field: " + key + " Value: "
                                + currentCell);
                        Object value = null;
                        if (currentCell != null) {
                            // Use the model property's dataType
                            switch (modelProperty.getDataType()) {
                                case STRING:
                                    value = currentCell.getStringCellValue();
                                    break;
                                case INTEGER:
                                    // check if the cell is a string
                                    if (currentCell.getCellType() == CellType.STRING) {
                                        value = currentCell.getStringCellValue();
                                        if (Utils.isNotEmpty(value)) {
                                            value = Integer.parseInt((String) value);
                                        }
                                    } else {
                                        double numValue = currentCell.getNumericCellValue();
                                        value = (int) numValue;
                                    }
                                    break;
                                case LONG:
                                    // check if the cell is a string
                                    if (currentCell.getCellType() == CellType.STRING) {
                                        value = currentCell.getStringCellValue();
                                        if (Utils.isNotEmpty(value)) {
                                            value = Long.parseLong((String) value);
                                        }
                                    } else {
                                        value = (long) currentCell.getNumericCellValue();
                                    }
                                    break;
                                case DOUBLE:
                                    // check if the cell is a string
                                    if (currentCell.getCellType() == CellType.STRING) {
                                        value = currentCell.getStringCellValue();
                                        if (Utils.isNotEmpty(value)) {
                                            value = Double.parseDouble((String) value);
                                        }
                                    } else {
                                        value = currentCell.getNumericCellValue();
                                    }
                                    break;
                                case DATE:
                                    // check if the cell is a string
                                    if (currentCell.getCellType() == CellType.STRING) {
                                        value = currentCell.getStringCellValue();
                                        if (Utils.isNotEmpty(value)) {
                                            value = Utils.parseDate(value);
                                        }
                                    } else {
                                        value = currentCell.getDateCellValue();
                                    }
                                    break;
                                case BOOLEAN:
                                    // check if the cell is a string
                                    if (currentCell.getCellType() == CellType.STRING) {
                                        value = currentCell.getStringCellValue();
                                        value = Boolean.parseBoolean((String) value);
                                    } else {
                                        value = currentCell.getBooleanCellValue();
                                    }
                                    break;
                                case REFERENCE:
                                    // Handle reference type fields
                                    value = currentCell.getStringCellValue();
                                    if (value != null && Utils.isNotEmpty((String) value)) {
                                        List<Option> searchResult = search(
                                                crmModelService.getModel(modelProperty.getModelId(),
                                                        isCommonApi),
                                                (String) value, new ArrayList<>(), true,
                                                field.getApiUrl(), new ArrayList<>(), isCommonApi);
                                        if (Utils.isNotEmpty(searchResult)) {
                                            value = searchResult.get(0).getKey();
                                        }
                                    }
                                    break;
                                default:
                                    value = currentCell.getStringCellValue();
                                    break;
                            }
                        }
                        if (value != null) {
                            BsonDocumentUtils.setDataValue(instance, key, value);
                        }
                    }
                }, isCommonApi);

                Map<String, Object> errors = new HashMap<>();
                validateToSaveCrmForm(null, crmForm, crmModel, instance, errors, null, isCommonApi);

                instance.put("meta", errors);
                if (!errors.isEmpty() && !hasError) {
                    hasError = true;
                }
                tableResult.getData().add(instance);
                rowNumber.incrementAndGet();
            }
            workbook.close();

            // Sort columns by index using a custom comparator
            // tableResult.getColumns().sort((a, b) -> {
            // try {
            // java.lang.reflect.Field indexField =
            // TableColumn.class.getDeclaredField("index");
            // indexField.setAccessible(true);
            // Integer indexA = (Integer) indexField.get(a);
            // Integer indexB = (Integer) indexField.get(b);
            // return Integer.compare(indexA != null ? indexA : 0, indexB != null ? indexB :
            // 0);
            // } catch (Exception e) {
            // logger.error("Failed to compare TableColumn indexes", e);
            // return 0;
            // }
            // });

            if (hasError) {
                return tableResult;
            } else {
                tableResult.getData().forEach(d -> {
                    if (updateAction != null) {
                        updateAction.run(d);
                    }
                    if (isCreate) {
                        // Save the document to the database
                        DocCreateResult doc = createDocumentForCrmForm(d, gridRequestParams,
                                crmForm, isCommonApi);
                        // Update the document ID in the result
                        d.put("_id", doc.getDocument().get("_id"));
                    }
                });
                return null;
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }


    private void createUser(CrmModel crmModel, Document finalDoc) {

        if (crmModel.getIsUserAccount() == null || !crmModel.getIsUserAccount()) {
            return;
        }

        String email = finalDoc.getString("email");

        if (email == null) {
            return;
        }

        if (!Regex.email.matcher(email).matches()) {
            throw new RuntimeException(Errors.INVALID_EMAIL);
        }

        List<BaseCrmModel> baseModels = crmModelService.getBaseModels();
        CrmModel userModel = crmModelService.getModelByCollectionName(CollectionNames.user);

        validateUserAccessScope(baseModels, finalDoc, "_access");

        DaoQuery userQuery = DaoQuery.fromCriteria(Criteria.where("email").is(email));
        Document userDoc = entityOrmDao.findOneByQuery(userQuery, userModel);

        if (userDoc != null) {
            finalDoc.put("userId", userDoc.getString("_id"));
            updateUserOrgScope(finalDoc, userModel, userQuery);
            return;
        }

        Document user = new Document();
        user.put("email", email);
        user.put("userName", email);
        user.put("status", UserStatus.INVITED.toString());
        user.put("isFirstLogin", true);
        user.put("appRoleId", RoleType.ACCOUNT_ADMIN);

        if (finalDoc.getString("firstName") != null) {
            user.put("firstName", finalDoc.getString("firstName"));
        }

        if (finalDoc.getString("lastName") != null) {
            user.put("lastName", finalDoc.getString("lastName"));
        }

        if (finalDoc.getString("middleName") != null) {
            user.put("middleName", finalDoc.getString("middleName"));
        }

        if (finalDoc.getString("password") != null) {
            user.put("password", passwordHash.hashPassword(finalDoc.getString("password")));
        }


        for (BaseCrmModel baseModel : baseModels) {
            String fieldName = baseModel.getFieldName();
            user.put(fieldName, finalDoc.get("_access" + fieldName));

            // remove the _access field from the finalDoc
            finalDoc.remove("_access" + fieldName);
        }

        CrmForm userForm = crmFormService.getById(CrmForms.RADYFY_USER_FORM, true);

        DocCreateResult docCreateResult =
                createDocumentForCrmForm(user, new GridRequestParams(), userForm, userModel, true);
        Document newUserDoc = docCreateResult.getDocument();

        finalDoc.put("userId", newUserDoc.getString("_id"));

    }


    private void updateUserOrgScope(Document finalDoc, CrmModel userModel, DaoQuery userQuery) {

        List<BaseCrmModel> baseModels = crmModelService.getBaseModels();

        Update update = new Update();

        for (BaseCrmModel baseModel : baseModels) {

            String fieldName = baseModel.getFieldName();

            update.set(fieldName, finalDoc.get("_access" + fieldName));

            // remove the _access field from the finalDoc
            finalDoc.remove("_access" + fieldName);

        }

        entityOrmDao.updateByQuery(userQuery, update, userModel);
    }

    public void validateUserAccessScope(List<BaseCrmModel> baseModels, Document finalDoc,
            String scopeType) {

        Document userDocument = currentUserSession.getUser().getDocument();


        for (BaseCrmModel baseModel : baseModels) {

            List<String> currentUserAccess =
                    userDocument.getList(baseModel.getFieldName(), String.class);
            List<String> entityUserAccess =
                    finalDoc.getList(scopeType + baseModel.getFieldName(), String.class);

            if (currentUserAccess == null || currentUserAccess.contains("All")) {
                continue;
            }

            if (entityUserAccess == null || entityUserAccess.contains("All")) {
                throw new RuntimeException(
                        "This user have higher access then you, please contact the admin.");
            }

            for (String access : entityUserAccess) {
                if (!currentUserAccess.contains(access)) {
                    throw new RuntimeException(
                            "This user have higher access then you, please contact the admin.");
                }
            }

        }
    }
}

