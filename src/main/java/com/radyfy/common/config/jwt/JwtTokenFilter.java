package com.radyfy.common.config.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.radyfy.common.auth.AccountSession;
import com.radyfy.common.auth.RequestSession;
import com.radyfy.common.model.Account;
import com.radyfy.common.model.App;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.enums.Environment;
import com.radyfy.common.service.AccountService;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.utils.Utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final AccountService accountService;
    private final CurrentUserSession currentUserSession;

    @Autowired
    public JwtTokenFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            AccountService accountService,
            CurrentUserSession currentUserSession) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.accountService = accountService;
        this.currentUserSession = currentUserSession;
    }

    private void logRequest(HttpServletRequest httpRequest) {

        StringBuilder stringBuilder = new StringBuilder();
        // Log the URL
        String url = httpRequest.getRequestURL().toString();
        stringBuilder.append("URL: ").append(url).append(" || ");

        // Log the parameters
        Enumeration<String> paramNames = httpRequest.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = httpRequest.getParameter(paramName);
            stringBuilder.append(" Parameter - ").append(paramName).append(": ").append(paramValue);
        }
        stringBuilder.append(" || ");
        // Log the headers
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = httpRequest.getHeader(headerName);
            stringBuilder.append(" Header - ").append(headerName).append(": ").append(headerValue);
        }

        logger.debug(stringBuilder.toString());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // logRequest(request);

        String domain = request.getServerName();
        String path = request.getRequestURI();
        logger.info("domain={}, path={}", domain, path);

        if (path.startsWith("/api/") 
                && !(path.startsWith("/api/public/downloadFile/")
                || path.startsWith("/api/public/health")
                || path.startsWith("/api/public/user/upload")
                || path.startsWith("/api/public/memory"))) {

            /*
             * Setting user account related data in current user session
             */

            String wildcardSubdomain = domain.split("\\.")[0];
            Environment environment = Environment.PROD;
            if(wildcardSubdomain.equals("dev")){
                environment = Environment.DEV;
                domain = domain.substring(wildcardSubdomain.length() + 1);
            } else if(wildcardSubdomain.equals("qa")){
                environment = Environment.QA;
                domain = domain.substring(wildcardSubdomain.length() + 1);
            } else if(wildcardSubdomain.equals("uat")){
                environment = Environment.UAT;
                domain = domain.substring(wildcardSubdomain.length() + 1);
            }

            EcomAccount ecomAccount = accountService.getEcomAccount(domain);
            Account account = accountService.getAccountById(ecomAccount.getAccountId());
            App app = accountService.getApp(ecomAccount);
            /*
             * Setting account Info in current user session with ecom Account and app
             */
            currentUserSession.setAccountSession(new AccountSession(account, ecomAccount, app));

            String platform = request.getHeader("x-app-platform");
            if(!Utils.isNotEmpty(platform)){
                platform = "WEB";
            }
            RequestSession requestSession = new RequestSession();
            requestSession.setEnvironment(environment);
            requestSession.setAppPlatform(RequestSession.AppPlatform.valueOf(platform.toUpperCase()));
            currentUserSession.setRequestSession(requestSession);

            /*
            * */
            if (!path.startsWith("/api/public/") && !path.startsWith("/api/io/crm/admin/public/") && !path.startsWith("/api/radyfy/public/")) {
                String jwt = Utils.getJwtFromRequest(request);

                if (jwt != null && tokenProvider.validateToken(jwt)) {
                    String userPayload = tokenProvider.getUserFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userPayload);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
