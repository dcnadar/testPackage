package com.radyfy.common.model.crm.model;

public enum CrmModelType {
    BASE("Base Model"), COLLECTION("Collection"), INNER("Inner");
    private String name;
    CrmModelType(String name){
        this.name = name;
    }

    public String value(){
        return this.name;
    }
}
