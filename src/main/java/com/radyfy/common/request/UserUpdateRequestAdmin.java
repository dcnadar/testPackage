package com.radyfy.common.request;

import java.util.ArrayList;
import java.util.List;

public class UserUpdateRequestAdmin {

    private boolean admin;

    private String firstName;

    private String lastName;

    private String password;

    private List<AccountAccessRequest> access = new ArrayList<>();

    public List<AccountAccessRequest> getAccess() {
        return access;
    }

    public void setAccess(List<AccountAccessRequest> access) {
        this.access = access;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}