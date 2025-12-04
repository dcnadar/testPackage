package com.radyfy.common.model.crm.grid.chart.config;

import java.io.Serializable;
import com.radyfy.common.model.crm.grid.chart.enums.Operation;
import com.radyfy.common.model.crm.grid.chart.TimeBin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineChartConfig implements Serializable {

    private String timeField;
    private Operation operation;
    private TimeBin timeBin;
    private String valueField;

    
}
