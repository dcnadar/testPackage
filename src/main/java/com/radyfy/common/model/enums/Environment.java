package com.radyfy.common.model.enums;

public enum Environment {
  DEV("Development"), QA("Under Review & Acceptance"), UAT("Pre-Production"), PROD("Production");

  private String name;

  Environment(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
}
