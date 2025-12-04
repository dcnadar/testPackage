package com.radyfy.common.model.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import com.radyfy.common.request.table.ColumnFilter;

@Getter
@Setter
public class ExportForm {

    private String fileName;
    private List<String> fields;
    private Map<String, List<ColumnFilter>> filters;
}

