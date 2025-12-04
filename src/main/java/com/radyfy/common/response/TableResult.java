package com.radyfy.common.response;

import java.util.List;
import java.util.Map;

import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.crm.grid.table.TableSelectable;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.dynamic.table.StepFilter;
import com.radyfy.common.model.dynamic.table.TableColumn;
import com.radyfy.common.model.enums.grid.GridType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableResult<T> extends GridResponse {

    private long total;
    private boolean hm;
    private List<T> data;
    private List<TableColumn> columns;
    private Button createButton;
    private List<Button> actions;

    private ApiType apiType;
    private String apiUrl;

    private List<StepFilter> stepFilters;
    private FormGroup[] customFilters;
    private TableSearch search;

    private TableDetails leftDetails;
    private TableSelectable selectable;
    private FormGroup[] leftOpenFilters;
    private Map<String, Object> meta;

    public TableResult() {
        super();
        setGridType(GridType.table);
    }

    public TableResult(List<T> data, long total) {
        super();
        setGridType(GridType.table);
        this.total = total;
        this.data = data;
    }

    public TableResult(List<T> data, boolean hm) {
        super();
        setGridType(GridType.table);
        this.hm = hm;
        this.data = data;
    }

}
