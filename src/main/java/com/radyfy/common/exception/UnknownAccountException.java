package com.radyfy.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnknownAccountException extends RuntimeException {
  public UnknownAccountException(String id) { super("Unknown or inactive account: " + id); }
}