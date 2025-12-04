package com.radyfy.common.model.crm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.BaseEntityModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrmBasicModel extends BaseEntityModel {

    private String name;
    private CrmModelType modelType;
    // private ModelStatus modelStatus;

    // public enum ModelStatus {
    //     ACTIVE("Active"), DISABLED("Disabled");

    //     private String name;
    //     ModelStatus(String name){
    //         this.name = name;
    //     }

    //     public String value(){
    //         return this.name;
    //     }
    // }
}
