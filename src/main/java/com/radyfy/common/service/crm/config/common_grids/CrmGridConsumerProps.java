package com.radyfy.common.service.crm.config.common_grids;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.utils.CrmUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrmGridConsumerProps {

    private static final Logger logger = LoggerFactory.getLogger(CrmGridConsumerProps.class);

    private final CrmModel crmModel;
    private CrmGrid crmGrid;
    private final GridRequestParams gridRequestParams;

    public <T extends CrmGrid> T getSyncedCrmGrid(Class<T> crmGridClass) {
        try {
            T instance = CrmUtils.cloneCrmGrid(crmGrid, crmGridClass.newInstance());
            crmGrid = instance;
            return instance;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
