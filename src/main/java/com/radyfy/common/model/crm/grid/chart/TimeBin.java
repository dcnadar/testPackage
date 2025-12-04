package com.radyfy.common.model.crm.grid.chart;

import java.io.Serializable;
import com.radyfy.common.model.crm.grid.chart.enums.Unit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeBin implements Serializable {

    private Unit unit;
    private Integer size;
}
