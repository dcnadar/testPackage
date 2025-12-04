package com.radyfy.common.model.crm.api.meta.table;

import java.io.Serializable;
import java.util.List;

import com.radyfy.common.request.table.ColumnFilter.FilterOperator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultFilter implements Serializable {
  private String propertyKey;
  private FilterOperator operator;
  private String EQUALS;
  private Boolean EXISTS;
  private List<String> IN;
  private String NOT_EQUALS;
}
