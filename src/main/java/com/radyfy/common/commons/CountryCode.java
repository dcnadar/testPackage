package com.radyfy.common.commons;

public enum CountryCode {
    IN("India");

    private String name;

    CountryCode(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
