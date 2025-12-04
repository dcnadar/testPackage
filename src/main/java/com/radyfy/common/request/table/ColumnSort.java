package com.radyfy.common.request.table;

import java.io.Serializable;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.radyfy.common.utils.Utils;

public class ColumnSort implements Serializable{

    private String field;
    private SortOrder order;
    public ColumnSort() {
        super();
    }

    public ColumnSort(String field, SortOrder order) {
        super();
        this.field = field;
        this.order = order;
    }

    public static boolean valid(ColumnSort sort) {
        return sort != null && Utils.isNotEmpty(sort.getField()) && sort.getOrder() != null && sort.getOrder().getValue() != null;
    }

    public Sort get() {
        return Sort.by(this.order.getValue(), this.field);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SortOrder getOrder() {
        return order;
    }

    public void setOrder(SortOrder order) {
        this.order = order;
    }

    public enum SortOrder {
        descend(Direction.DESC), ascend(Direction.ASC);

        private Sort.Direction value;

        SortOrder(Direction value) {
            this.value = value;
        }

        public Sort.Direction getValue() {
            return this.value;
        }
    }

}
