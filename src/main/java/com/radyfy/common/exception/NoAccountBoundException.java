package com.radyfy.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoAccountBoundException extends RuntimeException {
  public NoAccountBoundException() { super("No account bound to request"); }
  public NoAccountBoundException(String message) { super(message); }
}