package com.radyfy.common.service.crm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radyfy.common.commons.Api;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.commons.ExportForm;
import com.radyfy.common.model.crm.CrmIframe;
import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.crm.api.CrmApi;
import com.radyfy.common.model.crm.api.CrmApi.CrmApiAction;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.CrmGridAny;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.menu.CrmMenu;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.dynamic.DocCreateResult;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.enums.grid.GridType;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.response.CheckboxGroup;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.grid.CrmFormService;
import com.radyfy.common.service.crm.grid.CrmGridService;
import com.radyfy.common.service.crm.grid.CrmMenuService;
import com.radyfy.common.service.crm.grid.CrmTableService;
import com.radyfy.common.service.crm.permission.CrmPermissionService;
import com.radyfy.common.utils.CrmUtils;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.utils.ValidationUtils;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CrmApiService {

    private final MetaOrmDao metaOrmDao;
    private final CrmModelService crmModelService;
    private final CrmMenuService crmMenuService;
    private final CrmDynamicService crmDynamicService;
    private final CrmFormService crmFormService;
    private final CrmTableService crmTableService;
    private final CrmGridService crmGridService;
    private final CurrentUserSession currentUserSession;
    private final ObjectMapper objectMapper;
    private final CrmPermissionService crmPermissionService;

    @Autowired
    public CrmApiService(
            MetaOrmDao metaOrmDao,
            CrmModelService crmModelService,
            CrmMenuService crmMenuService,
            CrmDynamicService crmDynamicService,
            CrmFormService crmFormService,
            CrmTableService crmTableService,
            CrmGridService crmGridService,
            CurrentUserSession currentUserSession,
            ObjectMapper objectMapper,
            CrmPermissionService crmPermissionService
    ) {
        this.metaOrmDao = metaOrmDao;
        this.crmModelService = crmModelService;
        this.crmMenuService = crmMenuService;
        this.crmDynamicService = crmDynamicService;
        this.crmFormService = crmFormService;
        this.crmGridService = crmGridService;
        this.crmTableService = crmTableService;
        this.currentUserSession = currentUserSession;
        this.objectMapper = objectMapper;
        this.crmPermissionService = crmPermissionService;
    }

    private boolean isRadyfyCommonAPIPath(String path){
        return Api.radyfyCommonApis.stream().anyMatch(p -> path.startsWith(p));
    }

    public CrmApi apiByPath(String path, ApiType apiType, boolean isCommonApi) {

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("path").is(path));
        criteriaList.add(Criteria.where("apiType").is(apiType));
        
        if (isCommonApi) {
            criteriaList.add(Criteria.where("accountId").is(Constants.RADYFY_ACCOUNT_ID));
        }
        
        DaoQuery daoQuery = DaoQuery.builder().criteriaList(criteriaList)
                .build();
        CrmApi crmApi = metaOrmDao.findOneByQuery(daoQuery, CrmApi.class);
        if (crmApi == null) {
            throw new RuntimeException(Constants.API_NOT_FOUND);
        }
        // if (!userService.doCurrentUserHasAccess(crmApi)) {
        // throw new RuntimeException(Constants.ACCESS_DENIED);
        // }

        if (!crmPermissionService.doCurrentUserHasAccess(crmApi)) {
            throw new RuntimeException(Constants.ACCESS_DENIED);
        }

        return crmApi;
    }

    public Object getDataFromApi(String path, HttpServletRequest httpServletRequest) {
        boolean isCommonApi = isRadyfyCommonAPIPath(path);
        CrmApi crmApi = apiByPath(path, ApiType.GET, isCommonApi);
        GridRequestParams params = Utils.getGridParams(httpServletRequest);
        if (crmApi.getAction() == CrmApi.CrmApiAction.CRM_GRID) {
            String gridId = crmApi.getSourceId();
            boolean inner = "true".equals(params.get("inner"));
            params.remove("inner");
            return getGridData(crmApi, crmApi.getGridType(), gridId, params, isCommonApi, inner);
        } else
        // @Deprecated, will be used in DELETE API only
        if (crmApi.getAction() == CrmApi.CrmApiAction.ENTITY_DELETE) {
            CrmGrid tempCrmGrid = new CrmGrid();
            syncParamsAndServerValue(params, tempCrmGrid, crmApi);
            crmApi.setGridParams(tempCrmGrid.getGridParams());
            /*
             * Deleting entity
             */
            crmDynamicService.deleteEntity(params, crmApi);
        } else if (crmApi.getAction() == CrmApi.CrmApiAction.ENTITY_GET) {
            CrmGrid tempCrmGrid = new CrmGrid();
            syncParamsAndServerValue(params, tempCrmGrid, crmApi);
            crmApi.setGridParams(tempCrmGrid.getGridParams());
            return crmDynamicService.getCrmModelData(crmApi, params, isCommonApi);
        } else if (crmApi.getAction() == CrmApiAction.FORM_VALUES_OPTIONS) {
            String gridId = crmApi.getSourceId();
            CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
            syncParamsAndServerValue(params, crmForm, crmApi);
            return crmDynamicService.formValues(params, crmForm, isCommonApi);
        } else if (crmApi.getAction() == CrmApiAction.FORM_FIELDS) {
            String gridId = crmApi.getSourceId();
            CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
            syncParamsAndServerValue(params, crmForm, crmApi);
            return crmDynamicService.buildExportFields(crmForm, isCommonApi);
        } else if (crmApi.getAction() == CrmApi.CrmApiAction.DOWNLOAD_FILE) {
            return downloadFile(crmApi, crmApi.getSourceId(), params, isCommonApi);
        }
        return null;
    }

    private CrmGrid getGridData(
            CrmApi crmApi,
            GridType gridType,
            String gridId,
            GridRequestParams params,
            boolean isCommonApi,
            boolean inner) {
        switch (gridType) {
            case table:
                CrmTable crmTable = crmTableService.getById(gridId, isCommonApi);
                syncParamsAndServerValue(params, crmTable, crmApi);
                crmTable = syncGridValues(crmApi, crmTable, params);
                crmTable.setApiUrl(params.queryString(crmApi.getPath(), crmTable.getGridParams()));
                TableRequest tableRequest = getTableRequest(params);
                crmTable = crmDynamicService.table(crmTable, params, isCommonApi, tableRequest);
                return crmTable;
            case form:
                CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
                syncParamsAndServerValue(params, crmForm, crmApi);
                crmForm = syncGridValues(crmApi, crmForm, params);
                if (Utils.isNotEmpty(crmForm.getPostUrl())) {
                    crmForm.setApiUrl(params.queryStringWithId(crmForm.getPostUrl(), crmForm.getGridParams()));
                } else {
                    crmForm.setApiUrl(params.queryStringWithId(crmApi.getPath(), crmForm.getGridParams()));
                }
                crmForm = crmDynamicService.crmForm(crmForm, params, isCommonApi);
                return crmForm;
            case iframe:
                CrmIframe crmIframe = crmGridService.getById(gridId, CrmIframe.class, isCommonApi);
                syncParamsAndServerValue(params, crmIframe, crmApi);
                crmIframe = syncGridValues(crmApi, crmIframe, params);
                crmIframe.setApiUrl(params.queryStringWithId(crmApi.getPath(), crmIframe.getGridParams()));
                crmIframe.setIframeSrc(params.queryStringWithId(crmIframe.getIframeSrc(), crmIframe.getGridParams()));
                return crmDynamicService.getCrmGrid(crmIframe, params, isCommonApi);
            case calendar:
            case calendar_table:
            case progress_cards:
            case dashboard:
                CrmGridAny crmGrid = crmGridService.getById(gridId, CrmGridAny.class, isCommonApi);
                syncParamsAndServerValue(params, crmGrid, crmApi);
                crmGrid = syncGridValues(crmApi, crmGrid, params);
                crmGrid.setApiUrl(params.queryStringWithId(crmApi.getPath(), crmGrid.getGridParams()));
                return crmDynamicService.getCrmGrid(crmGrid, params, isCommonApi);
            case menu:
                if (inner) {
                    return getGridData(crmApi, crmApi.getInnerGridType(), crmApi.getInnerSourceId(), params, isCommonApi, false);
                } else {
                    CrmMenu crmMenu = crmMenuService.getById(gridId, isCommonApi);
                    syncParamsAndServerValue(params, crmMenu, crmApi);
                    return crmMenuService.syncCrmMenu(crmMenu, params);
                }
            default:
                throw new RuntimeException(Constants.API_GRID_NOT_PRESENT);
        }
    }

    private TableRequest getTableRequest(GridRequestParams params) {
        TableRequest tableRequest = new TableRequest();
        if (Utils.isNotEmpty(params)) {
            if (params.get("p") == null || params.get("p").isEmpty()) {
                tableRequest.setP(0);
            } else {
                tableRequest.setP(Integer.parseInt(params.get("p")));
            }
            if (params.get("s") == null || params.get("s").isEmpty()) {
                tableRequest.setS(10);
            } else {
                tableRequest.setS(Integer.parseInt(params.get("s")));
            }
            if (params.get("q") != null && !params.get("q").isEmpty()) {
                tableRequest.setQ(params.get("q"));
            }
            if (params.get("filters") != null && !params.get("filters").isEmpty()) {
                tableRequest.setFilters(TableRequest.decodeFilters(params.get("filters")));
            }
            if (params.get("sort") != null && !params.get("sort").isEmpty()) {
                tableRequest.setSort(TableRequest.decodeSort(params.get("sort")));
            }
        }
        return tableRequest;
    }

    public CrmForm getInnerCrmForm(HttpServletRequest httpServletRequest, String formId, boolean isCommonApi) {
        GridRequestParams params = Utils.getGridParams(httpServletRequest);
        CrmForm crmForm = crmFormService.getById(formId, isCommonApi);
        syncParamsAndServerValue(params, crmForm, null);
        return crmDynamicService.crmForm(crmForm, params, isCommonApi);
    }

    private <T extends CrmGrid> T syncGridValues(CrmApi crmApi, T crmGrid, GridRequestParams params) {

        crmGrid.setApiType(ApiType.POST);
        if (Utils.isNotEmpty(crmApi.getName())) {
            crmGrid.setGridTitle(crmApi.getName());
        }
        if (Utils.isNotEmpty(crmGrid.getBackUrl())) {
            crmGrid.setBackUrl(params.queryString(crmGrid.getBackUrl(), crmGrid.getGridParams()));
        }
        return crmGrid;
    }

    public Object postDataFromApi(String path, String postBody, HttpServletRequest httpServletRequest) {
        boolean isCommonApi = isRadyfyCommonAPIPath(path);
        CrmApi crmApi = apiByPath(path, ApiType.POST, isCommonApi);
        String gridId = crmApi.getSourceId();
        GridRequestParams params = Utils.getGridParams(httpServletRequest);
        params.remove("inner");
        if (crmApi.getAction() == CrmApi.CrmApiAction.CRM_GRID) {
            // if (crmApi.getGridType() == GridType.table) {
            // TableRequest tableRequest = gson.fromJson(postBody, TableRequest.class);
            // CrmTable crmTable = crmTableService.getById(gridId);
            // syncParamsAndServerValue(params, crmTable, crmApi);
            // crmTable = syncGridValues(crmApi, crmTable, params);
            // crmTable.setApiUrl(params.queryString(crmApi.getPath(),
            // crmTable.getGridParams()));
            // crmTable = crmDynamicService.table(tableRequest, crmTable, params);
            // return crmTable;
            // }
            throw new RuntimeException(Constants.API_GRID_NOT_PRESENT);
        } else if (crmApi.getAction() == CrmApi.CrmApiAction.FORM_SAVE) {
            CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
            syncParamsAndServerValue(params, crmForm, crmApi);
            if (Utils.isTrue(crmForm.getUpsertDoc())) {
                try {
                    return crmDynamicService.upsertDocumentForCrmForm(objectMapper.readValue(postBody, Document.class),
                            params,
                            crmForm, isCommonApi);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to parse post body to Document");
                }
            } else {
                String updateId = params.get(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()));
                if (Utils.isNotEmpty(updateId)) {
                    if (ValidationUtils.isValidHexID(updateId)) {
                        try {
                            // updating
                            return crmDynamicService.updateDocumentForCrmForm(
                                    objectMapper.readValue(postBody, Document.class),
                                    params, crmForm, isCommonApi);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse post body to Document");
                        }
                    } else {
                        throw new AuthException();
                    }
                } else {
                    String resultType = params.get("resultType");
                    params.remove("resultType");
                    try {
                        // creating
                        DocCreateResult docCreateResult = crmDynamicService.createDocumentForCrmForm(
                                objectMapper.readValue(postBody, Document.class), params, crmForm, isCommonApi);

                        if ("option".equals(resultType)) {
                            List<CriteriaDefinition> criteriaDefinitions = Collections.singletonList(
                                    Criteria.where("_id").is(docCreateResult.getDocument().getString("_id")));
                            CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);
                            return crmDynamicService.search(crmModel, "", criteriaDefinitions, isCommonApi);
                        }
                        if (docCreateResult.getReturnValue() != null) {
                            return docCreateResult.getReturnValue();
                        }
                        return docCreateResult.getDocument();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse post body to Document");
                    }
                }
            }
        } else
        // @Deprecated, will be used in GET API only
        if (crmApi.getAction() == CrmApi.CrmApiAction.DOWNLOAD_FILE) {
            return downloadFile(crmApi, gridId, postBody, params, isCommonApi);
        }
        throw new RuntimeException(Constants.INVALID_API_ACTION);
    }

    @Deprecated
    private Object downloadFile(CrmApi crmApi, String gridId, String postBody, GridRequestParams params, boolean isCommonApi) {
        CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
        syncParamsAndServerValue(params, crmForm, crmApi);
        crmForm = syncGridValues(crmApi, crmForm, params);
        if (crmApi.getDownloadFileType() == CrmApi.FileType.EXCEL) {
            try {
                ExportForm exportForm = Utils.isNotEmpty(postBody) ? objectMapper.readValue(postBody, ExportForm.class)
                        : new ExportForm();
                if (exportForm.getFields() == null) {
                    exportForm.setFields(new ArrayList<>());
                }
                if (exportForm.getFileName() == null) {
                    exportForm.setFileName(crmForm.getGridTitle());
                }
                if (!Utils.isNotEmpty(exportForm.getFields())) {
                    CheckboxGroup[] checkboxGroups = crmDynamicService.buildExportFields(crmForm, isCommonApi);
                    for (CheckboxGroup checkboxGroup : checkboxGroups) {
                        for (Option option : checkboxGroup.getOptions()) {
                            if (!exportForm.getFields().contains(option.getKey())) {
                                exportForm.getFields().add(option.getKey());
                            }
                        }
                    }
                }
                CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);
                List<CriteriaDefinition> criteriaDefinitions = params.getGridFiltersCriteria(crmForm.getGridParams());
                return crmDynamicService.exportExcel(exportForm, crmModel, criteriaDefinitions, crmForm,
                        crmApi.getGetAllRecords(), crmApi.getRecordsLimit(), isCommonApi);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse post body to ExportForm");
            }
        }
        return null;
    }

    private Object downloadFile(CrmApi crmApi, String gridId, GridRequestParams params, boolean isCommonApi) {
        CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
        syncParamsAndServerValue(params, crmForm, crmApi);
        crmForm = syncGridValues(crmApi, crmForm, params);
        if (crmApi.getDownloadFileType() == CrmApi.FileType.EXCEL) {
            ExportForm exportForm = new ExportForm();
            if (Utils.isNotEmpty(params.get("fields"))) {
                exportForm.setFields(Arrays.asList(params.get("fields").split(",")));
            } else if (exportForm.getFields() == null) {
                exportForm.setFields(new ArrayList<>());
            }
            if (Utils.isNotEmpty(params.get("fileName"))) {
                exportForm.setFileName(params.get("fileName"));
            } else if (exportForm.getFileName() == null) {
                exportForm.setFileName(crmForm.getGridTitle());
            }
            if (!Utils.isNotEmpty(exportForm.getFields())) {
                CheckboxGroup[] checkboxGroups = crmDynamicService.buildExportFields(crmForm, isCommonApi);
                for (CheckboxGroup checkboxGroup : checkboxGroups) {
                    for (Option option : checkboxGroup.getOptions()) {
                        if (!exportForm.getFields().contains(option.getKey())) {
                            exportForm.getFields().add(option.getKey());
                        }
                    }
                }
            }
            CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);
            List<CriteriaDefinition> criteriaDefinitions = params.getGridFiltersCriteria(crmForm.getGridParams());
            return crmDynamicService.exportExcel(exportForm, crmModel, criteriaDefinitions, crmForm,
                    crmApi.getGetAllRecords(), crmApi.getRecordsLimit(), isCommonApi);
        }
        throw new UnsupportedOperationException("Unsupported file type '" + crmApi.getDownloadFileType() + "'");
    }

    private void syncParamsAndServerValue(GridRequestParams params, CrmGrid crmGrid, CrmApi crmApi) {

        List<GridParam> gridParams = crmGrid.getGridParams();
        if (gridParams == null) {
            gridParams = new ArrayList<>();
            crmGrid.setGridParams(gridParams);
        }
        List<GridParam> apiParams;
        if (crmApi == null) {
            apiParams = new ArrayList<>();
        } else {
            apiParams = crmApi.getGridParams();
        }
        if (apiParams == null) {
            apiParams = new ArrayList<>();
            crmApi.setGridParams(apiParams);
        }

        // merge apiParams with gridParams and remove duplicates priotiizing apiParams
        if (Utils.isNotEmpty(apiParams)) {
            for (GridParam apiParam : apiParams) {
                if (gridParams.stream().noneMatch(p -> p.getKey().equals(apiParam.getKey()))) {
                    gridParams.add(apiParam);
                } else {
                    GridParam gridParam = gridParams.stream().filter(p -> p.getKey().equals(apiParam.getKey()))
                            .findFirst().get();
                    gridParam.setRequired(apiParam.isRequired());
                    gridParam.setServerValue(apiParam.getServerValue());
                    gridParam.setDocumentKey(apiParam.getDocumentKey());
                }
            }
        }

        if (Utils.isNotEmpty(gridParams)) {
            for (GridParam gridParam : gridParams) {
                if (gridParam.getServerValue() != null) {
                    switch (gridParam.getServerValue()) {
                        case current_user_id:
                            if (currentUserSession.getUser() == null) {
                                if (gridParam.isRequired()) {
                                    throw new AuthException();
                                }
                            } else {
                                params.put(gridParam.getKey(), currentUserSession.getUser().getId());
                            }
                            break;
                        case fixed_value:
                            if (Utils.isNotEmpty(gridParam.getValue())) {
                                params.put(gridParam.getKey(), gridParam.getValue());
                            } else {
                                if (gridParam.isRequired()) {
                                    throw new RuntimeException(Constants.INVALID_FIXED_VALUE);
                                }
                            }
                            break;
                        case filter_value:
                            if (Utils.isNotEmpty(gridParam.getValue())) {
                                if (currentUserSession.getUserSession() != null) {
                                    String value = currentUserSession.getUserSession().getFeFilters()
                                            .get(gridParam.getValue());
                                    if (Utils.isNotEmpty(value)) {
                                        params.put(gridParam.getKey(), value);
                                    } else {
                                        if (gridParam.isRequired()) {
                                            throw new RuntimeException(Constants.INVALID_FILTER_VALUE);
                                        }
                                    }
                                } else {
                                    if (gridParam.isRequired()) {
                                        throw new RuntimeException(Constants.INVALID_FILTER_VALUE);
                                    }
                                }
                            } else {
                                if (gridParam.isRequired()) {
                                    throw new RuntimeException(Constants.INVALID_FILTER_VALUE);
                                }
                            }
                            break;
                        default:
                            throw new RuntimeException(Constants.INVALID_SERVER_VALUE);
                    }
                }
            }
        }
    }

    public void deleteDataFromApi(String path, HttpServletRequest httpServletRequest) {
        boolean isCommonApi = isRadyfyCommonAPIPath(path);
        CrmApi crmApi = apiByPath(path, ApiType.DELETE, isCommonApi);
        GridRequestParams params = Utils.getGridParams(httpServletRequest);

        if (crmApi.getAction() == CrmApi.CrmApiAction.ENTITY_DELETE) {

            CrmGrid tempCrmGrid = new CrmGrid();
            syncParamsAndServerValue(params, tempCrmGrid, crmApi);
            crmApi.setGridParams(tempCrmGrid.getGridParams());
            /*
             * Deleting entity
             */
            crmDynamicService.deleteEntity(params, crmApi);
            return;
        }
        throw new UnsupportedOperationException("Unsupported action for DELETE method '" + crmApi.getAction() + "'");
    }

    public Object putDataFromApi(String path, String postBody, HttpServletRequest httpServletRequest) {
        boolean isCommonApi = isRadyfyCommonAPIPath(path);
        CrmApi crmApi = apiByPath(path, ApiType.PUT, isCommonApi);
        String gridId = crmApi.getSourceId();
        GridRequestParams params = Utils.getGridParams(httpServletRequest);
        params.remove("inner");
        if (crmApi.getAction() == CrmApi.CrmApiAction.FORM_SAVE) {
            updateOrUpsertForm(crmApi, gridId, params, postBody, isCommonApi);
        }

        throw new RuntimeException(Constants.INVALID_API_ACTION);
    }

    public Object patchDataFromApi(String path, String postBody, HttpServletRequest httpServletRequest) {
        boolean isCommonApi = isRadyfyCommonAPIPath(path);
        CrmApi crmApi = apiByPath(path, ApiType.PATCH, isCommonApi);
        String gridId = crmApi.getSourceId();
        GridRequestParams params = Utils.getGridParams(httpServletRequest);
        params.remove("inner");
        if (crmApi.getAction() == CrmApi.CrmApiAction.FORM_SAVE) {
            updateOrUpsertForm(crmApi, gridId, params, postBody, isCommonApi);
        }

        throw new RuntimeException(Constants.INVALID_API_ACTION);
    }

    private Object updateOrUpsertForm(CrmApi crmApi, String gridId, GridRequestParams params, String postBody, boolean isCommonApi) {

        CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
        syncParamsAndServerValue(params, crmForm, crmApi);
        if (Utils.isTrue(crmForm.getUpsertDoc())) {
            try {
                return crmDynamicService.upsertDocumentForCrmForm(objectMapper.readValue(postBody, Document.class),
                        params,
                        crmForm, isCommonApi);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse post body to Document");
            }
        } else {
            String updateId = params.get(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()));
            if (Utils.isNotEmpty(updateId)) {
                if (ValidationUtils.isValidHexID(updateId)) {
                    try {
                        // updating
                        return crmDynamicService.updateDocumentForCrmForm(
                                objectMapper.readValue(postBody, Document.class),
                                params, crmForm, isCommonApi);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse post body to Document");
                    }
                } else {
                    throw new AuthException();
                }
            } else {
                throw new AuthException();
            }
        }

    }
}
