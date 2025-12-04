package com.radyfy.common.utils;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.StringUtils;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.model.crm.grid.GridRequestParams;

import javax.script.*;
import jakarta.servlet.http.HttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Base64;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static boolean isNotEmpty(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static boolean isNotEmptyOrNull(String s) {
        return s != null && !s.trim().equals("null") && s.trim().length() > 0;
    }

    public static boolean isNotEmpty(Integer input) {
        return input != null;
    }

    public static boolean isNotEmpty(Double input) {
        return input != null;
    }

    public static boolean isNotEmpty(Boolean input) {
        return input != null;
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNotEmpty(Object o) {
        return o != null;
    }

    public static boolean isTrue(Boolean o) {
        return o != null && o;
    }

    public static GridRequestParams getGridParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        // Convert parameterMap to Map<String, String>
        GridRequestParams paramMap = new GridRequestParams();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();
            if (paramValues.length > 0) {
                paramMap.put(paramName, paramValues[0]);
            }
        }
        return paramMap;
    }

    public String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    // public static Map<String, String> getCrmFilters(
    // String filter,
    // CrmModelService crmModelService,
    // CrmAccountOrmDao crmAccountOrmDao
    // ) {
    // Map<String, String> feFilters = null;
    // // Map<String, Document> filterDocuments = null;
    // if (Utils.isNotEmpty(filter)) {
    // Gson gson = new Gson();
    // Type type = new TypeToken<Map<String, String>>() {
    // }.getType();
    // feFilters = gson.fromJson(filter, type);
    // if (Utils.isNotEmpty(feFilters)) {
    // // filterDocuments = new HashMap<>();
    // List<BaseCrmModel> baseModels = crmModelService.getBaseModels();
    // for (BaseCrmModel baseModel : baseModels) {
    // if (!(feFilters.containsKey("appId") ||
    // feFilters.containsKey("ecomAccountId"))) {
    // if (feFilters.containsKey(baseModel.getFieldName())) {
    // CrmModel crmModel = crmModelService.getModel(baseModel.getModelId());
    // Document data =
    // crmAccountOrmDao.getById(feFilters.get(baseModel.getFieldName()), crmModel,
    // null);
    // // filterDocuments.put(data.getObjectId("_id").toString(), data);
    // if (!Utils.isNotEmpty(data)) {
    // return null;
    // }
    // } else {
    // return null;
    // }
    // }
    // }
    // }
    // }
    // return feFilters;
    // }

    public static String getJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null) {
            return null;
        }

        header = header.trim();
        int spaceIndex = header.indexOf(' ');
        if (spaceIndex <= 0) {
            return null;
        }

        String scheme = header.substring(0, spaceIndex).trim();
        String credentials = header.substring(spaceIndex + 1).trim();
        if (credentials.isEmpty()) {
            return null;
        }

        if ("Bearer".equalsIgnoreCase(scheme) || "Basic".equalsIgnoreCase(scheme)) {
            return isLikelyJwt(credentials) ? credentials : null;
        }

        // Ignore Basic and any other auth schemes for JWT extraction
        return null;
    }

    private static boolean isLikelyJwt(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            // JWT uses base64url without padding
            if (!part.matches("^[A-Za-z0-9_-]+$")) {
                return false;
            }
            try {
                // Validate decodability using URL-safe decoder
                Base64.getUrlDecoder().decode(part);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    public static String getCurrencySymbol(String countryCode) {
        if ("IN".equals(countryCode)) {
            return "₹";
        }
        return "$";
    }

    private static String buildInFunctions = "function resolveProp(source, path, defaultValue) {  var parts = String(path).split('.');  var prop = parts.shift();  var result = source;  while (prop) {    if (result) {      if (isUndefined(result[prop]) || isNull(result[prop])) {        return defaultValue;      } else {        result = result[prop];      }    };    prop = parts.shift();  };  return result;};"
            +
            "function isUndefined(value) {  return value === undefined;};" +
            "function isNull(value) {  return value === null;};";

    private static volatile Boolean javascriptEngineAvailable = null;

    private static boolean isJavaScriptEngineAvailable() {
        if (javascriptEngineAvailable == null) {
            synchronized (Utils.class) {
                if (javascriptEngineAvailable == null) {
                    try {
                        ScriptEngineManager manager = new ScriptEngineManager();
                        ScriptEngine engine = manager.getEngineByName("JavaScript");
                        javascriptEngineAvailable = (engine != null);
                        if (!javascriptEngineAvailable) {
                            logger.warn(
                                    "JavaScript engine is not available. Show conditions will use default behavior.");
                        }
                    } catch (Exception e) {
                        logger.warn("Error checking JavaScript engine availability: {}", e.getMessage());
                        javascriptEngineAvailable = false;
                    }
                }
            }
        }
        return javascriptEngineAvailable;
    }

    /**
     * Evaluates a JavaScript condition function to determine if a form field should
     * be shown.
     * 
     * This method uses the JavaScript engine to evaluate dynamic show/hide
     * conditions for form fields.
     * If the JavaScript engine is not available (e.g., in Java 15+ where Nashorn
     * was removed),
     * it will return true as a fallback behavior to ensure the field is shown by
     * default.
     * 
     * @param strFunction The JavaScript function string to evaluate
     * @param thisContext The current document context for the evaluation
     * @param rootDoc     The root document containing all form data
     * @param parentPath  The parent path for the form field
     * @return true if the field should be shown, false if it should be hidden
     * @throws RuntimeException if there's an error evaluating the JavaScript
     *                          function
     */
    public static Boolean getScriptBooleanValue(String strFunction, Document thisContext, Document rootDoc,
            String parentPath) {
        try {

            Invocable inv = getInvocable(strFunction, thisContext, rootDoc, parentPath);
            return (Boolean) inv.invokeFunction("myFunction");
        } catch (ScriptException | NoSuchMethodException e) {
            logger.error("Failed to run string function: {}", strFunction, e);
            throw new RuntimeException("Failed to run string function", e);
        }
    }

    public static String getScriptStringValue(String strFunction, Document thisContext, Document rootDoc,
            String parentPath) {
        try {

            Invocable inv = getInvocable(strFunction, thisContext, rootDoc, parentPath);
            return (String) inv.invokeFunction("myFunction");
        } catch (ScriptException | NoSuchMethodException e) {
            logger.error("Failed to run string function: {}", strFunction, e);
            throw new RuntimeException("Failed to run string function", e);
        }
    }

    private static Invocable getInvocable(String strFunction, Document thisContext, Document rootDoc, String parentPath)
            throws ScriptException {
        // Check if JavaScript engine is available first
        if (!isJavaScriptEngineAvailable()) {
            logger.debug("JavaScript engine not available, returning default value true for show condition: {}",
                    strFunction);
            throw new RuntimeException("JavaScript engine not available");
        }

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        // Double-check if JavaScript engine is available
        if (engine == null) {
            logger.warn("JavaScript engine is not available. Returning default value true for show condition.");
            throw new RuntimeException("JavaScript engine not available");
        }

        Document thsiDocumentClone = Document.parse(thisContext.toJson());
        thsiDocumentClone.put("$root", rootDoc);
        thsiDocumentClone.put("$formName", parentPath);

        String script = buildInFunctions + "function myFunction() { \n" +
                "   return (new Function(\"" +
                strFunction + "\").call(" +
                thsiDocumentClone.toJson() +
                "    ));" +
                "}";
        logger.debug("<<<<<RUNNING SCRIPT>>>>> {}", script);
        // evaluate script
        engine.eval(script);
        // invoke the function
        return (Invocable) engine;
    }

    public static String toUrlPathValue(String name) {
        return name.toLowerCase().replaceAll("/\\s+/", "-");
    }

    public static MediaType getMediaType(String fileName) {
        String fileExtension = StringUtils.getFilenameExtension(fileName);
        if (StringUtils.hasText(fileExtension)) {
            return MediaTypeFactory.getMediaType(fileName)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    public static Date parseDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof String) {
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.dateFormatNative, Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                return formatter.parse((String) value);
            } catch (Exception e) {
                logger.error("Unable to parse date: " + value + ", " + e.getMessage(), e);
            }
        } else if (value instanceof Long) {
            return new Date((long) value);
        }
        return null;
    }
}
