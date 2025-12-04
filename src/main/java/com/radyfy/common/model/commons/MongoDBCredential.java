package com.radyfy.common.model.commons;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongoDBCredential implements Serializable{
  
  private Boolean useDefaultCluster;
  private String URI;
  private String dbName;

  public MongoDBCredential() {
    super();
  }

  public MongoDBCredential(String URI, String dbName) {
    super();
    this.URI = URI;
    this.dbName = dbName;
  }

}
