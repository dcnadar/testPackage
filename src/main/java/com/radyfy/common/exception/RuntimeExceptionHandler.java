package com.radyfy.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.radyfy.common.response.GenericResponse;
import io.sentry.Sentry;

@ControllerAdvice
public class RuntimeExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<GenericResponse> handleRuntimeException(RuntimeException ex) {
        logger.error(ex.getMessage(), ex);
        Sentry.captureException(ex);
        if(ex instanceof AuthException)
            return new ResponseEntity<>(new GenericResponse("Unauthorized"), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(new GenericResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}