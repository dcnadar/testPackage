package com.radyfy.common.service;

import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radyfy.common.auth.UserSession;
import com.radyfy.common.commons.AccountType;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.config.jwt.JwtTokenProvider;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.Account;
import com.radyfy.common.model.EcomAccount;
import com.radyfy.common.model.enums.Environment;
import com.radyfy.common.model.user.User;
import com.radyfy.common.response.config.ConfigResponse;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.main.CrmMainConsumerProps;
import com.radyfy.common.service.crm.config.main.CrmMainConfig.Event;
import com.radyfy.common.utils.Utils;

@Component
public class ConfigService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

	private final SessionService sessionService;
	private final UserService userService;
	private final CurrentUserSession currentUserSession;
	private final HttpServletRequest httpServletRequest;
	private final JwtTokenProvider tokenProvider;
	private final ConfigBuilder configBuilder;

	@Autowired
	public ConfigService(
			SessionService sessionService,
			UserService userService,
			HttpServletRequest httpServletRequest,
			CurrentUserSession currentUserSession,
			JwtTokenProvider tokenProvider,
			ConfigBuilder configBuilder) {
		this.sessionService = sessionService;
		this.userService = userService;
		this.httpServletRequest = httpServletRequest;
		this.currentUserSession = currentUserSession;
		this.tokenProvider = tokenProvider;
		this.configBuilder = configBuilder;
	}

	public ConfigResponse getConfig() {
		ConfigResponse configResponse = new ConfigResponse();
		EcomAccount ecomAccount = this.currentUserSession.getEcomAccount();
		Account mainAccount = this.currentUserSession.getAccount();

		if (ecomAccount != null && mainAccount != null) {
			configResponse.setAccountId(mainAccount.getAId());
			configResponse.setEcomAccountId(ecomAccount.getId());
			configResponse.setAccountStatus(mainAccount.getStatus());
			configResponse.setRunWebsite(setRunWebsite(mainAccount));
			configResponse.setFavicon(ecomAccount.getFavicon());
			configResponse.setLogo(ecomAccount.getLogo());
			configResponse.setLoginImages(ecomAccount.getLoginImages());
			configResponse.setName(ecomAccount.getName());

			configResponse.setS(true);

			try {
				String jwt = Utils.getJwtFromRequest(httpServletRequest);
				/*
				 * Validating token
				 */
				if (jwt != null && tokenProvider.validateToken(jwt)) {
					String userPayload = tokenProvider.getUserFromToken(jwt);
					/*
					 * Fetching user
					 */
					Document userPayloadDoc = Document.parse(userPayload);
					Optional<User> optionalUser = userService.getUserByUserName(
							userPayloadDoc.getString("userName"));
					optionalUser.ifPresent(user -> {

						if (!mainAccount.getId().equals(userPayloadDoc.getString("accountId")) ||
								!ecomAccount.getId().equals(userPayloadDoc.getString("ecomAccountId"))) {
							throw new AuthException();
						}

						currentUserSession.setUserSession(new UserSession(user));
						/*
						* Fetching user app filters
						*/
						String filter = this.httpServletRequest.getHeader("io-filter");
						Map<String, String> feFilters = userService.validate_Sync_GetCrmFeFilters(filter);
						/*
						* Normalizing user app filters
						*/
						if (!Utils.isNotEmpty(feFilters)) {
							feFilters = user.getCrmLastFilter();
						}
						currentUserSession.getUserSession().setFeFilters(feFilters);
						/*
						* Setting user app filters
						*/
						userService.setAdminFilters(user, feFilters);
						configResponse.setLoginData(sessionService.getLoginResponse(user));
						configBuilder.getMainConfig().runConsumer(Event.ON_CONFIG_LOAD,
								new CrmMainConsumerProps(configResponse));
					});
				}
			} catch (Exception e) {
				logger.error("INCORRECT TOKEN : " + e.getMessage(), e);
			}
		} else {
			configResponse.setEd("Invalid Request");
		}
		return configResponse;
	}

	private boolean setRunWebsite(Account account) {

		boolean isRunWebsite = false;

		String status = account.getStatus();
		Environment environment = currentUserSession.getRequestSession().getEnvironment();

		if (status != null) {

			if (status.equals(AccountType.Status.LIVE)) {

				return true;

			}

			if (environment != null) {

				if (status.equals(AccountType.Status.UNDER_MAINTENANCE)) {
					if (!environment.equals(Environment.PROD)) {
						isRunWebsite = true;
					}
				}

				else if (status.equals(AccountType.Status.UNDER_DEVELOPMENT)) {
					if (environment.equals(Environment.DEV)) {

						isRunWebsite = true;

					}

				} else if (status.equals(AccountType.Status.UNDER_REVIEW_AND_ACCEPTANCE)) {

					if (environment.equals(Environment.DEV)
							|| environment.equals(Environment.QA)
							|| environment.equals(Environment.UAT)) {
						isRunWebsite = true;
					}
				}

			}
		}

		return isRunWebsite;

	}
}