package com.radyfy.common.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class AdminLoginRequest {

    @Size(message = "Please enter valid user name", min = 1, max = 100)
    @NotEmpty(message = "Please provide user name")
    private String userName;

    @Size(min = 6, max = 100, message = "Please enter valid length of password")
    @NotEmpty(message = "Please provide valid password")
    private String secret;

}