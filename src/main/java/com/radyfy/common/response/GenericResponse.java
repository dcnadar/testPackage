package com.radyfy.common.response;

import lombok.Getter;
import lombok.Setter;

import org.bson.Document;

@Getter
@Setter
public class GenericResponse {

    private String msg;
    private String ed;

    private boolean s;
    private Document meta;

    public GenericResponse(){
        super();
    }
    public GenericResponse(String msg){
        super();
        this.msg = msg;
    }

}