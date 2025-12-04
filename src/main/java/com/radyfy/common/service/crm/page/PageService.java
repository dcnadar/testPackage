package com.radyfy.common.service.crm.page;

import com.radyfy.common.commons.Api;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.CrmModels;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.commons.BreadcrumbItem;
import com.radyfy.common.model.commons.ExportForm;
import com.radyfy.common.model.crm.CrmIframe;
import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.crm.api.CrmApi;
import com.radyfy.common.model.crm.grid.CrmDashboard;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.CrmGridAny;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.Page;
import com.radyfy.common.model.crm.grid.chart.CrmChart;
import com.radyfy.common.model.crm.grid.menu.CrmMenu;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.crm.page.EntityActionMeta;
import com.radyfy.common.model.crm.page.FormSaveMeta;
import com.radyfy.common.model.crm.page.GetPageMeta;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.dynamic.DocCreateResult;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.dynamic.table.ButtonInitType;
import com.radyfy.common.model.dynamic.table.PageOpenType;
import com.radyfy.common.model.dynamic.table.PageParam;
import com.radyfy.common.model.dynamic.table.TableColumn;
import com.radyfy.common.model.dynamic.table.Column.Type;
import com.radyfy.common.model.enums.grid.GridType;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.response.CheckboxGroup;
import com.radyfy.common.service.ChartService;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.CrmDynamicService;
import com.radyfy.common.service.crm.CrmModelService;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PageService {

	private final MetaOrmDao metaOrmDao;
	private final CrmModelService crmModelService;
	private final CrmMenuService crmMenuService;
	private final CrmDynamicService crmDynamicService;
	private final CrmFormService crmFormService;
	private final CrmTableService crmTableService;
	private final CrmGridService crmGridService;
	private final CurrentUserSession currentUserSession;
	private final ChartService chartService;
	private final DashboardService dashboardService;
	private final CrmPermissionService crmPermissionService;

	private record PagePathData(Page page, List<BreadcrumbItem> breadcrumbItems) {
	}

	@Autowired
	public PageService(MetaOrmDao metaOrmDao, CrmModelService crmModelService,
			CrmMenuService crmMenuService, CrmDynamicService crmDynamicService,
			CrmFormService crmFormService, CrmTableService crmTableService, CrmGridService crmGridService,
			CurrentUserSession currentUserSession, CrmPermissionService crmPermissionService,
			ChartService chartService, DashboardService dashboardService) {
		this.metaOrmDao = metaOrmDao;
		this.crmModelService = crmModelService;
		this.crmMenuService = crmMenuService;
		this.crmDynamicService = crmDynamicService;
		this.crmFormService = crmFormService;
		this.crmTableService = crmTableService;
		this.crmGridService = crmGridService;
		this.currentUserSession = currentUserSession;
		this.chartService = chartService;
		this.crmPermissionService = crmPermissionService;
		this.dashboardService = dashboardService;
	}

	private Page getPage(String pageId) {
		return metaOrmDao.getById(pageId, Page.class,
				DaoQuery.builder().criteriaList(List.of(Criteria.where("accountId").exists(true)))
						.fields(List.of("gridType", "gridParams", "accountId", "crmModelId", "gridTitle",
								"actions", "columns.actions", "columns.type", "menuMainPage", "menuItems"))
						.build());
	}

	public CrmGrid getPageWithData(GetPageMeta pageMeta) {

		List<BreadcrumbItem> breadcrumb = new ArrayList<>();
		Page page = getPage(pageMeta.getPageId());
		if (page == null) {
			throw new RuntimeException("Page not found");
		}

		String slug = pageMeta.getSlug();
		if (slug.startsWith("/")) {
			slug = slug.substring(1);
		}
		if (slug.endsWith("/")) {
			slug = slug.substring(0, slug.length() - 1);
		}
		String parentSlug = pageMeta.getParentSlug();
		if (Utils.isNotEmpty(parentSlug)) {
			if (parentSlug.startsWith("/")) {
				parentSlug = parentSlug.substring(1);
			}
			if (parentSlug.endsWith("/")) {
				parentSlug = parentSlug.substring(0, parentSlug.length() - 1);
			}
			if (Utils.isNotEmpty(parentSlug)) {
				if (slug.startsWith(parentSlug)) {
					slug = slug.substring(parentSlug.length());
					if (slug.startsWith("/")) {
						slug = slug.substring(1);
					}
					breadcrumb.addAll(pageMeta.getBreadcrumb());
				} else {
					throw new RuntimeException("Path mismatch");
				}
			}
		}

		PagePathData pagePathData = null;
		if (Utils.isNotEmpty(slug)) {
			String[] slugParts = slug.split("/");
			if (slugParts.length > 0) {
				pagePathData = getPagePathData(page, slugParts, parentSlug);
				if (pagePathData.page() != null) {
					page = pagePathData.page();
					pageMeta.setPageId(page.getId());
				}
			}
		}

		boolean isCommonApi = CrmModels.RADYDY_COMMON_MODELS.contains(page.getCrmModelId());
		CrmGrid result = getGridData(page, pageMeta, isCommonApi);
		if (pagePathData != null) {
			breadcrumb.addAll(pagePathData.breadcrumbItems());
		}
		result.setBreadcrumb(breadcrumb);
		addParamsToSearch(result);
		return result;
	}

	private void addParamsToSearch(CrmGrid crmGrid) {

		if (Utils.isNotEmpty(crmGrid.getApiUrl())) {



			// Parse existing parameters from URL
			Map<String, String> uniqueParams = new HashMap<>();
			String baseUrl = crmGrid.getApiUrl();
			if (crmGrid.getApiUrl().contains("?")) {
				int questionMarkIndex = crmGrid.getApiUrl().indexOf("?");
				baseUrl = crmGrid.getApiUrl().substring(0, questionMarkIndex);
				String queryPart = crmGrid.getApiUrl().substring(questionMarkIndex + 1);
				if (Utils.isNotEmpty(queryPart)) {
					for (String param : queryPart.split("&")) {
						String[] keyValue = param.split("=");
						if (keyValue.length == 2) {
							uniqueParams.put(keyValue[0], keyValue[1]);
						}
					}
				}
			}

			if (!uniqueParams.isEmpty()) {
				try {
					crmGrid.setApiUrl(baseUrl + "?search="
							+ URLEncoder.encode(uniqueParams.entrySet().stream()
									.map(entry -> entry.getKey() + "=" + entry.getValue())
									.collect(Collectors.joining("&")), StandardCharsets.UTF_8.toString()));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Failed to encode search parameters: " + e.getMessage());
				}
			}
		}
	}

	private PagePathData getPagePathData(Page currentPage, String[] slugParts, String parentSlug) {
		if (Utils.isNotEmpty(parentSlug)) {
			parentSlug = "/" + parentSlug;
		} else {
			parentSlug = "";
		}
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();
		breadcrumbItems
				.add(new BreadcrumbItem(currentPage.getGridTitle(), parentSlug + "/" + slugParts[0]));
		Page finalPage = null;
		String[] parentPath = new String[slugParts.length];
		parentPath[0] = slugParts[0];
		if (currentPage.getGridType() == GridType.table) {
			// || (parentSlug.isEmpty() && currentPage.getGridType() == GridType.menu)) {
			for (int i = 1; i < slugParts.length; i++) {
				String slug = slugParts[i];
				parentPath[i] = slug;

				Button foundButton = getButtonByPageKey(currentPage.getActions(), slug);
				if (foundButton == null && Utils.isNotEmpty(currentPage.getColumns())) {
					for (TableColumn column : currentPage.getColumns()) {
						if (column.getType() == Type.actions || column.getType() == Type.options) {
							foundButton = getButtonByPageKey(column.getActions(), slug);
							if (foundButton != null) {
								break;
							}
						}
					}
				}

				if (foundButton != null) {

					currentPage = getPage(foundButton.getPage());
					if (currentPage == null) {
						throw new RuntimeException("Page not found: " + foundButton.getPage());
					}
					if (i == slugParts.length - 1) {
						finalPage = currentPage;

					}
					if(Utils.isNotEmpty(foundButton.getGridTitle())) {
						currentPage.setGridTitle(foundButton.getGridTitle());
					}
					breadcrumbItems.add(new BreadcrumbItem(currentPage.getGridTitle(),
							parentSlug + "/" + String.join("/", parentPath)));

				} else if (currentPage.getGridType() == GridType.menu) {
					MenuItem menuItem = getMenuItemByKey(currentPage.getMenuItems(), slug);
					if (menuItem != null) {
						breadcrumbItems
								.add(new BreadcrumbItem(menuItem.getValue(), "/" + String.join("/", parentPath)));
						currentPage = getPage(menuItem.getPage());
						if (currentPage == null) {
							throw new RuntimeException("Page not found: " + menuItem.getPage());
						}
						if (i == slugParts.length - 1) {
							finalPage = currentPage;
						}
					}
				} else {
					throw new RuntimeException("Page not found: " + slug);
				}
			}
		}
		return new PagePathData(finalPage, breadcrumbItems);
	}


	private void syncActions(List<Button> actions) {
		if (Utils.isNotEmpty(actions)) {
			for (Button button : actions) {
				if (button.getInitType() == ButtonInitType.PAGE && Utils.isNotEmpty(button.getPage())) {
					Page page = getPage(button.getPage());
					if (page != null) {
						button.setPageType(page.getGridType());
						if (Utils.isNotEmpty(page.getGridParams())) {
							button.setPageParams(new ArrayList<>());
							for (GridParam gridParam : page.getGridParams()) {
								button.getPageParams().add(new PageParam(gridParam.getKey(),
										gridParam.getFilterKey(), gridParam.isRequired()));
							}
						}
					}
				}
			}
		}
	}

	private Button getButtonByPageKey(List<Button> actions, String pageKey) {
		if (Utils.isNotEmpty(actions)) {
			for (Button button : actions) {
				if (button.getInitType() == ButtonInitType.PAGE && Utils.isNotEmpty(button.getPage())
						&& (button.getPageOpenType() == PageOpenType.SAME_TAB
								|| button.getPageOpenType() == PageOpenType.NEW_TAB)
						&& pageKey.equals(button.getPageKey())) {
					return button;
				}
			}
		}
		return null;
	}

	private MenuItem getMenuItemByKey(List<MenuItem> menuItems, String pageKey) {
		if (Utils.isNotEmpty(menuItems)) {
			for (MenuItem menuItem : menuItems) {
				if (Utils.isNotEmpty(menuItem.getPage()) && pageKey.equals(menuItem.getKey())) {
					return menuItem;
				}
			}
		}
		return null;
	}

	public void deleteEntity(EntityActionMeta deleteActionMeta) {
		/*
		 * Deleting entity
		 */
		crmDynamicService.deleteEntity(deleteActionMeta);
	}

	private CrmGrid getGridData(Page page, GetPageMeta pageMeta, boolean isCommonApi) {
		GridRequestParams params = pageMeta.getParams();
		String gridId = pageMeta.getPageId();
		switch (page.getGridType()) {
			case table:
				CrmTable crmTable = crmTableService.getById(pageMeta.getPageId(), isCommonApi);
				syncParamsAndServerValue(params, crmTable);
				crmTable.setApiType(ApiType.GET);
				crmTable.setApiUrl(params.queryStringWithId(Api.radyfyBaseURL + "/page/" + gridId,
						crmTable.getGridParams()));
				TableRequest tableRequest =
						pageMeta.getPayload() != null ? TableRequest.fromPayload(pageMeta.getPayload())
								: new TableRequest();
				crmTable = crmDynamicService.table(crmTable, params, isCommonApi, tableRequest);
				// sync actions
				syncActions(crmTable.getActions());
				if (Utils.isNotEmpty(crmTable.getColumns())) {
					for (TableColumn column : crmTable.getColumns()) {
						if (column.getType() == Type.actions || column.getType() == Type.options) {
							syncActions(column.getActions());
						}
					}
				}
				return crmTable;
			case form:
				CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
				syncParamsAndServerValue(params, crmForm);
				crmForm.setApiType(ApiType.POST);
				if (Utils.isNotEmpty(crmForm.getPostUrl())) {
					crmForm
							.setApiUrl(params.queryStringWithId(crmForm.getPostUrl(), crmForm.getGridParams()));
				} else {
					crmForm.setApiUrl(params.queryStringWithId(Api.radyfyBaseURL + "/page/" + gridId,
							crmForm.getGridParams()));
				}
				crmForm = crmDynamicService.crmForm(crmForm, params, isCommonApi);
				return crmForm;
			case iframe:
				CrmIframe crmIframe = crmGridService.getById(gridId, CrmIframe.class, isCommonApi);
				syncParamsAndServerValue(params, crmIframe);
				crmIframe.setApiUrl(params.queryStringWithId(Api.radyfyBaseURL + "/page/" + gridId,
						crmIframe.getGridParams()));
				crmIframe.setIframeSrc(
						params.queryStringWithId(crmIframe.getIframeSrc(), crmIframe.getGridParams()));
				return crmDynamicService.getCrmGrid(crmIframe, params, isCommonApi);
			case dashboard:
				CrmDashboard crmDashboard = dashboardService.getDashboard(gridId, isCommonApi);
				syncParamsAndServerValue(params, crmDashboard);
				return crmDashboard;
			case calendar:
			case calendar_table:
			case progress_cards:
				CrmGridAny crmGrid = crmGridService.getById(gridId, CrmGridAny.class, isCommonApi);
				syncParamsAndServerValue(params, crmGrid);
				crmGrid.setApiUrl(params.queryStringWithId(Api.radyfyBaseURL + "/page/" + gridId,
						crmGrid.getGridParams()));
				return crmDynamicService.getCrmGrid(crmGrid, params, isCommonApi);
			case menu:
				CrmMenu crmMenu = crmMenuService.getById(gridId, isCommonApi);
				syncParamsAndServerValue(params, crmMenu);
				return crmMenuService.syncCrmMenu(crmMenu, params);
			case chart:
				CrmChart crmChart = crmGridService.getById(gridId, CrmChart.class, isCommonApi);
				syncParamsAndServerValue(params, crmChart);
				crmChart.setApiUrl(params.queryStringWithId(Api.radyfyBaseURL + "/page/" + gridId,
						crmChart.getGridParams()));
				return chartService.getChartData(crmChart);
			default:
				throw new RuntimeException(Constants.API_GRID_NOT_PRESENT);
		}
	}



	public Object postPage(FormSaveMeta formSaveMeta) {
		String pageId = formSaveMeta.getPageId();
		Page page = getPage(pageId);
		if (page == null) {
			throw new RuntimeException("Page not found");
		}
		boolean isCommonApi = CrmModels.RADYDY_COMMON_MODELS.contains(page.getCrmModelId());
		GridRequestParams params = formSaveMeta.getParams();
		Document postBody = formSaveMeta.getDocument();

		if (postBody == null) {
			throw new RuntimeException("Post body is required");
		}

		CrmForm crmForm = crmFormService.getById(pageId, isCommonApi);
		syncParamsAndServerValue(params, crmForm);
		if (Utils.isTrue(crmForm.getUpsertDoc())) {
			return crmDynamicService.upsertDocumentForCrmForm(postBody, params, crmForm, isCommonApi);
		} else {
			String updateId = params.get(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()));
			if (Utils.isNotEmpty(updateId)) {
				if (ValidationUtils.isValidHexID(updateId)) {
					return crmDynamicService.updateDocumentForCrmForm(postBody, params, crmForm, isCommonApi);
				} else {
					throw new AuthException();
				}
			} else {
				String resultType = params.get("resultType");
				params.remove("resultType");
				// creating
				DocCreateResult docCreateResult =
						crmDynamicService.createDocumentForCrmForm(postBody, params, crmForm, isCommonApi);

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
			}
		}
	}

	// @Deprecated
	// private Object downloadFile(CrmApi crmApi, String gridId, String postBody,
	// GridRequestParams params, boolean isCommonApi) {
	// CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
	// syncParamsAndServerValue(params, crmForm, crmApi);
	// crmForm = syncGridValues(crmApi, crmForm, params);
	// if (crmApi.getDownloadFileType() == CrmApi.FileType.EXCEL) {
	// try {
	// ExportForm exportForm =
	// Utils.isNotEmpty(postBody) ? objectMapper.readValue(postBody, ExportForm.class)
	// : new ExportForm();
	// if (exportForm.getFields() == null) {
	// exportForm.setFields(new ArrayList<>());
	// }
	// if (exportForm.getFileName() == null) {
	// exportForm.setFileName(crmForm.getGridTitle());
	// }
	// if (!Utils.isNotEmpty(exportForm.getFields())) {
	// CheckboxGroup[] checkboxGroups =
	// crmDynamicService.buildExportFields(crmForm, isCommonApi);
	// for (CheckboxGroup checkboxGroup : checkboxGroups) {
	// for (Option option : checkboxGroup.getOptions()) {
	// if (!exportForm.getFields().contains(option.getKey())) {
	// exportForm.getFields().add(option.getKey());
	// }
	// }
	// }
	// }
	// CrmModel crmModel = crmModelService.getModel(crmForm.getCrmModelId(), isCommonApi);
	// List<CriteriaDefinition> criteriaDefinitions =
	// params.getGridFiltersCriteria(crmForm.getGridParams());
	// return crmDynamicService.exportExcel(exportForm, crmModel, criteriaDefinitions, crmForm,
	// crmApi.getGetAllRecords(), crmApi.getRecordsLimit(), isCommonApi);
	// } catch (JsonProcessingException e) {
	// throw new RuntimeException("Failed to parse post body to ExportForm");
	// }
	// }
	// return null;
	// }

	private Object downloadFile(CrmApi crmApi, String gridId, GridRequestParams params,
			boolean isCommonApi) {
		CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
		syncParamsAndServerValue(params, crmForm);
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
			List<CriteriaDefinition> criteriaDefinitions =
					params.getGridFiltersCriteria(crmForm.getGridParams());
			return crmDynamicService.exportExcel(exportForm, crmModel, criteriaDefinitions, crmForm,
					crmApi.getGetAllRecords(), crmApi.getRecordsLimit(), isCommonApi);
		}
		throw new UnsupportedOperationException(
				"Unsupported file type '" + crmApi.getDownloadFileType() + "'");
	}

	private void syncParamsAndServerValue(GridRequestParams params, CrmGrid crmGrid) {

		List<GridParam> gridParams = crmGrid.getGridParams();
		if (gridParams == null) {
			gridParams = new ArrayList<>();
			crmGrid.setGridParams(gridParams);
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
									String value =
											currentUserSession.getUserSession().getFeFilters().get(gridParam.getValue());
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

	public void deleteDataFromApi(EntityActionMeta entityActionMeta) {
		crmDynamicService.deleteEntity(entityActionMeta);
	}

	public Object putPage(FormSaveMeta formSaveMeta) {
		String pageId = formSaveMeta.getPageId();
		Page page = getPage(pageId);
		if (page == null) {
			throw new RuntimeException("Page not found");
		}
		boolean isCommonApi = CrmModels.RADYDY_COMMON_MODELS.contains(page.getCrmModelId());
		Document postBody = formSaveMeta.getDocument();

		if (postBody == null) {
			throw new RuntimeException("Put body is required");
		}
		return updateOrUpsertForm(formSaveMeta, isCommonApi);

	}

	public Object patchPage(FormSaveMeta formSaveMeta) {
		String pageId = formSaveMeta.getPageId();
		Page page = getPage(pageId);
		if (page == null) {
			throw new RuntimeException("Page not found");
		}
		boolean isCommonApi = CrmModels.RADYDY_COMMON_MODELS.contains(page.getCrmModelId());
		GridRequestParams params = formSaveMeta.getParams();
		Document postBody = formSaveMeta.getDocument();

		if (postBody == null) {
			throw new RuntimeException("Patch body is required");
		}

		return updateOrUpsertForm(formSaveMeta, isCommonApi);
	}

	private Object updateOrUpsertForm(FormSaveMeta formSaveMeta, boolean isCommonApi) {

		String gridId = formSaveMeta.getPageId();
		GridRequestParams params = formSaveMeta.getParams();
		Document postBody = formSaveMeta.getDocument();
		CrmForm crmForm = crmFormService.getById(gridId, isCommonApi);
		syncParamsAndServerValue(params, crmForm);
		if (Utils.isTrue(crmForm.getUpsertDoc())) {
			return crmDynamicService.upsertDocumentForCrmForm(postBody, params, crmForm, isCommonApi);
		} else {
			String updateId = params.get(CrmUtils.getCrmFormIdGridKey(crmForm.getGridParams()));
			if (Utils.isNotEmpty(updateId)) {
				if (ValidationUtils.isValidHexID(updateId)) {
					return crmDynamicService.updateDocumentForCrmForm(postBody, params, crmForm, isCommonApi);
				}
			}
		}

		throw new AuthException();
	}

	public List<Document> getPageParams(String pageId) {
		Page page = getPage(pageId);
		if (page == null) {
			throw new RuntimeException("Page not found: " + pageId);
		}
		List<GridParam> gridParams = page.getGridParams();

		List<Document> pageParams = new ArrayList<>();
		for (GridParam gridParam : gridParams) {
			Document document = new Document();
			document.append("paramKey", gridParam.getKey());
			document.append("paramValue", gridParam.getFilterKey());
			document.append("required", gridParam.isRequired());
			pageParams.add(document);
		}
		return pageParams;
	}

}
