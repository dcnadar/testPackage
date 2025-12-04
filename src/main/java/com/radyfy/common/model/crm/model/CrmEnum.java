package com.radyfy.common.model.crm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.BaseEntityModel;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.dynamic.Option;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_enum")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmEnum extends BaseEntityModel {

    private List<Option> values;
    private String name;
}
