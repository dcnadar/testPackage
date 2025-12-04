package com.radyfy.common.model.dynamic.table;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

import com.radyfy.common.model.dynamic.form.FormItem;
import com.radyfy.common.response.Label;

@Data
@Builder
public class TableCell {
    private Button link;
    private String align;
    private List<Button> actions;
    private Map<String, Label> labels;
    private String handleKey;
    private String disableKey;
    private FormItem.Type inputType;
    private String valueCondition;
    private String rowSpan;
    private Map<String, Object> meta;
}
