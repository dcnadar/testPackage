package com.radyfy.common.model.account;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDBCredentials implements Serializable{
  private AccountEnvDBCredentials dev;
  private AccountEnvDBCredentials qa;
  private AccountEnvDBCredentials uat;
  private AccountEnvDBCredentials prod;
}
