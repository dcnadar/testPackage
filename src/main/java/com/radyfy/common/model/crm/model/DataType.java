package com.radyfy.common.model.crm.model;

public enum DataType {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    DOUBLE("Double"),
    DATE("Date"),
    BOOLEAN("Boolean"),
    LIST_OF("List"),
    REFERENCE("Reference"),
    INNER_MODEL("Inner Model"),
    OBJECT("Object"),
    ANY("Any");
    private String name;
    DataType(String name){
        this.name = name;
    }

    public String value(){
        return this.name;
    }
}
