package com.radyfy.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.enums.grid.ActionGridType;
import com.radyfy.common.model.enums.grid.GridType;

@Getter
@Setter
public class FormResult extends GridResponse{

    private ApiType apiType;
    private String apiUrl;
    private boolean edit = true;
    private String param;
    private FormGroup[] rows;
    private ActionGridType submitType;
    private String responseType;
    private List<Button> actions;
    private List<Option> tabs;

    public FormResult(){
        super();
        setGridType(GridType.form);
    }
}
