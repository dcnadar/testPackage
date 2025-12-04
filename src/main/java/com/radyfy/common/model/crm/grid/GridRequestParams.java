package com.radyfy.common.model.crm.grid;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.utils.BsonDocumentUtils;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.utils.ValidationUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GridRequestParams extends HashMap<String, String> {


    public static GridRequestParams fromSearch(String queryString) {

        if (!Utils.isNotEmpty(queryString)) {
            return null;
        }
        GridRequestParams params = new GridRequestParams();

        if (Utils.isNotEmpty(queryString)) {

            if (queryString.startsWith("?")) {
                queryString = queryString.substring(1);
            }

            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    public String queryString(String url, List<GridParam> params) {

        if (!Utils.isNotEmpty(url)) {
            return null;
        }

        // Parse existing parameters from URL
        Map<String, String> uniqueParams = new HashMap<>();
        String baseUrl = url;
        if (url.contains("?")) {
            int questionMarkIndex = url.indexOf("?");
            baseUrl = url.substring(0, questionMarkIndex);
            String queryPart = url.substring(questionMarkIndex + 1);
            if (Utils.isNotEmpty(queryPart)) {
                for (String param : queryPart.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        uniqueParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }

        // Add new parameters, overwriting existing ones if necessary
        if (Utils.isNotEmpty(params)) {
            params.forEach(param -> {
                String value = getEncodedValue(param);
                if (Utils.isNotEmpty(value)) {
                    uniqueParams.put(param.getKey(), value);
                }
            });
        }

        // Build the final URL
        StringBuilder queryString = new StringBuilder(baseUrl);
        if (!uniqueParams.isEmpty()) {
            queryString.append("?");
            queryString.append(uniqueParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&")));
        }

        return queryString.toString();
    }

    public String queryStringWithId(String url, List<GridParam> params) {
        if (params != null) {
            params = new ArrayList<>(params);
        } else {
            params = new ArrayList<>();
        }

        String id = get("id");
        if (Utils.isNotEmpty(id)) {
            if (params.stream().noneMatch((p) -> {
                return p.getFilterKey().equals("id");
            })) {
                params.add(new GridParam("id", true));
            }
        }
        return queryString(url, params);
    }

    @Deprecated
    public List<CriteriaDefinition> getGridFiltersCriteria() {
        List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
        forEach((key, value) -> {
            if (Utils.isNotEmpty(value)) {
                criteriaDefinitions.add(getCriteria(key, getDecodedValue(new GridParam(key))));
            }
        });
        return criteriaDefinitions;
    }

    public List<CriteriaDefinition> getGridFiltersCriteriaWithId(List<GridParam> params) {
        List<GridParam> requiredParams =
                params != null ? new ArrayList<>(params) : new ArrayList<>();
        if (requiredParams.stream().noneMatch(p -> p.getFilterKey().equals("id"))) {
            requiredParams.add(new GridParam("id", true));
        }
        return getGridFiltersCriteria(requiredParams);
    }

    public List<CriteriaDefinition> getGridFiltersCriteria(List<GridParam> filters) {
        List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
        if (Utils.isNotEmpty(filters)) {
            filters.stream().filter(filter -> Utils.isNotEmpty(filter.getFilterKey())).filter(f -> {
                if (f.isRequired()) {
                    if (Utils.isNotEmpty(getDecodedValue(f))) {
                        return true;
                    } else {
                        throw new RuntimeException("Filter Value not present: " + f.getKey());
                    }
                } else {
                    return Utils.isNotEmpty(getDecodedValue(f));
                }
            }).forEach((filter) -> criteriaDefinitions
                    .add(getCriteria(filter.getFilterKey(), getDecodedValue(filter))));
        }
        return criteriaDefinitions;
    }

    // public List<CriteriaDefinition> getGridFiltersCriteria(List<GridParam> filters, Map<String,
    // String> modelKeys){
    // List<CriteriaDefinition> criteriaDefinitions = new ArrayList<>();
    // if(Utils.isNotEmpty(filters) && Utils.isNotEmpty(modelKeys)) {
    // filters.forEach((filter) -> {
    // String modelKey = modelKeys.get(filter.getKey());
    // if(!Utils.isNotEmpty(modelKey)) {
    // modelKey = filter.getKey();
    // }
    // criteriaDefinitions.add(getCriteria(modelKey, getDecodedValue(filter)));
    // });
    // }
    // return criteriaDefinitions;
    // }

    public void setFiltersToDoc(List<GridParam> filters, Document document) {
        if (Utils.isNotEmpty(filters)) {
            filters.forEach((filter) -> {
                Object value = getDecodedValue(filter);
                if (value != null) {
                    BsonDocumentUtils.setDataValue(document, filter.getFilterKey(), value);
                }
            });
        }
    }

    private CriteriaDefinition getCriteria(String key, String value) {
        if ("id".equals(key)) {
            if (ValidationUtils.isValidHexID(value)) {
                return Criteria.where("_id").is(new ObjectId(value));
            } else {
                throw new AuthException();
            }
        } else {
            if (Utils.isNotEmpty(value) && !value.equals("null") && !value.equals("undefined")) {
                if (value.contains(",")) {
                    return Criteria.where(key).in(value.split(","));
                } else {
                    return Criteria.where(key).is(value);
                }
            } else {
                return Criteria.where(key).exists(false);
            }
        }
    }

    /**
     * Encoded value is Used to append in Link
     */
    private String getEncodedValue(GridParam param) {
        String value = get(param.getKey());
        if (value == null) {
            if (param.isRequired()) {
                throw new RuntimeException("Filter Value not present");
            }
            return null;
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Filter Value not valid");
        }
    }

    private String getDecodedValue(GridParam param) {
        String value = get(param.getKey());
        if (value == null) {
            if (param.isRequired()) {
                throw new RuntimeException(
                        "Grid Param is but value not present : " + param.getKey());
            }
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Grid Param is but value not valid : " + param.getKey());
        }
    }
}
