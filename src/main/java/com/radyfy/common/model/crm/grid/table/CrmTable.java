package com.radyfy.common.model.crm.grid.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.api.meta.table.TableMeta;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.model.dynamic.table.StepFilter;
import com.radyfy.common.model.dynamic.table.TableColumn;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmTable extends CrmGrid {

    @Transient
    private long total;
    @Transient
    private boolean hm;
    @Transient
    private List<org.bson.Document> data;

    private List<TableColumn> columns;
    private Button createButton;
    private List<Button> actions;

    private Boolean border;

    private FormGroup[] customFilters;
    private TableSearch search;

    private FormGroup[] leftOpenFilters;

    private TableMeta tableMeta;
    private List<String> fetchAdditionalKeys;

    private List<StepFilter> stepFilters;
    private TableDetails leftDetails;
    private TableSelectable selectable;
}
