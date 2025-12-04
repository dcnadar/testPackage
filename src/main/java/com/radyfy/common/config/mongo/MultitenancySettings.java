package com.radyfy.common.config.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "multitenancy")
public class MultitenancySettings {
  private String defaultTenant = "defaultTenant";
  private Cache cache = new Cache();

  public String getDefaultTenant() { return defaultTenant; }
  public void setDefaultTenant(String defaultTenant) { this.defaultTenant = defaultTenant; }
  public Cache getCache() { return cache; }
  public void setCache(Cache cache) { this.cache = cache; }

  public static class Cache {
    private int tenantTtlMinutes = 10;
    private int clientMaxSize = 50;
    public int getTenantTtlMinutes() { return tenantTtlMinutes; }
    public void setTenantTtlMinutes(int v) { tenantTtlMinutes = v; }
    public int getClientMaxSize() { return clientMaxSize; }
    public void setClientMaxSize(int v) { clientMaxSize = v; }
  }
}
