package com.radyfy.common.auth;

import lombok.Getter;
import org.bson.Document;

import com.radyfy.common.model.user.User;

import java.util.Map;

@Getter
public class UserSession {

    final private User user;
    private Map<String, String> feFilters;
    private Map<String, Document> filterDocuments;

    public UserSession(User user) {
        this.user = user;
    }

    public void setFeFilters(Map<String, String> feFilters) {
        this.feFilters = feFilters;
    }

    public void setFilterDocuments(Map<String, Document> filterDocuments) {
        this.filterDocuments = filterDocuments;
    }
}
