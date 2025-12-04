package com.radyfy.common.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.radyfy.common.response.config.ConfigResponse;
import com.radyfy.common.service.ConfigService;

@RestController
@RequestMapping(value = "/api/public")
public class ConfigController {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

	private final ConfigService configService;

	@Autowired
	public ConfigController(ConfigService configService) {
		this.configService = configService;
	}

	@RequestMapping(value = "/config", method = RequestMethod.GET)
	public ConfigResponse config(@RequestHeader String host, HttpServletRequest request) throws IOException {
//		String referrer = request.getHeader("referer");
//		String domain = InternetDomainName.from(new URL(referrer).getHost()).topPrivateDomain().toString();
		return configService.getConfig();
	}

}