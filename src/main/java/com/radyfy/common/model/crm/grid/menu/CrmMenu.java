package com.radyfy.common.model.crm.grid.menu;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.dynamic.Option;

import java.util.List;
@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
public class CrmMenu extends CrmGrid {

    private String menuMainPage;
    private String thumb;
    private List<MenuItem> menuItems;
    // private List<Button> actions;
    private List<Option> details;
    private String title;
    // private String slug;
    private String description;
}
