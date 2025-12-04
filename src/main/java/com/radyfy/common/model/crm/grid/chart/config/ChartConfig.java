package com.radyfy.common.model.crm.grid.chart.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.api.meta.table.DefaultFilter;
import com.radyfy.common.model.crm.grid.chart.enums.ChartType;
import com.radyfy.common.model.crm.grid.chart.response.BaseChart;
import java.util.List;
import org.springframework.data.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartConfig implements Serializable {
    private ChartType chartType;
    private StatChartConfig statChart;
    private BarChartConfig barChart;
    private PieChartConfig pieChart;
    private LineChartConfig lineChart;
    private List<DefaultFilter> defaultFilters;

    @Transient
    private BaseChart data;

}
