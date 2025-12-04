package com.radyfy.common.model.dynamic.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormGroup implements Serializable {
    private List<FormField> fields;
    private String label;
    private String tab;
    private Integer span;
    private Boolean showDescription;
    private Integer descriptionSpan;
    private String description;
    private String showCondition;
    private Boolean isOrg;
}
