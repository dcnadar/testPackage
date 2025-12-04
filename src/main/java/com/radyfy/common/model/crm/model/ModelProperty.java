package com.radyfy.common.model.crm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.dynamic.Option;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelProperty implements Serializable {

    private DataType dataType;
    private String key;
    private String name;
    private PropertyData data;
    private Searchable searchable;
    // unique value across all the document on account level
    private Boolean unique;
    
    // these are the flags that are used to validate the data before saving
    private Boolean trim;
    private Boolean escapeChar;
    private Boolean script;

    // only for dataType = INNER_MODEL, REFERENCE, or (LIST_OF for INNER_MODEL or
    // REFERENCE)
    private String modelId;

    // only for dataType = LIST_OF
    private DataType listType;

    @Transient
    private Boolean isOrg;

    @Getter
    @Setter
    public static class PropertyData implements Serializable {
        private String tagCategory;
        private Boolean allowAddTags;
        private List<Option> options;
    }
}
