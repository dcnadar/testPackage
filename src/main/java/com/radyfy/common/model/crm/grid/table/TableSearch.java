package com.radyfy.common.model.crm.grid.table;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TableSearch implements Serializable {
    private String placeHolder;
}
