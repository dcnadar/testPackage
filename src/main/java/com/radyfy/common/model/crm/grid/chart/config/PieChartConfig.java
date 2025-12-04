package com.radyfy.common.model.crm.grid.chart.config;

import java.io.Serializable;
import com.radyfy.common.model.crm.grid.chart.enums.Operation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PieChartConfig implements Serializable {

    private String labelField;
    private Operation operation;
    private String valueField;

}
