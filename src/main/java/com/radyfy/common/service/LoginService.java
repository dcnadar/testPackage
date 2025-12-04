package com.radyfy.common.service;

import org.springframework.stereotype.Component;
import com.radyfy.common.auth.AccountSession;
import com.radyfy.common.auth.PasswordHash;
import com.radyfy.common.auth.UserSession;
import com.radyfy.common.config.jwt.JwtTokenProvider;
import com.radyfy.common.model.enums.Environment;
import com.radyfy.common.model.enums.UserStatus;
import com.radyfy.common.model.user.User;
import com.radyfy.common.request.AdminLoginRequest;
import com.radyfy.common.response.config.LoginResponse;
import com.radyfy.common.service.aws.sqs.SqsMessageProducer;
import com.radyfy.common.service.utils.ResetPasswordService;

import java.util.Optional;
import org.bson.Document;

@Component
public class LoginService {

    private final UserService userService;
    private final SessionService sessionService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordHash passwordHash;
    private final CurrentUserSession currentUserSession;
    private final ResetPasswordService emailService;
    private final SqsMessageProducer sqsMessageProducer;

    public LoginService(UserService userService, SessionService sessionService,
            JwtTokenProvider tokenProvider, PasswordHash passwordHash,
            CurrentUserSession currentUserSession, ResetPasswordService emailService,
            SqsMessageProducer sqsMessageProducer

    ) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordHash = passwordHash;
        this.tokenProvider = tokenProvider;
        this.currentUserSession = currentUserSession;
        this.emailService = emailService;
        this.sqsMessageProducer = sqsMessageProducer;
    }

    public LoginResponse loginUser(AdminLoginRequest loginRequest) {

        Optional<User> optionalUser =
                userService.getUserByUserName(loginRequest.getUserName().toLowerCase());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!UserStatus.ACTIVE.toString().equals(user.getStatus())
                    && !UserStatus.INVITED.toString().equals(user.getStatus())) {
                throw new RuntimeException("User is not active");
            }
            // matching password
            if (passwordHash.hashPassword(loginRequest.getSecret()).equals(user.getPassword())) {

                String requestId = null;
                if (user.isFirstLogin()) {
                    Document forgetEmailRequest = emailService.sendUserForgetEmail(user.getEmail());
                    requestId = forgetEmailRequest.getString("requestId");
                }

                LoginResponse loginResponse = null;
                if (requestId == null) {

                    if (UserStatus.INVITED.toString().equals(user.getStatus())) {
                        userService.activateUser(user.getId());
                    }

                    AccountSession accountSession = currentUserSession.getAccountSession();

                    String token = tokenProvider.generateToken(user, accountSession.getAccount().getId(), accountSession.getEcomAccount().getId());

                    currentUserSession.setUserSession(new UserSession(user));
                    currentUserSession.getUserSession().setFeFilters(user.getCrmLastFilter());
                    userService.setAdminFilters(user, user.getCrmLastFilter());
                    loginResponse = sessionService.getLoginResponse(user);
                    loginResponse.setLoginToken(token);

                    Environment env = currentUserSession.getRequestSession().getEnvironment();
                    if (env != Environment.PROD) {
                        sqsMessageProducer.sendLoginEvent(user, accountSession.getAccount().getId(), env);
                    }

                } else {
                    loginResponse = new LoginResponse(user);
                    loginResponse.setRequestId(requestId);
                    loginResponse.setS(true);
                }
                return loginResponse;
            } else {
                throw new RuntimeException("Email/Password is not valid");
            }
        } else {
            throw new RuntimeException("User Not found");
        }
    }
}
