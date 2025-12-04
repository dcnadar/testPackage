package com.radyfy.common.request.table;

public class ColumnFilter {

    private FilterOperator operator;
    private Object value;
    public ColumnFilter() {
        super();
    }

    public static boolean valid(ColumnFilter filter) {
        return filter != null && filter.getOperator() != null && filter.getValue() != null;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public void setOperator(FilterOperator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public enum FilterOperator {
        // Date filters
        BEFORE, AFTER, FROM, TO,

        // Number filters
        LESS_THAN, GREATER_THAN,

        // String filters
        CONTAINS, STARTS_WITH,

        // multi select filters
        IN, NOT_IN,

        // Common filters
        EQUALS, EXISTS, NOT_EQUALS
    }

}