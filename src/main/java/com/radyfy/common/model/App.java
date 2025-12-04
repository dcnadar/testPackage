package com.radyfy.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.dao.MemoryCached;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "app")
@MemoryCached
@JsonInclude(JsonInclude.Include.NON_NULL)
public class App extends BaseEntityModel {
    private String name;
    private String basePath;
    private String ecomAccountId;
    private List<org.bson.Document> plugins;
    private org.bson.Document seo;
    private String robotTxt;
    private String sitemapsXml;
    private String theme;
}
