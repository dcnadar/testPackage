package com.radyfy.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlTemplateUtils {
    private static final Logger logger = LoggerFactory.getLogger(HtmlTemplateUtils.class);

    public static String loadHtmlTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            logger.error("Failed to load email template: " + templatePath, e);
            throw new RuntimeException("Failed to load email template", e);
        }
    }
    
}
