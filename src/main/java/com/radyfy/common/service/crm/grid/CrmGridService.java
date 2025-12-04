package com.radyfy.common.service.crm.grid;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.Page;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.common_grids.CrmGridConfig;
import com.radyfy.common.service.crm.config.common_grids.CrmGridConsumerProps;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CrmGridService {

    private final MetaOrmDao metaOrmDao;
    private final CrmGridConfig crmGridConfig;

    @Autowired
    public CrmGridService(MetaOrmDao metaOrmDao, ConfigBuilder configBuilder) {
        this.metaOrmDao = metaOrmDao;
        this.crmGridConfig = configBuilder.getCrmGridConfig();
    }

    public <T extends CrmGrid> T getById(String id, Class<T> clazz) {
        return metaOrmDao.getById(id, clazz, null);
    }

    public <T extends CrmGrid> T getById(String id, Class<T> clazz, boolean isCommonApi) {
        if (isCommonApi) {
            return metaOrmDao.getById(id, clazz,
                    DaoQuery.keyValue(Constants.ACCOUNT_ID, Constants.RADYFY_ACCOUNT_ID));
        }
        return metaOrmDao.getById(id, clazz, null);
    }

    public CrmGrid runEventListener(CrmGridConfig.Event event, CrmGrid crmGrid, CrmModel crmModel,
            GridRequestParams gridRequestParams) {
        CrmGridConsumerProps payload =
                new CrmGridConsumerProps(crmModel, crmGrid, gridRequestParams);
        crmGridConfig.runConsumer(event, payload);
        return payload.getCrmGrid();
    }
}
