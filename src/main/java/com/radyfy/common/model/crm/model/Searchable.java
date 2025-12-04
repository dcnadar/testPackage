package com.radyfy.common.model.crm.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Searchable implements Serializable {
    // highest but not included in search
    private Boolean thumbColor;

    // highest and included in search
    private Boolean thumbInitials;

    // highest but not included in search
    private Boolean thumb;

    // medium and included in search, value used for description
    private Boolean description;
    // last and included in search, value used for title
    private Boolean value;

    /**
     *  primary are used for unique key available for model to search (used in export/import)
     */
    private Boolean primary;
}