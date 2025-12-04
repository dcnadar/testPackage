package com.radyfy.common.response;

import com.radyfy.common.model.enums.grid.GridType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GridResponse {
    private String gridTitle;
    private GridType gridType;
    private String backUrl;
}
