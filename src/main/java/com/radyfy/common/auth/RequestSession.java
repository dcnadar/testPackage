package com.radyfy.common.auth;

import com.radyfy.common.model.enums.Environment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSession {
  
  public enum AppPlatform {
    WEB, ANDROID, IOS
  }

  private AppPlatform appPlatform;
  private Environment environment;
  private Boolean radyfySupport;
  private Boolean previewData;
}
