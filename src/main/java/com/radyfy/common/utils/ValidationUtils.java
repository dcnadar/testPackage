package com.radyfy.common.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern HEX_ID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

    public static boolean isValidHexID(String id) {
        if(Utils.isNotEmpty(id)){
            return HEX_ID_PATTERN.matcher(id).matches();
        }
        return false;
    }
    
    public static boolean isValidEmail(String email) {
        if(Utils.isNotEmpty(email)){
            return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        }
        return false;
    }
}
