package com.radyfy.common.service.crm.config;

import org.springframework.stereotype.Component;

import com.radyfy.common.service.crm.config.common_grids.CrmGridConfig;
import com.radyfy.common.service.crm.config.main.CrmMainConfig;
import com.radyfy.common.service.crm.config.menu.CrmMenuConfig;
import com.radyfy.common.service.crm.config.model.CrmModelConfig;
import com.radyfy.common.service.crm.config.search.CrmSearchConfig;

@Component
public class ConfigBuilder {

    private final CrmTableConfig crmTableConfig;
    private final CrmFormConfig crmFormConfig;
    private final CrmGridConfig crmGridConfig;
    private final CrmSearchConfig crmSearchConfig;
    private final CrmMainConfig crmMainConfig;
    private final CrmMenuConfig crmMenuConfig;
    private final CrmModelConfig crmModelConfig;

    public ConfigBuilder(){
        this.crmTableConfig = new CrmTableConfig();
        this.crmFormConfig = new CrmFormConfig();
        this.crmGridConfig = new CrmGridConfig();
        this.crmSearchConfig = new CrmSearchConfig();
        this.crmMainConfig = new CrmMainConfig();
        this.crmMenuConfig = new CrmMenuConfig();
        this.crmModelConfig = new CrmModelConfig();
    }

    public CrmTableConfig getTableConfig(){
        return this.crmTableConfig;
    }
    public CrmFormConfig getFormConfig(){
        return this.crmFormConfig;
    }

    public CrmGridConfig getCrmGridConfig(){
        return this.crmGridConfig;
    }

    public CrmSearchConfig getSearchConfig(){
        return this.crmSearchConfig;
    }

    public CrmMainConfig getMainConfig(){
        return this.crmMainConfig;
    }

    public CrmMenuConfig getMenuConfig(){
        return this.crmMenuConfig;
    }

    public CrmModelConfig getModelConfig(){
        return this.crmModelConfig;
    }
}
