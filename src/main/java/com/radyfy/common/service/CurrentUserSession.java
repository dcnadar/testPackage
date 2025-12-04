package com.radyfy.common.service;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.radyfy.common.auth.AccountSession;
import com.radyfy.common.auth.RequestSession;
import com.radyfy.common.auth.UserSession;
import com.radyfy.common.model.Account;
import com.radyfy.common.model.App;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.user.User;

@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS) // explicit (the default is already TARGET_CLASS)
public class CurrentUserSession {

    private UserSession userSession;
    private AccountSession accountSession;
    private RequestSession requestSession;

    public void setUserSession(UserSession userSession){ this.userSession = userSession; }
    public void setAccountSession(AccountSession accountSession){ this.accountSession = accountSession; }
    public void setRequestSession(RequestSession requestSession){ this.requestSession = requestSession; }

    // ---- Null-safe getters ----
    public User getUser() {
        return (userSession != null) ? userSession.getUser() : null;
    }

    public AccountSession getAccountSession() { return accountSession; }

    public Account getAccount() {
        return (accountSession != null) ? accountSession.getAccount() : null;
    }

    public App getApp() {
        return (accountSession != null) ? accountSession.getApp() : null;
    }

    public EcomAccount getEcomAccount() {
        return (accountSession != null) ? accountSession.getEcomAccount() : null;
    }

    public UserSession getUserSession(){ return userSession; }

    public RequestSession getRequestSession(){ return requestSession; }

    // Handy helpers you can reuse elsewhere
    public String getAccountIdOrNull() {
        Account acc = getAccount();
        return (acc != null) ? acc.getId() : null;
    }
}
