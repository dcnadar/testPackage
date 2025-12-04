package com.radyfy.common.config.mongo.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.radyfy.common.config.mongo.MultitenancySettings;
import com.radyfy.common.exception.NoAccountBoundException;
import com.radyfy.common.model.Account;
import com.radyfy.common.model.commons.MongoDBCredential;
import com.radyfy.common.model.enums.Environment;
import com.radyfy.common.service.AccountService;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.utils.Utils;

@Service
public class EntityMongoDBResolver {

  @Value("${spring.data.mongodb.uri}")
  private String defaultDatabaseUri;

  private final CurrentUserSession currentUserSession;

  public EntityMongoDBResolver(
      CurrentUserSession currentUserSession,
      MultitenancySettings settings,
      AccountService accountService) {
    this.currentUserSession = currentUserSession;
  }

  public MongoDBCredential resolve() {
    if (currentUserSession.getAccount() != null && currentUserSession.getEcomAccount() != null
        && currentUserSession.getRequestSession() != null) {
      Environment environment = currentUserSession.getRequestSession().getEnvironment();
      Account account = currentUserSession.getAccount();
      if (account.getDbCredentials() != null) {
        MongoDBCredential mongoDBEntity = null;
        switch (environment) {
          case PROD:
            mongoDBEntity = account.getDbCredentials().getProd().getMongoDBEntity();
            break;
          case QA:
            mongoDBEntity = account.getDbCredentials().getQa().getMongoDBEntity();
            break;
          case UAT:
            mongoDBEntity = account.getDbCredentials().getUat().getMongoDBEntity();
            break;
          case DEV:
            mongoDBEntity = account.getDbCredentials().getDev().getMongoDBEntity();
        }
        if (mongoDBEntity != null) {
          if (Utils.isTrue(mongoDBEntity.getUseDefaultCluster())) {
            return new MongoDBCredential(defaultDatabaseUri, mongoDBEntity.getDbName());
          } else {
            if (Utils.isNotEmpty(mongoDBEntity.getURI())) {
              return new MongoDBCredential(mongoDBEntity.getURI(), mongoDBEntity.getDbName());
            }
          }
        }
      }
    }
    throw new NoAccountBoundException("Entity MongoDB Credential not found");
  }
}
