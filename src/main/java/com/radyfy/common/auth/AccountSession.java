package com.radyfy.common.auth;

import com.radyfy.common.model.Account;
import com.radyfy.common.model.App;
import com.radyfy.common.model.EcomAccount;

import lombok.Getter;

@Getter
public class AccountSession {
    final private Account account;
    final private App app;
    final private EcomAccount ecomAccount;

    public AccountSession(Account account, EcomAccount ecomAccount, App app){
        this.account = account;
        this.app = app;
        this.ecomAccount = ecomAccount;
    }
}
