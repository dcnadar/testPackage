package com.radyfy.common.model.account;

import java.io.Serializable;

import com.radyfy.common.model.commons.MongoDBCredential;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountEnvDBCredentials implements Serializable{
  private MongoDBCredential mongoDBEntity;
}
