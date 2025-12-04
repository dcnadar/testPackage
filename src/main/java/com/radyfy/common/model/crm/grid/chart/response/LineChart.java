package com.radyfy.common.model.crm.grid.chart.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineChart extends BaseChart {

    private List<String> labels;
    private List<DataSet> datasets;

    @Getter
    @Setter
    public static class DataSet {
        private String label;
        private List<Number> data;
        private List<String> borderColor;
        private Integer borderWidth;
        private List<String> backgroundColor;
        private Double tension;
    }

}
