package com.radyfy.common.config.mongo;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import com.radyfy.common.auth.RequestSession;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.service.CurrentUserSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CollectionMongoTemplateFactory {

  private final MongoTemplate metaMongoTemplate;
  private final MongoTemplate entityMongoTemplate;
  private final MongoTemplate radyfyAccountEntityMongoTemplate;
  private final ObjectProvider<CurrentUserSession> currentUserSessionObjectProvider;

  public CollectionMongoTemplateFactory(
      @Qualifier("metaMongoTemplate") MongoTemplate metaMongoTemplate,
      @Qualifier("entityMongoTemplate") MongoTemplate entityMongoTemplate,
      @Qualifier("radyfyAccountEntityMongoTemplate") MongoTemplate radyfyAccountEntityMongoTemplate,
      ObjectProvider<CurrentUserSession> currentUserSessionObjectProvider) {
    this.metaMongoTemplate = metaMongoTemplate;
    this.entityMongoTemplate = entityMongoTemplate;
    this.radyfyAccountEntityMongoTemplate = radyfyAccountEntityMongoTemplate;
    this.currentUserSessionObjectProvider = currentUserSessionObjectProvider;
  }

  /** Return the appropriate MongoTemplate for the given collection name. */
  public MongoTemplate forCollection(String collectionName) {
    // if (collectionName == null || collectionName.isBlank()) {
    // return entityMongoTemplate; // sensible default
    // }

    String accountId = (currentUserSessionObjectProvider.getObject() != null)
        ? currentUserSessionObjectProvider.getObject().getAccountIdOrNull()
        : null;

    boolean isRadyfyAccount = Constants.RADYFY_ACCOUNT_ID.equals(accountId);
    // if (isRadyfyAccount) {

    // if (Constants.RADYFY_META_COLLECTIONS.contains(collectionName)) {
    // return metaMongoTemplate;
    // }
    // // else {
    // // if (session != null && session.getUserSession() != null &&
    // // session.getUserSession().getFeFilters() != null &&
    // // session.getUserSession().getFeFilters().containsKey(Constants.ACCOUNT_ID)) {
    // // throw new RuntimeException("Radyfy account is not allowed to access this collection");
    // // // return radyfyAccountEntityMongoTemplate;
    // // }
    // // }
    // }
    boolean isPreview = false;
    if (currentUserSessionObjectProvider.getObject() != null) {
      RequestSession req = currentUserSessionObjectProvider.getObject().getRequestSession();
      if (req != null && Boolean.TRUE.equals(req.getPreviewData())) {
        isPreview = true;
      }
    }
    if (isRadyfyAccount && isPreview) {
      return radyfyAccountEntityMongoTemplate;
    }

    return entityMongoTemplate;
  }
}
