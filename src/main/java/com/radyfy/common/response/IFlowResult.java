package com.radyfy.common.response;

import com.radyfy.common.model.enums.grid.GridType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IFlowResult extends GridResponse{

    private String iFlowId;
    public IFlowResult() {
        super();
        setGridType(GridType.iframe);
    }

}
