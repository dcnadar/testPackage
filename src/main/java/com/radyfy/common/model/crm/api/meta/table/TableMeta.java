package com.radyfy.common.model.crm.api.meta.table;

import java.io.Serializable;
import java.util.List;

import com.radyfy.common.request.table.ColumnSort;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableMeta implements Serializable{
	private ColumnSort defaultSort;
	private List<DefaultFilter> defaultFilters;
}
