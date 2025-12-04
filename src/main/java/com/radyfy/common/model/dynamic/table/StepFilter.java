package com.radyfy.common.model.dynamic.table;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class StepFilter implements Serializable {
    private String key;
    private String value;
    private String name;
    private boolean selected;
}
