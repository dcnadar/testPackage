package com.radyfy.common.model.crm.grid;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import com.radyfy.common.model.BaseEntityModel;
import com.radyfy.common.model.commons.BreadcrumbItem;
import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.enums.grid.GridType;

import java.util.List;

@Getter
@Setter
public class CrmGrid extends BaseEntityModel {
    private String gridTitle;
    private GridType gridType;
    private String backUrl;
    // private GridStatus status;
    private String crmModelId;
    private List<GridParam> gridParams;
    private List<BreadcrumbItem> breadcrumb;

    @Transient
    private ApiType apiType;

    @Transient
    private String apiUrl;

    public enum GridStatus {
        ACTIVE("Active"), INACTIVE("Inactive");

        private String name;
        GridStatus(String name){
            this.name = name;
        }

        public String value(){
            return this.name;
        }
    }
}
