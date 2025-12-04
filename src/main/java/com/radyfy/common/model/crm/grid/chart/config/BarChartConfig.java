package com.radyfy.common.model.crm.grid.chart.config;

import java.io.Serializable;
import com.radyfy.common.model.crm.grid.chart.TimeBin;
import com.radyfy.common.model.crm.grid.chart.enums.Operation;
import com.radyfy.common.model.crm.grid.chart.enums.XType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarChartConfig implements Serializable {

    private XType xType;
    private String xField;
    private String valueField;
    private TimeBin timeBin;
    private Operation operation;

}


