package com.radyfy.common.request;


import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "icon")
public class CrmIconRequest {

    @NotEmpty(message = "Please provide name")
    private String name;

    @NotEmpty(message = "Please provide type")
    private String type;

    @NotEmpty(message = "Please provide icon")
    private String icon;
    
}
