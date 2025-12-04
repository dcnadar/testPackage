package com.radyfy.common.response.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.enums.grid.GridType;
import com.radyfy.common.response.GridResponse;

@Getter
@Setter
public class DashboardResponse extends GridResponse {

    private List<DashboardItem> data;
    private List<Button> actions;

    private ApiType apiType;
    private String apiUrl;

    public DashboardResponse(){
        super();
        setGridType(GridType.dashboard);
    }
}
