package com.radyfy.common.controller.crm;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/io/crm")
public class AuthenticationController {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

  @RequestMapping(value = "/auth", method = RequestMethod.GET)
  public Document auth() {
    return new Document("auth", true);
  }
}
