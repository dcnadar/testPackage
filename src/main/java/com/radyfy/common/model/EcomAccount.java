package com.radyfy.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.commons.MongoDBCredential;
import com.radyfy.common.model.dao.MemoryCached;
import com.radyfy.common.model.enums.EcomAccountStatus;
import com.radyfy.common.model.enums.Environment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "ecom_account")
@MemoryCached
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EcomAccount extends BaseEntityModel {
    private String name;
    private String logo;
    private EcomAccountStatus status;
    private List<String> domain;
    private String subDomain;
    private String favicon;
    private List<String> loginImages;
    // private MongoDBCredential entityMongoDBCredential;
    // private Environment environment;
}
