package com.radyfy.common.service.crm.config.menu;

import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.menu.CrmMenu;
import com.radyfy.common.model.crm.model.CrmModel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrmMenuConsumerProps {
    private final CrmModel crmModel;
    private final CrmMenu crmMenu;
    private final GridRequestParams gridRequestParams;
}
