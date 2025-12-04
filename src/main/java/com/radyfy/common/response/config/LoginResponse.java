package com.radyfy.common.response.config;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.crm.menu.AppMenu;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.model.enums.Gender;
import com.radyfy.common.model.user.User;
import com.radyfy.common.response.GenericResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse extends GenericResponse {

    private String email;
    private String firstName;
    private String lastName;
    private String userId;
    private String profile;
    private String mobileNumber;
    private String callingCode;
    private String salutation;
    private String middleName;
    private Gender gender;
    private Date birthDate;
    private Boolean admin;
    private String userType;
    private String roleId;
    private String status;
    private String loginToken;
    private AppMenu appMenu;
    private FormGroup[] topFilters;
    private String requestId;
    private boolean isFirstLogin;

    public LoginResponse(
            User user) {
        super();
        this.email = user.getDocument().getString("email");
        this.userId = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.profile = user.getProfile();
        this.mobileNumber = user.getMobileNumber();
        this.callingCode = user.getCallingCode();
        this.salutation = user.getSalutation();
        this.middleName = user.getMiddleName();
        this.status = user.getStatus();
        this.isFirstLogin = user.isFirstLogin();
    }
}