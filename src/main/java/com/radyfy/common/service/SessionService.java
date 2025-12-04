package com.radyfy.common.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.user.User;
import com.radyfy.common.response.config.LoginResponse;
import com.radyfy.common.service.crm.CrmDynamicService;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.grid.CrmFormService;
import com.radyfy.common.utils.Utils;

@Component
public class SessionService {

    private final MenuService menuService;
    private final CrmFormService crmFormService;
    private final CrmModelService crmModelService;
    private final CrmDynamicService crmDynamicService;

    @Autowired
    public SessionService(
            MenuService menuService,
            CrmFormService crmFormService,
            CrmDynamicService crmDynamicService,
            CrmModelService crmModelService

    ) {
        this.menuService = menuService;
        this.crmFormService = crmFormService;
        this.crmDynamicService = crmDynamicService;
        this.crmModelService = crmModelService;
    }

    public LoginResponse getLoginResponse(
            User user
    ) {
        LoginResponse loginResponse = new LoginResponse(user);

        //setting menu items
        loginResponse.setAppMenu(menuService.getAppMenu(user));
        loginResponse.setS(true);

        // Utils.isTrue(user.getAdmin()) && 
        if(Utils.isNotEmpty(user.getCrmLastFilter())){
            
            CrmForm accountFilterForm = crmFormService.getAccountFilterForm();

            if (accountFilterForm != null && accountFilterForm.getRows() != null && accountFilterForm.getRows().length > 0) {

                Document accountFilterData = new Document();
                accountFilterData.putAll(user.getCrmLastFilter());

                CrmModel crmModel = crmModelService.getModel(accountFilterForm.getCrmModelId(), false);
                crmDynamicService.syncValuesCrmForm(accountFilterForm.getRows(), crmModel, accountFilterData, false);
                loginResponse.setTopFilters(accountFilterForm.getRows());
            }
        }

        return loginResponse;
    }
}
