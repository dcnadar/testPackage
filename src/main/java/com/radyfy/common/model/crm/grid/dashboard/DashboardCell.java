package com.radyfy.common.model.crm.grid.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardCell<T extends BaseCell> {
    private Integer span;
    private String type;
    private T data;

    public DashboardCell(Integer span, T data, String type) {
        this.span = span;
        this.data = data;
        this.type = type;
    }
}
