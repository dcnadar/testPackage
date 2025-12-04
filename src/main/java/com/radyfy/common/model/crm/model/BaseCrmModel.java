package com.radyfy.common.model.crm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.dao.MemoryCached;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_model")
@CompoundIndex(name = "unique-name-ecom", def = "{'accountId': 1, 'name': 1, 'ecomAccountId': 1}", unique = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseCrmModel extends CrmBasicModel {
    private String fieldName;
    private Integer order;
    private String modelId;
}
