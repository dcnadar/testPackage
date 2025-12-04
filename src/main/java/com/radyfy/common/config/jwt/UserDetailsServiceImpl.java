package com.radyfy.common.config.jwt;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.radyfy.common.auth.AccountSession;
import com.radyfy.common.auth.UserSession;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.user.User;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserService userService;
    private final CurrentUserSession currentUserSession;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public UserDetailsServiceImpl(
            UserService userService,
            CurrentUserSession currentUserSession,
            HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.currentUserSession = currentUserSession;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public UserDetails loadUserByUsername(String userPayload) throws UsernameNotFoundException {
        AccountSession accountSession = currentUserSession.getAccountSession();
        Document userPayloadDoc = Document.parse(userPayload);
        User user = userService
                .getUserByUserName(userPayloadDoc.getString("userName"))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with userName: " + userPayloadDoc.getString("userName")));
        if (!accountSession.getAccount().getId().equals(userPayloadDoc.getString("accountId")) ||
                !accountSession.getEcomAccount().getId().equals(userPayloadDoc.getString("ecomAccountId"))) {
            throw new AuthException();
        }
        // user session should be set before to fetch fe filter data
        currentUserSession.setUserSession(new UserSession(user));

        userService.validate_Sync_GetCrmFeFilters(this.httpServletRequest.getHeader("io-filter"));

        return UserDetailsImpl.build(user, accountSession.getAccount().getId());
    }
}
