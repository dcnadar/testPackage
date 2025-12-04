package com.radyfy.common.model.crm.grid.table;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

import com.radyfy.common.response.Label;

@Getter
@Setter
public class TableDetails implements Serializable {

    private Progress progress;
    private Map<String, Label> labels;
}
