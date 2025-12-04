package com.radyfy.common.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.utils.BsonDocumentUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import org.bson.Document;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    private String id;

    private Date created;
    private Date updated;

    private String email;
    private String profile;
    private String mobileNumber;
    private String callingCode;
    // userName will be combination of callingCode and mobileNumber
    private String userName;
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private Date lastLogin;
    private String password;
    private String lastLoginIP;
    private String status;
    private Map<String, String> crmLastFilter;
    private boolean isFirstLogin;
    private String appRoleId;
    private String userGroupId;

    private Document document;

    public Object getDynamicField(String key) {
        return BsonDocumentUtils.getDataValue(document, key);
    }
}