package com.radyfy.common.config.mongo;

import com.radyfy.common.model.user.User;
import com.radyfy.common.utils.Utils;

import java.util.Map;

import org.bson.Document;

public class UserReadConverter {

    public User convert(Document source) {
        User user = new User();

        user.setEmail(source.getString("email"));
        user.setProfile(source.getString("profile"));
        user.setMobileNumber(source.getString("mobileNumber"));
        user.setCallingCode(source.getString("callingCode"));
        user.setUserName(source.getString("userName"));
        user.setSalutation(source.getString("salutation"));
        user.setFirstName(source.getString("firstName"));
        user.setMiddleName(source.getString("middleName"));
        user.setLastName(source.getString("lastName"));
        user.setPassword(source.getString("password"));
        user.setLastLoginIP(source.getString("lastLoginIP"));
        user.setLastLogin(Utils.parseDate(source.get("lastLogin")));
        user.setStatus(source.getString("status"));
        user.setCrmLastFilter(source.get("crmLastFilter", Map.class));

        user.setId(source.getString("_id"));
        user.setCreated(Utils.parseDate(source.get("created")));
        user.setFirstLogin(source.getBoolean("isFirstLogin", false));
        user.setUserGroupId(source.getString("userGroupId"));
        user.setAppRoleId(source.getString("appRoleId"));

        user.setDocument(source);

        return user;
    }
}
