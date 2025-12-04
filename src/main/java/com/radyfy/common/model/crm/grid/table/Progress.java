package com.radyfy.common.model.crm.grid.table;

import lombok.Data;

import java.io.Serializable;

@Data
public class Progress implements Serializable {

    private String label;
    private double value;
}
