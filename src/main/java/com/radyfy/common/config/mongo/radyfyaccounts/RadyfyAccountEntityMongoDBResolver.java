package com.radyfy.common.config.mongo.radyfyaccounts;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.exception.NoAccountBoundException;
import com.radyfy.common.exception.UnknownAccountException;
import com.radyfy.common.model.commons.MongoDBCredential;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.utils.BsonDocumentUtils;
import com.radyfy.common.utils.Utils;

@Service
public class RadyfyAccountEntityMongoDBResolver {

  @Value("${spring.data.mongodb.uri}")
  private String defaultDatabaseUri;

  private final CurrentUserSession currentUserSession;

  public RadyfyAccountEntityMongoDBResolver(
      CurrentUserSession currentUserSession) {
    this.currentUserSession = currentUserSession;
  }

  public MongoDBCredential resolve() {

    // System.out.println("----------START----------");
    // System.out.println(
    // "account: " + (currentUserSession.getAccount() != null ?
    // currentUserSession.getAccount().getName() : "null"));
    // System.out.println("ecomaccount: "
    // + (currentUserSession.getEcomAccount() != null ?
    // currentUserSession.getEcomAccount().getName() : "null"));
    // System.out.println(
    // "user: " + (currentUserSession.getUser() != null ?
    // currentUserSession.getUser().getFirstName() : "null"));
    // System.out.println("filters: " + (currentUserSession.getUserSession() != null
    // ? currentUserSession.getUserSession().getFeFilters()
    // : "null"));
    // System.out.println("filters documents: " +
    // (currentUserSession.getUserSession() != null ?
    // currentUserSession.getUserSession().getFilterDocuments()
    // : "null"));
    // System.out.println("----------END----------");

    if (currentUserSession.getUserSession() != null
        && currentUserSession.getUserSession().getFilterDocuments() != null) {
      Document account = currentUserSession.getUserSession().getFilterDocuments().get(Constants.ACCOUNT_ID);
      Document mongoDBEntity = (Document) BsonDocumentUtils.getDataValue(account, "dbCredentials.dev.mongoDBEntity");
      if (mongoDBEntity != null) {
        if (mongoDBEntity.getBoolean("useDefaultCluster")) {
          return new MongoDBCredential(defaultDatabaseUri, mongoDBEntity.getString("dbName"));
        }
        if (Utils.isNotEmpty(mongoDBEntity.getString("URI"))) {
          return new MongoDBCredential(mongoDBEntity.getString("URI"), mongoDBEntity.getString("dbName"));
        }
        throw new UnknownAccountException(account.getString("_id"));
      }
    }
    throw new NoAccountBoundException();
  }
}
