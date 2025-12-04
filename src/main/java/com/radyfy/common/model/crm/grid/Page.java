package com.radyfy.common.model.crm.grid;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.dynamic.table.TableColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Page extends CrmGrid {

	private List<TableColumn> columns;
	private List<Button> actions;
	private String menuMainPage;
	private List<MenuItem> menuItems;
}
