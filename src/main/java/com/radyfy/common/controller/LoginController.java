package com.radyfy.common.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.request.AdminLoginRequest;
import com.radyfy.common.response.GenericResponse;
import com.radyfy.common.response.config.LoginResponse;
import com.radyfy.common.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/public")
public class LoginController {

    private final LoginService loginService;


    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse login(@Valid @RequestBody AdminLoginRequest adminLoginRequest)
            throws IOException {

        return loginService.loginUser(adminLoginRequest);
    }

    @RequestMapping(value = "/login/send-otp", method = RequestMethod.GET)
    public GenericResponse setOtp() {

        GenericResponse response1 = new GenericResponse();
        response1.setS(true);
        return response1;
    }

}
