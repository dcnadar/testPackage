package com.radyfy.common.model.crm.grid.chart.config;

import java.io.Serializable;
import com.radyfy.common.model.crm.grid.chart.enums.Operation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatChartConfig implements Serializable{

   private String field;
   private Operation operation;
    
}

 