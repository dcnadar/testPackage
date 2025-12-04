package com.radyfy.common.response.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.App;
import com.radyfy.common.response.GenericResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigResponse extends GenericResponse {

    private String name;
    private String logo;
    private List<String> loginImages;
    private String favicon;
    private LoginResponse loginData;
    private String accountId;
    private String ecomAccountId;
    private String accountStatus;
    private boolean runWebsite;
}
