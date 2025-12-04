package com.radyfy.common.config.mongo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.radyfy.common.config.mongo.entity.EntityMongoDatabaseFactory;
import com.radyfy.common.config.mongo.radyfyaccounts.RadyfyAccountEntityMongoDatabaseFactory;

@Configuration
public class MongoConfig {
  @Value("${spring.data.mongodb.database}")
  private String metaDatabase;

  // @Value("${spring.data.mongodb.entity.database}")
  // private String entityDatabase;

  @Value("${spring.data.mongodb.uri}")
  private String metaUri;

  // @Value("${spring.data.mongodb.entity.uri}")
  // private String entityUri;

  // meta client setup
  @Bean(name = "metaMongoClient")
  public MongoClient metaMongoClient() {
    return MongoClients.create(metaUri);
  }

  @Bean(name = "metaMongoDatabaseFactory")
  @Primary
  public MongoDatabaseFactory metaMongoDatabaseFactory(
      @Qualifier("metaMongoClient") MongoClient mongoClient) {
    return new SimpleMongoClientDatabaseFactory(mongoClient, metaDatabase);
  }

  @Bean(name = "metaMongoTemplate")
  @Primary
  public MongoTemplate metaMongoTemplate(
      @Qualifier("metaMongoDatabaseFactory") MongoDatabaseFactory mongoDatabaseFactory) {
    return new MongoTemplate(mongoDatabaseFactory);
  }

  @Bean("entityMongoTemplate")
  public MongoTemplate entityMongoTemplate(EntityMongoDatabaseFactory routingFactory) {
    return new MongoTemplate(routingFactory);
  }

  @Bean("radyfyAccountEntityMongoTemplate")
  public MongoTemplate radyfyAccountEntityMongoTemplate(RadyfyAccountEntityMongoDatabaseFactory routingFactory) {
    return new MongoTemplate(routingFactory);
  }

  @Bean
  public MongoTransactionManager transactionManager(MongoDatabaseFactory metaMongoDatabaseFactory) {
    return new MongoTransactionManager(metaMongoDatabaseFactory);
  }

}
