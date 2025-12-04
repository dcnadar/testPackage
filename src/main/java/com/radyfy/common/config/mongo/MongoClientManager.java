package com.radyfy.common.config.mongo;

import com.github.benmanes.caffeine.cache.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MongoClientManager {
  private final LoadingCache<String, MongoClient> clients;

  public MongoClientManager(MultitenancySettings settings) {
    int maxPool = settings.getCache().getClientMaxSize();
    this.clients = Caffeine.newBuilder()
      .expireAfterAccess(60, TimeUnit.MINUTES)  // adjust if you expect many URIs
      .removalListener((String uri, MongoClient client, RemovalCause cause) -> {
        if (client != null) client.close();
      })
      .build(uri -> {
        var cs = new ConnectionString(uri);
        var opts = MongoClientSettings.builder()
          .applyConnectionString(cs)
          .applyToConnectionPoolSettings(b -> b.maxSize(maxPool))
          .build();
        return MongoClients.create(opts);
      });
  }

  public MongoClient get(String uri) { return clients.get(uri); }
}