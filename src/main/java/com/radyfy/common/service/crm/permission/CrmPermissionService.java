package com.radyfy.common.service.crm.permission;

import org.springframework.beans.factory.annotation.Autowired;

import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.utils.Utils;

import org.springframework.stereotype.Service;
import java.util.List;
import org.bson.Document;

import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.model.crm.api.ApiType;
import com.radyfy.common.model.crm.api.CrmApi;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.user.User;
import com.radyfy.common.service.CurrentUserSession;

@Service
public class CrmPermissionService {

    private final CurrentUserSession currentUserSession;
    private final CrmModelService crmModelService;
    private final EntityOrmDao entityOrmDao;

    @Autowired
    public CrmPermissionService(
            CurrentUserSession currentUserSession,
            CrmModelService crmModelService,
            EntityOrmDao entityOrmDao) {
        this.currentUserSession = currentUserSession;
        this.crmModelService = crmModelService;
        this.entityOrmDao = entityOrmDao;
    }

    public boolean doCurrentUserHasAccess(CrmApi crmApi) {
        String path = crmApi.getPath();

        // Always allow public endpoints
        if (path.startsWith("/admin/public/")) {
            return true;
        }

        User user = currentUserSession.getUser();
        String roleKey = user.getAppRoleId();

        // Check if this is the radyfy account
        String accountId = currentUserSession.getAccount().getId();
        boolean isRadyfyAccount = Constants.RADYFY_ACCOUNT_ID.equals(accountId);

        if (isRadyfyAccount) {
            // For radyfy account

            if (RoleType.SUPER_ADMIN.equals(roleKey)) {
                // Allow all access
                return true;
            } else if (RoleType.SYSTEM_ADMIN.equals(roleKey)) {
                // Allow access if not starting with /admin/account
                return !path.startsWith("/admin/account");
            } else {
                // Check according to permission (existing code)
                return checkPermissionBasedAccess(user, crmApi);
            }
        } else {
            // For non-radyfy accounts

            if (RoleType.ACCOUNT_ADMIN.equals(roleKey)) {
                // Allow all access
                return true;
            } else {
                // Check according to permission (existing code)
                return checkPermissionBasedAccess(user, crmApi);
            }
        }
    }

    // Check permission-based access using existing logic

    private boolean checkPermissionBasedAccess(User user, CrmApi crmApi) {
        boolean hasDirectAccess = hasApiAccessByRole(user.getAppRoleId(), crmApi);
        if (hasDirectAccess) {
            return true;
        }

        String userGroupId = user.getUserGroupId();
        if (Utils.isNotEmpty(userGroupId)) {
            CrmModel userGroupCrmModel = crmModelService.getModelByCollectionName(CollectionNames.USER_GROUP);

            Document userGroupDoc = entityOrmDao.getById(userGroupId, userGroupCrmModel, null);
            List<String> roles = userGroupDoc.getList("roles", String.class);
            for (String roleId : roles) {
                boolean hasGroupdAccess = hasApiAccessByRole(roleId, crmApi);
                if (hasGroupdAccess) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasApiAccessByRole(String appRoleId, CrmApi crmApi) {
        String path = crmApi.getPath();
        CrmModel appPermissionModel = crmModelService.getModelByCollectionName(CollectionNames.APP_PERMISSION);
        CrmModel appRoleCrmModel = crmModelService.getModelByCollectionName(CollectionNames.APP_ROLE);

        Document appRoleDoc = entityOrmDao.getById(appRoleId, appRoleCrmModel, null);
        List<String> permissionIds = appRoleDoc.getList("permissions", String.class);
        String accessType = chekAccessType(crmApi.getApiType());

        for (String permissionId : permissionIds) {

            Document appPermissionDoc = entityOrmDao.getById(permissionId, appPermissionModel,
                    null);

            List<Document> entries = appPermissionDoc.getList("entries", Document.class);

            for (Document entry : entries) {

                if (path.startsWith(entry.getString("slug"))) {
                    if (entry.getBoolean(accessType, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String chekAccessType(ApiType apiType) {
        switch (apiType) {
            case GET:
                return "read";
            case POST:
                return "create";
            case PUT:
            case PATCH:
                return "update";
            case DELETE:
                return "delete";
            default:
                throw new RuntimeException("Invalid api type");
        }
    }

    public boolean isValidHardRole(String roleId){
        if(Constants.RADYFY_ACCOUNT_ID.equals(currentUserSession.getAccount().getId())){
            if(RoleType.SUPER_ADMIN.equals(currentUserSession.getUser().getAppRoleId())){
                return RoleType.SUPER_ADMIN.equals(roleId) || RoleType.SYSTEM_ADMIN.equals(roleId);
            } else if(RoleType.SYSTEM_ADMIN.equals(currentUserSession.getUser().getAppRoleId())){
                return RoleType.SYSTEM_ADMIN.equals(roleId);
            }
        } else {
            if(Boolean.TRUE.equals(currentUserSession.getRequestSession().getRadyfySupport())){
                return RoleType.ACCOUNT_ADMIN.equals(roleId);
            }
            else if(RoleType.ACCOUNT_ADMIN.equals(currentUserSession.getUser().getAppRoleId())){
                return RoleType.ACCOUNT_ADMIN.equals(roleId);
            }
        }
        return false;
    }

}
