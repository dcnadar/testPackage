package com.radyfy.common.request;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class TableUpdateRequest<T> {

    private Set<String> excluded;
    private Map<String, T> selected;
    private boolean selectAll;
    private T selectAllValue;
    private String date;
    private Map<String, T> formValues;
}
