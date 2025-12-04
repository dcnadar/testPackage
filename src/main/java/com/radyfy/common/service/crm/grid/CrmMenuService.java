package com.radyfy.common.service.crm.grid;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.menu.CrmMenu;
import com.radyfy.common.model.crm.grid.menu.CrmMenuTargets;
import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.menu.CrmMenuConfig;
import com.radyfy.common.service.crm.config.menu.CrmMenuConsumerProps;
import com.radyfy.common.service.crm.config.menu.CrmMenuConfig.Event;
import com.radyfy.common.utils.BsonDocumentUtils;
import com.radyfy.common.utils.Utils;

@Component
public class CrmMenuService {

    private static final Logger logger = LoggerFactory.getLogger(CrmFormService.class);

    private final MetaOrmDao metaOrmDao;
    private final CrmModelService crmModelService;
    private final EntityOrmDao crmAccountOrmDao;
    private final CrmMenuConfig crmMenuConfig;

    @Autowired
    public CrmMenuService(
            MetaOrmDao metaOrmDao,
            CrmModelService crmModelService,
            EntityOrmDao crmAccountOrmDao,
            ConfigBuilder configBuilder) {
        this.metaOrmDao = metaOrmDao;
        this.crmModelService = crmModelService;
        this.crmAccountOrmDao = crmAccountOrmDao;
        this.crmMenuConfig = configBuilder.getMenuConfig();
    }

    public CrmMenu getById(String crmMenuId, boolean isCommonApi) {
        if(isCommonApi){
            return metaOrmDao.getById(crmMenuId, CrmMenu.class, DaoQuery.keyValue(Constants.ACCOUNT_ID, Constants.RADYFY_ACCOUNT_ID));
        }
        return metaOrmDao.getById(crmMenuId, CrmMenu.class, null);
    }

    public CrmMenu syncCrmMenu(CrmMenu crmMenu, GridRequestParams filters) {
        CrmModel crmModel = null;
        if (Utils.isNotEmpty(crmMenu.getCrmModelId())) {
            crmModel = crmModelService.getModel(crmMenu.getCrmModelId(), false);
        }
        runEventListener(Event.BEFORE_LOAD, crmMenu, crmModel, filters);
        if (Utils.isNotEmpty(crmMenu.getCrmModelId())) {
            if (Utils.isNotEmpty(filters) && crmModel.getMenuTargets() != null) {
                CrmMenuTargets crmMenuTargets = crmModel.getMenuTargets();
                if (crmMenuTargets != null) {
                    // TODO fix pre assumed valid data, for filters and requiredFilters
                    DaoQuery daoQuery = DaoQuery.builder().fields(crmMenuTargets.getFetchableFields()).build();
                    daoQuery.setCriteriaList(filters.getGridFiltersCriteriaWithId(crmMenu.getGridParams()));
                    Document data = crmAccountOrmDao.findOneByQuery(daoQuery, crmModel);
                    if (data != null) {
                        CrmMenuTargets crmMenuTargetsData = crmMenuTargets.generate(data);
                        if (Utils.isNotEmpty(crmMenuTargetsData.getTitle())) {
                            crmMenu.setTitle(crmMenuTargetsData.getTitle());
                        }
                        if (Utils.isNotEmpty(crmMenuTargetsData.getThumb())) {
                            crmMenu.setThumb(crmMenuTargetsData.getThumb());
                        }
                        if (Utils.isNotEmpty(crmMenuTargetsData.getDescription())) {
                            crmMenu.setDescription(crmMenuTargetsData.getDescription());
                        }
                        if (Utils.isNotEmpty(crmMenuTargetsData.getDetails())) {
                            crmMenu.setDetails(crmMenuTargetsData.getDetails());
                        }
                        if (Utils.isNotEmpty(crmMenu.getMenuItems())) {
                            for (MenuItem menuItem : crmMenu.getMenuItems()) {
                                menuItem.setSlug(filters.queryString(menuItem.getSlug(), crmMenu.getGridParams()));
                                if (Utils.isNotEmpty(menuItem.getSubMenu())) {
                                    for (MenuItem subMenu : menuItem.getSubMenu()) {
                                        subMenu.setSlug(
                                                filters.queryString(subMenu.getSlug(), crmMenu.getGridParams()));
                                    }
                                }
                            }
                        }
                    } else {
                        throw new RuntimeException("Not found");
                    }
                }
            }
        }
        runEventListener(Event.ON_LOAD, crmMenu, crmModel, filters);
        return crmMenu;
    }

    public void runEventListener(
            CrmMenuConfig.Event event,
            CrmMenu crmMenu,
            CrmModel crmModel,
            GridRequestParams gridRequestParams) {
        crmMenuConfig.runConsumer(event, new CrmMenuConsumerProps(
                crmModel, crmMenu, gridRequestParams));
    }

    private String resolveSlug(String slug, Document source) {
        if (Utils.isNotEmpty(slug)) {
            String[] items = slug.split(" ");
            String result = items[0];
            for (int x = 1; x < items.length; x++) {
                String path = items[x];
                Object value = BsonDocumentUtils.getDataValue(source, path, "");
                result = result.replace(":" + path, (String) value);
            }
            return result;
        }
        return slug;
    }
}