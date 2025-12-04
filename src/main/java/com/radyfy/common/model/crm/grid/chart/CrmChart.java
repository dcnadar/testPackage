package com.radyfy.common.model.crm.grid.chart;

import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.grid.CrmGrid;
import com.radyfy.common.model.crm.grid.chart.config.ChartConfig;
import com.radyfy.common.model.dao.MemoryCached;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmChart extends CrmGrid {

    private ChartConfig chart;

}

