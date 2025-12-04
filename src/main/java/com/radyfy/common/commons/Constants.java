package com.radyfy.common.commons;

import java.util.Set;

public class Constants {

    public final static String RADYFY_ACCOUNT_ID = "65e23e399013eda2edcd3f2e";

    public final static String ACCOUNT = "account";
    public final static String ACCOUNT_ID = "accountId";
    public final static String ECOM_ACCOUNT_ID = "ecomAccountId";

    public final static String dateFormatNative = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public final static String dateFormatFe = "yyyy/MM/DD";

    public final static String monthFormat = "yyyy/MM";
    public final static String dateFormat = "yyyy/MM/dd";

    public final static String ACCESS_DENIED = "You don't have access. Please contact admin!";
    public final static String ACCOUNT_ALREADY_REGISTERED = "Account already registered";
    public final static String EMAIL_ALREADY_REGISTERED = "Email already exists";
    public final static String CREDENTIALS_NOT_VALID = "Credentials not valid";
    public final static String SUCCESSFULLY_UPDATED = "Updated successfully";
    public final static String SUCCESSFULLY_DELETED = "Deleted successfully";
    public final static String USER_NOT_FOUND = "User not found";
    public final static String NOT_FOUND = " not found";
    public final static String NOT_VALID = " not valid";
    public final static String ALREADY_EXIST = " already exist";
    public final static String INTERNAL_ERROR = " Internal Error";
    public final static String INVALID_API_ACTION = "Invalid api action";
    public final static String API_GRID_NOT_PRESENT = "API GRID NOT PRESENT";
    public final static String API_NOT_FOUND = "NOT FOUND";
    public final static String INVALID_SERVER_VALUE = "Invalid server value";
    public final static String INVALID_FIXED_VALUE = "Invalid fixed value";
    public final static String INVALID_FILTER_VALUE = "Invalid filter value";

    // Fast membership checks
    public static final Set<String> RADYFY_META_COLLECTIONS =
            Set.of("crm_grid", "crm_api", "crm_model", "app_menu", "account_tag", "account_user_access");
    
    public final static String RADYFY_SUPPORT_API_KEY_HEADER = "x-radyfy-support-api-key";
}
