package com.radyfy.common.model.dynamic.table;

import lombok.Builder;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.radyfy.common.model.dynamic.form.FormItem;
import com.radyfy.common.response.Label;

@Data
@Builder
public class TableColumn implements Serializable {
	private String name;
	private String key;
	private Column.Type type;
	private Boolean sort;
	private String slug;
	private Integer width;
	private List<Button> actions;
	private String align;
	private String handleKey;
	private String disableKey;
	private FormItem.Type inputType;
	private String valueCondition;

	// labels and meta should set in runtime
	private Map<String, Label> labels;
	private Map<String, Object> meta;


	// dynamic colomn setting in runtime
	private Boolean show;
	private Boolean select;
}
