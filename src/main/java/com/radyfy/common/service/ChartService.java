package com.radyfy.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import com.radyfy.common.commons.Errors;
import com.radyfy.common.model.crm.api.meta.table.DefaultFilter;
import com.radyfy.common.model.crm.grid.chart.CrmChart;
import com.radyfy.common.model.crm.grid.chart.config.BarChartConfig;
import com.radyfy.common.model.crm.grid.chart.config.ChartConfig;
import com.radyfy.common.model.crm.grid.chart.config.LineChartConfig;
import com.radyfy.common.model.crm.grid.chart.config.PieChartConfig;
import com.radyfy.common.model.crm.grid.chart.config.StatChartConfig;
import com.radyfy.common.model.crm.grid.chart.enums.ChartType;
import com.radyfy.common.model.crm.grid.chart.enums.Operation;
import com.radyfy.common.model.crm.grid.chart.enums.XType;
import com.radyfy.common.model.crm.grid.chart.TimeBin;
import com.radyfy.common.model.crm.grid.chart.response.BarChart;
import com.radyfy.common.model.crm.grid.chart.response.BaseChart;
import com.radyfy.common.model.crm.grid.chart.response.LineChart;
import com.radyfy.common.model.crm.grid.chart.response.PieChart;
import com.radyfy.common.model.crm.grid.chart.response.StatChart;
import com.radyfy.common.model.crm.grid.chart.response.BarChart.DataSet;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.grid.CrmGridService;
import com.radyfy.common.utils.CrmUtils;

@Service
public class ChartService {

    private final EntityOrmDao entityOrmDao;
    private final CrmModelService crmModelService;

    @Autowired
    public ChartService(EntityOrmDao entityOrmDao, CrmModelService crmModelService,
            CrmGridService crmGridService) {
        this.entityOrmDao = entityOrmDao;
        this.crmModelService = crmModelService;
    }

    // unique professional colors for pie chart
    private static final String[] backgroundColors = new String[] {"rgba(144, 171, 113, 1.0)",
            "rgba(0, 164, 161, 1.0)", "rgba(134, 207, 192, 1.0)", "rgba(2, 118, 116, 1.0)",
            "rgba(59, 148, 128, 1.0)", "rgba(202, 202, 202, 1.0)", "rgba(169, 191, 146, 1.0)",
            "rgba(152, 152, 152, 1.0)", "rgba(132, 132, 132, 1.0)", "rgba(227, 229, 227, 1.0)",
            "rgba(87, 87, 87, 1.0)", "rgba(185, 185, 185, 1.0)", "rgba(9, 107, 90, 1.0)",
            "rgba(34, 34, 34, 1.0)", "rgba(85, 182, 179, 1.0)", "rgba(243, 244, 242, 1.0)",
            "rgba(166, 210, 205, 1.0)", "rgba(207, 224, 204, 1.0)", "rgba(162, 162, 162, 1.0)",
            "rgba(112, 112, 112, 1.0)", "rgba(221, 249, 247, 1.0)", "rgba(249, 250, 249, 1.0)",
            "rgba(203, 238, 235, 1.0)", "rgba(183, 225, 220, 1.0)", "rgba(219, 220, 218, 1.0)",
            "rgba(173, 174, 173, 1.0)", "rgba(154, 191, 147, 1.0)", "rgba(142, 142, 142, 1.0)",
            "rgba(46, 122, 118, 1.0)", "rgba(89, 153, 145, 1.0)", "rgba(51, 164, 160, 1.0)",
            "rgba(72, 72, 72, 1.0)", "rgba(178, 196, 169, 1.0)", "rgba(252, 253, 252, 1.0)",
            "rgba(71, 140, 125, 1.0)", "rgba(123, 123, 123, 1.0)", "rgba(149, 171, 130, 1.0)",
            "rgba(117, 172, 164, 1.0)", "rgba(16, 154, 149, 1.0)", "rgba(236, 237, 235, 1.0)",
            "rgba(237, 254, 253, 1.0)", "rgba(146, 197, 186, 1.0)", "rgba(102, 102, 102, 1.0)",
            "rgba(8, 109, 106, 1.0)", "rgba(193, 193, 193, 1.0)", "rgba(28, 109, 103, 1.0)",
            "rgba(194, 209, 184, 1.0)", "rgba(210, 210, 210, 1.0)", "rgba(55, 55, 55, 1.0)"};


    // border colors that complement the background colors for better visibility
    private static final String[] borderColors = new String[] {"rgba(115, 137, 90, 1.0)",
            "rgba(0, 131, 129, 1.0)", "rgba(107, 166, 154, 1.0)", "rgba(1, 94, 93, 1.0)",
            "rgba(47, 118, 102, 1.0)", "rgba(162, 162, 162, 1.0)", "rgba(135, 153, 117, 1.0)",
            "rgba(122, 122, 122, 1.0)", "rgba(106, 106, 106, 1.0)", "rgba(182, 184, 182, 1.0)",
            "rgba(70, 70, 70, 1.0)", "rgba(148, 148, 148, 1.0)", "rgba(7, 86, 72, 1.0)",
            "rgba(27, 27, 27, 1.0)", "rgba(68, 146, 143, 1.0)", "rgba(195, 196, 194, 1.0)",
            "rgba(133, 168, 164, 1.0)", "rgba(166, 179, 163, 1.0)", "rgba(130, 130, 130, 1.0)",
            "rgba(90, 90, 90, 1.0)", "rgba(177, 199, 198, 1.0)", "rgba(199, 200, 199, 1.0)",
            "rgba(162, 190, 188, 1.0)", "rgba(146, 180, 176, 1.0)", "rgba(175, 176, 174, 1.0)",
            "rgba(138, 139, 138, 1.0)", "rgba(123, 153, 118, 1.0)", "rgba(114, 114, 114, 1.0)",
            "rgba(37, 98, 94, 1.0)", "rgba(71, 122, 116, 1.0)", "rgba(41, 131, 128, 1.0)",
            "rgba(58, 58, 58, 1.0)", "rgba(142, 157, 135, 1.0)", "rgba(202, 203, 202, 1.0)",
            "rgba(57, 112, 100, 1.0)", "rgba(98, 98, 98, 1.0)", "rgba(119, 137, 104, 1.0)",
            "rgba(94, 138, 131, 1.0)", "rgba(13, 123, 119, 1.0)", "rgba(189, 190, 188, 1.0)",
            "rgba(190, 203, 202, 1.0)", "rgba(117, 158, 149, 1.0)", "rgba(82, 82, 82, 1.0)",
            "rgba(6, 87, 85, 1.0)", "rgba(154, 154, 154, 1.0)", "rgba(22, 87, 82, 1.0)",
            "rgba(155, 167, 147, 1.0)", "rgba(168, 168, 168, 1.0)", "rgba(44, 44, 44, 1.0)"};

    public CrmChart getChartData(CrmChart crmChart) {

        ChartConfig chartConfig = crmChart.getChart();
        String chartTitle = crmChart.getGridTitle();

        if (chartConfig == null) {
            throw new RuntimeException(Errors.CRM_CHART_NOT_FOUND);
        }

        ChartType chartType = chartConfig.getChartType();

        String crmModelId = crmChart.getCrmModelId();
        CrmModel crmModel = crmModelService.getModel(crmModelId, false);

        if (crmModel == null) {
            throw new RuntimeException(Errors.CRM_MODEL_NOT_FOUND);
        }

        BaseChart data = null;

        switch (chartType) {
            case STAT_CHART:
                data = getStatChartData(chartConfig, crmModel, chartTitle);
                break;
            case BAR_CHART:
                data = getBarChartData(chartConfig, crmModel, chartTitle);
                break;
            case PIE_CHART:
                data = getPieChartData(chartConfig, crmModel, chartTitle);
                break;
            case LINE_CHART:
                data = getLineChartData(chartConfig, crmModel, chartTitle);
                break;
            default:
                throw new RuntimeException(Errors.INVALID_CHART_TYPE);
        }

        chartConfig.setData(data);
        return crmChart;
    }

    private StatChart getStatChartData(ChartConfig chartConfig, CrmModel crmModel, String chartTitle) {

        StatChartConfig statChartConfig = chartConfig.getStatChart();
        String field = statChartConfig.getField();
        Operation operation = statChartConfig.getOperation();

        List<AggregationOperation> operations = new ArrayList<>();

        operations.addAll(buildDefaultFilterOperations(chartConfig.getDefaultFilters()));

        switch (operation) {
            case COUNT:
                operations.add(Aggregation.group().count().as("count"));
                break;
            case SUM:
                operations.add(Aggregation.group().sum(field).as("sum"));
                break;
            case VALUE:
                operations.add(Aggregation.project(field));
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        AggregationResults<Document> results =
                entityOrmDao.aggregate(crmModel, operations.toArray(new AggregationOperation[0]));


        StatChart statChart = new StatChart();

        Document doc = results.getMappedResults().get(0);

        Object value = null;

        switch (operation) {
            case VALUE:
                value = doc.get(field);
                break;
            case COUNT:
                value = doc.get("count", 0);
                break;
            case SUM:
                value = extractNumericValue(doc.get("sum"));
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        statChart.setName(chartTitle);
        statChart.setValue(value.toString());
        return statChart;
    }

    private BarChart getBarChartData(ChartConfig chartConfig, CrmModel crmModel, String chartTitle) {

        BarChartConfig barChartConfig = chartConfig.getBarChart();
        XType xType = barChartConfig.getXType();
        String xField = barChartConfig.getXField();
        String valueField = barChartConfig.getValueField();
        Operation operation = barChartConfig.getOperation();
        TimeBin timeBin = barChartConfig.getTimeBin();

        AggregationResults<Document> results = null;

        switch (xType) {
            case CATEGORY:
                results = getAggregationResults(chartConfig.getDefaultFilters(), crmModel,
                        operation, xField, valueField);
                break;
            case TIME:
                results = getTimeAggregationResults(chartConfig.getDefaultFilters(), crmModel,
                        operation, xField, timeBin, valueField);
        }

        BarChart barChart = new BarChart();
        barChart.setName(chartTitle);

        switch (operation) {
            case COUNT:
                buildBarChartFromResults(barChart, results, chartConfig,
                        Operation.COUNT.toString().toLowerCase(), chartTitle);
                break;
            case SUM:
                buildBarChartFromResults(barChart, results, chartConfig,
                        Operation.SUM.toString().toLowerCase(), chartTitle);
                break;
            case AVG:
                buildBarChartFromResults(barChart, results, chartConfig,
                        Operation.AVG.toString().toLowerCase(), chartTitle);
                break;
            case MIN:
                buildBarChartFromResults(barChart, results, chartConfig,
                        Operation.MIN.toString().toLowerCase(), chartTitle);
                break;

            case MAX:
                buildBarChartFromResults(barChart, results, chartConfig,
                        Operation.MAX.toString().toLowerCase(), chartTitle);
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        return barChart;

    }

    private PieChart getPieChartData(ChartConfig chartConfig, CrmModel crmModel, String chartTitle) {
        PieChartConfig pieChartConfig = chartConfig.getPieChart();
        String labelField = pieChartConfig.getLabelField();
        String valueField = pieChartConfig.getValueField();
        Operation operation = pieChartConfig.getOperation();


        AggregationResults<Document> results = getAggregationResults(
                chartConfig.getDefaultFilters(), crmModel, operation, labelField, valueField);


        PieChart pieChart = new PieChart();
        pieChart.setName(chartTitle);

        switch (operation) {
            case COUNT:
                buildPieChartFromResults(pieChart, results, chartConfig,
                        Operation.COUNT.toString().toLowerCase(), chartTitle);
                break;
            case SUM:
                buildPieChartFromResults(pieChart, results, chartConfig,
                        Operation.SUM.toString().toLowerCase(), chartTitle);
                break;
            case AVG:
                buildPieChartFromResults(pieChart, results, chartConfig,
                        Operation.AVG.toString().toLowerCase(), chartTitle);
                break;
            case MIN:
                buildPieChartFromResults(pieChart, results, chartConfig,
                        Operation.MIN.toString().toLowerCase(), chartTitle);
                break;
            case MAX:
                buildPieChartFromResults(pieChart, results, chartConfig,
                        Operation.MAX.toString().toLowerCase(), chartTitle);
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        return pieChart;
    }

    private LineChart getLineChartData(ChartConfig chartConfig, CrmModel crmModel, String chartTitle) {
        LineChartConfig lineChartConfig = chartConfig.getLineChart();
        String timeField = lineChartConfig.getTimeField();
        Operation operation = lineChartConfig.getOperation();
        TimeBin timeBin = lineChartConfig.getTimeBin();
        String valueField = lineChartConfig.getValueField();

        AggregationResults<Document> results =
                getTimeAggregationResults(chartConfig.getDefaultFilters(), crmModel, operation,
                        timeField, timeBin, valueField);

        LineChart lineChart = new LineChart();
        lineChart.setName(chartTitle);

        switch (operation) {
            case COUNT:
                buildLineChartFromResults(lineChart, results, chartConfig,
                        Operation.COUNT.toString().toLowerCase(), chartTitle);
                break;
            case SUM:
                buildLineChartFromResults(lineChart, results, chartConfig,
                        Operation.SUM.toString().toLowerCase(), chartTitle);
                break;
            case AVG:
                buildLineChartFromResults(lineChart, results, chartConfig,
                        Operation.AVG.toString().toLowerCase(), chartTitle);
                break;
            case MIN:
                buildLineChartFromResults(lineChart, results, chartConfig,
                        Operation.MIN.toString().toLowerCase(), chartTitle);
                break;
            case MAX:
                buildLineChartFromResults(lineChart, results, chartConfig,
                        Operation.MAX.toString().toLowerCase(), chartTitle);
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        return lineChart;
    }

    private void buildBarChartFromResults(BarChart barChart, AggregationResults<Document> results,
            ChartConfig chartConfig, String operation, String chartTitle) {

        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        for (Document doc : results.getMappedResults()) {
            Object id = doc.get("_id");
            labels.add(id != null ? id.toString() : "null");
            data.add(extractNumericValue(doc.get(operation)));
        }

        barChart.setLabels(labels);

        DataSet dataset = new DataSet();
        dataset.setLabel(chartTitle);
        dataset.setData(data);

        List<String> backgroundColors = getBackgroundColor(labels.size());
        List<String> borderColors = getBorderColor(labels.size());
        dataset.setBackgroundColor(backgroundColors);
        dataset.setBorderColor(borderColors);
        dataset.setBorderWidth(2);

        barChart.setDatasets(new ArrayList<>(Collections.singletonList(dataset)));
    }

    private void buildPieChartFromResults(PieChart pieChart, AggregationResults<Document> results,
            ChartConfig chartConfig, String operation, String chartTitle) {
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Object id = doc.get("_id");
            labels.add(id != null ? id.toString() : "null");
            data.add(extractNumericValue(doc.get(operation)));
        }
        pieChart.setLabels(labels);

        PieChart.DataSet dataset = new PieChart.DataSet();
        dataset.setLabel(chartTitle);
        dataset.setData(data);

        List<String> backgroundColors = getBackgroundColor(labels.size());
        List<String> borderColors = getBorderColor(labels.size());
        dataset.setBackgroundColor(backgroundColors);
        dataset.setBorderColor(borderColors);
        dataset.setBorderWidth(2);
        pieChart.setDatasets(new ArrayList<>(Collections.singletonList(dataset)));
    }

    private void buildLineChartFromResults(LineChart lineChart,
            AggregationResults<Document> results, ChartConfig chartConfig, String operation, String chartTitle) {
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        for (Document doc : results.getMappedResults()) {
            Object id = doc.get("_id");
            labels.add(id != null ? id.toString() : "null");
            data.add(extractNumericValue(doc.get(operation)));
        }

        lineChart.setLabels(labels);

        LineChart.DataSet dataset = new LineChart.DataSet();
        dataset.setLabel(chartTitle);
        dataset.setData(data);

        List<String> backgroundColors = getBackgroundColor(labels.size());
        List<String> borderColors = getBorderColor(labels.size());
        dataset.setBackgroundColor(backgroundColors);
        dataset.setBorderColor(borderColors);
        dataset.setBorderWidth(2);
        dataset.setTension(0.2);
        lineChart.setDatasets(new ArrayList<>(Collections.singletonList(dataset)));
    }


    private AggregationResults<Document> getAggregationResults(List<DefaultFilter> defaultFilters,
            CrmModel crmModel, Operation operation, String labelField, String valueField) {

        List<AggregationOperation> operations = new ArrayList<>();

        operations.addAll(buildDefaultFilterOperations(defaultFilters));

        // Exclude documents where labelField is null or doesn't exist
        operations.add(Aggregation.match(new Criteria().andOperator(
                Criteria.where(labelField).exists(true), Criteria.where(labelField).ne(null))));

        switch (operation) {
            case COUNT:
                operations.add(Aggregation.group(labelField).count().as("count").min("created")
                        .as("created"));
                break;
            case SUM:
                operations.add(Aggregation.group(labelField).sum(valueField).as("sum")
                        .min("created").as("created"));
                break;
            case AVG:
                operations.add(Aggregation.group(labelField).avg(valueField).as("avg")
                        .min("created").as("created"));
                break;
            case MIN:
                operations.add(Aggregation.group(labelField).min(valueField).as("min")
                        .min("created").as("created"));
                break;
            case MAX:
                operations.add(Aggregation.group(labelField).max(valueField).as("max")
                        .min("created").as("created"));
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        // Sort by created date to ensure consistent ordering based on creation time
        operations.add(Aggregation.sort(Sort.Direction.ASC, "created"));

        AggregationResults<Document> results =
                entityOrmDao.aggregate(crmModel, operations.toArray(new AggregationOperation[0]));

        return results;
    }

    private AggregationResults<Document> getTimeAggregationResults(
            List<DefaultFilter> defaultFilters, CrmModel crmModel, Operation operation,
            String timeField, TimeBin timeBin, String valueField) {

        List<AggregationOperation> operations = new ArrayList<>();

        operations.addAll(buildDefaultFilterOperations(defaultFilters));

        // Exclude documents where timeField is null or doesn't exist
        operations.add(Aggregation.match(new Criteria().andOperator(
                Criteria.where(timeField).exists(true), Criteria.where(timeField).ne(null))));

        // Add date range filter: current date and 30 days before (last month's data)
        // Date currentDate = new Date();
        // Calendar calendar = Calendar.getInstance();
        // calendar.setTime(currentDate);
        // calendar.add(Calendar.DAY_OF_MONTH, -30);
        // Date thirtyDaysAgo = calendar.getTime();

        // operations.add(Aggregation.match(
        // Criteria.where(timeField).gte(thirtyDaysAgo).lte(currentDate)));

        // Add date truncation
        operations.add(createDateTruncOperation(timeField, timeBin));

        // Group by time bucket

        switch (operation) {
            case COUNT:
                operations.add(Aggregation.group("__t").count().as("count"));
                break;
            case SUM:
                operations.add(Aggregation.group("__t").sum(valueField).as("sum"));
                break;
            case AVG:
                operations.add(Aggregation.group("__t").avg(valueField).as("avg"));
                break;
            case MIN:
                operations.add(Aggregation.group("__t").min(valueField).as("min"));
                break;
            case MAX:
                operations.add(Aggregation.group("__t").max(valueField).as("max"));
                break;
            default:
                throw new RuntimeException(Errors.INVALID_OPERATION);
        }

        // Sort by time
        operations.add(Aggregation.sort(Sort.Direction.ASC, "_id"));

        return entityOrmDao.aggregate(crmModel, operations.toArray(new AggregationOperation[0]));
    }

    private AggregationOperation createDateTruncOperation(String timeField, TimeBin timeBin) {
        return context -> {
            Document dateTrunc = new Document();
            dateTrunc.put("date", "$" + timeField);
            dateTrunc.put("unit", timeBin.getUnit().toString().toLowerCase());
            dateTrunc.put("binSize", timeBin.getSize());
            dateTrunc.put("timezone", "UTC");
            Document set = new Document("$set",
                    new Document("__t", new Document("$dateTrunc", dateTrunc)));
            return set;
        };
    }


    private List<AggregationOperation> buildDefaultFilterOperations(
            List<DefaultFilter> defaultFilters) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Criteria> criteriaList = CrmUtils.buildCriteriaFromDefaultFilters(defaultFilters);

        if (!criteriaList.isEmpty()) {
            Criteria andCriteria =
                    new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            operations.add(Aggregation.match(andCriteria));
        }
        return operations;
    }

    private List<String> getBackgroundColor(int size) {
        List<String> colors = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            // Use modulo to cycle through colors if there are more labels than colors
            String randomColor = backgroundColors[i % backgroundColors.length];
            colors.add(randomColor);
        }

        return colors;

    }

    private List<String> getBorderColor(int size) {
        List<String> colors = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String randomColor = borderColors[i % borderColors.length];
            colors.add(randomColor);
        }
        return colors;
    }

    private Number extractNumericValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Float) {
            return (Float) value;
        } else {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

}
