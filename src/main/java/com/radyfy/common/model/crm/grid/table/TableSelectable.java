package com.radyfy.common.model.crm.grid.table;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

import com.radyfy.common.model.crm.api.ApiType;

@Getter
@Setter
public class TableSelectable implements Serializable {

    private String bulkUpdateCol;
    private String updateApi;
    private ApiType apiType;
    private String selectKey = "_id";
    private Map<String, Object> postData;
    private String disableKey;
    private Boolean checkbox;
    private String crmFormId;
}
