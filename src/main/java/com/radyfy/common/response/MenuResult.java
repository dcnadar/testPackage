package com.radyfy.common.response;

import lombok.Data;

import java.util.List;

import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.enums.grid.GridType;

@Data
public class MenuResult extends GridResponse{

    private String thumb;
    private List<MenuItem> menuItems;
    private List<Button> actions;
    private List<Option> details;
    private String title;
    private String backUrl;
    private String slug;

    public MenuResult() {
        super();
        setGridType(GridType.menu);
    }
}
