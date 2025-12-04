package com.radyfy.common.commons;

import java.util.Arrays;
import java.util.List;

public class Api {

    private static final String baseURL = "/api/io";
    public static final String radyfyBaseURL = "/api/radyfy";
    public static final String crmBaseURL = baseURL + "/crm";
    public static final String crmSlugBaseURL = crmBaseURL + "/slug";
    public static final String crmModelSearchBaseApi = crmBaseURL + "/search/model";
    // ATTENDANCE
    public static final String STAFF_EXPORT_FIELDS = baseURL + "/user/export/fields";

    public static final String ADMIN_ACCOUNT = "/admin/account";
    public static final String ADMIN_USER= "/admin/user";
    public static final String ADMIN_APP_ROLE = "/admin/app-role";
    public static final String ADMIN_APP_PERMISSION = "/admin/app-permission";
    public static final String ADMIN_USER_GROUP = "/admin/user-group";


    public static final List<String> radyfyCommonApis = Arrays.asList(ADMIN_USER, ADMIN_USER_GROUP, ADMIN_APP_PERMISSION, ADMIN_APP_ROLE);

}
