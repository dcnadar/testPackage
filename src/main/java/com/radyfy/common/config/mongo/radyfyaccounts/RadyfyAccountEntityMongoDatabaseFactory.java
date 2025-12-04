package com.radyfy.common.config.mongo.radyfyaccounts;

import com.mongodb.client.ClientSession;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.radyfy.common.config.mongo.MongoClientManager;
import com.radyfy.common.model.commons.MongoDBCredential;

import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;
import org.springframework.stereotype.Component;

@Component
public class RadyfyAccountEntityMongoDatabaseFactory implements MongoDatabaseFactory {

  private final RadyfyAccountEntityMongoDBResolver resolver;
  private final MongoClientManager clients;

  // share a translator instance
  private final PersistenceExceptionTranslator exceptionTranslator = new MongoExceptionTranslator();

  public RadyfyAccountEntityMongoDatabaseFactory(RadyfyAccountEntityMongoDBResolver resolver,
                                     MongoClientManager clients) {
    this.resolver = resolver;
    this.clients = clients;
  }

  // ----- helpers

  private MongoDBCredential currentConn() {
    return resolver.resolve();
  }

  private MongoClient currentClient() {
    var tc = currentConn();
    return clients.get(tc.getURI());
  }

  private String currentDbName() {
    return currentConn().getDbName();
  }

  // ----- MongoDatabaseFactory implementation

  @Override
  public MongoDatabase getMongoDatabase() {
    return currentClient().getDatabase(currentDbName());
  }

  @Override
  public MongoDatabase getMongoDatabase(String dbName) {
    return currentClient().getDatabase(dbName);
  }

  @Override
  public ClientSession getSession(ClientSessionOptions options) {
    ClientSessionOptions opts = (options != null) ? options : ClientSessionOptions.builder().build();
    return currentClient().startSession(opts);
  }

  @Override
  public MongoDatabaseFactory withSession(ClientSession session) {
    RadyfyAccountEntityMongoDatabaseFactory outer = this;

    return new MongoDatabaseFactory() {
      @Override
      public MongoDatabase getMongoDatabase() {
        return outer.getMongoDatabase();
      }

      @Override
      public MongoDatabase getMongoDatabase(String dbName) {
        return outer.getMongoDatabase(dbName);
      }

      @Override
      public ClientSession getSession(ClientSessionOptions options) {
        return session; // reuse provided session
      }

      @Override
      public MongoDatabaseFactory withSession(ClientSession s) {
        return (s == session) ? this : outer.withSession(s);
      }

      @Override
      public PersistenceExceptionTranslator getExceptionTranslator() {
        return outer.getExceptionTranslator();
      }
    };
  }

  @Override
  public PersistenceExceptionTranslator getExceptionTranslator() {
    return exceptionTranslator;
  }

  // NOTE: helper (not part of interface) — do NOT annotate with @Override
  // public MongoClient getCurrentMongoClient() {
  //   return currentClient();
  // }
}
