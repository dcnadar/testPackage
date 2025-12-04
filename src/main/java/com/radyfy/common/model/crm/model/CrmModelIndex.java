package com.radyfy.common.model.crm.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class CrmModelIndex implements Serializable {

    private String name;
    private Boolean unique;
    private Map<String, Integer> keys;
}
