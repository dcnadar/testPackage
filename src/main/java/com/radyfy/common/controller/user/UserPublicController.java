package com.radyfy.common.controller.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.request.ResetPasswordRequest;
import com.radyfy.common.service.utils.ResetPasswordService;

@RestController
@RequestMapping(value = "/api/public")
public class UserPublicController {

    private final ResetPasswordService emailService;

    public UserPublicController(
            ResetPasswordService emailService) {
        this.emailService = emailService;

    }

    @RequestMapping(value = "/user/forget-password", method = RequestMethod.GET)
    public void sendForgetEmail(@RequestParam String email) {
        emailService.sendForgetEmail(email);
    }

    @RequestMapping(value = "/user/reset-password", method = RequestMethod.POST)
    public void verifyAndResetPassword(
       @Valid  @RequestBody ResetPasswordRequest resetRequest) {
        emailService.verifyAndResetPassword(resetRequest);
    }

}
