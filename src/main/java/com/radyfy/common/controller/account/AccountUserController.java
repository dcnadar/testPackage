package com.radyfy.common.controller.account;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.service.account.AccountUserService;

@RestController
@RequestMapping("/api/io/crm/account/user")
public class AccountUserController {

  private final AccountUserService accountUserService;

  public AccountUserController(AccountUserService accountUserService) {
    this.accountUserService = accountUserService;
  }

  @RequestMapping(value = "/search/emails", method = RequestMethod.GET)
  public List<Option> visitorSearchByNumber(
      @RequestParam(required = false) String email) {
    return accountUserService.searchUserEmails(email);
  }

  @RequestMapping(value = "/form/values", method = RequestMethod.GET)
  public List<Option> visitorValues(
      @RequestParam String id) {
    return accountUserService.userFormValues(id);
  }

}
