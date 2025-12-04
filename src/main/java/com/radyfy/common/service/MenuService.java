package com.radyfy.common.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Api;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.model.crm.menu.AppMenu;
import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.user.User;
import com.radyfy.common.service.crm.CrmModelService;
import com.radyfy.common.service.crm.EntityOrmDao;
import com.radyfy.common.utils.Utils;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;

@Component
public class MenuService {

    private final MetaOrmDao metaOrmDao;
    private final CrmModelService crmModelService;
    private final EntityOrmDao entityOrmDao;
    private final CurrentUserSession currentUserSession;

    @Autowired
    public MenuService(MetaOrmDao metaOrmDao, CrmModelService crmModelService, EntityOrmDao entityOrmDao,
            CurrentUserSession currentUserSession) {
        this.metaOrmDao = metaOrmDao;
        this.crmModelService = crmModelService;
        this.entityOrmDao = entityOrmDao;
        this.currentUserSession = currentUserSession;
    }

    public AppMenu getAppMenu(User user) {
        if (user == null) {
            return null;
        }

        AppMenu appMenu = metaOrmDao.findOneByQuery(null, AppMenu.class);
        // if (Utils.isTrue(user.getAdmin())) {
        // return appMenu;
        // }
        if (appMenu == null) {
            return null;
        }

        // Check if this is the radyfy account
        String accountId = currentUserSession.getAccount().getId();
        boolean isRadyfyAccount = Constants.RADYFY_ACCOUNT_ID.equals(accountId);

        String roleKey = user.getAppRoleId();

        if (isRadyfyAccount) {
            // For radyfy account
            if (RoleType.SUPER_ADMIN.equals(roleKey)) {
                // Allow all menu items and bottom items
                return appMenu;
            } else if (RoleType.SYSTEM_ADMIN.equals(roleKey)) {
                // Allow all menu items and bottom items except /admin/account
                return filterMenuForSystemAdmin(appMenu);
            } else {
                // Check according to role (existing logic)
                return filterMenuByPermissions(appMenu, user);
            }
        } else {
            // For non-radyfy accounts
            if (RoleType.ACCOUNT_ADMIN.equals(roleKey)) {
                // Allow all menu items and bottom items
                return appMenu;
            } else {
                // Check according to role (existing logic)
                return filterMenuByPermissions(appMenu, user);
            }
        }
    }

    // Filter menu for system_admin role (exclude /admin/account)

    private AppMenu filterMenuForSystemAdmin(AppMenu appMenu) {
        AppMenu filteredMenu = new AppMenu();
        filteredMenu.setMenuItems(filterMenuItems(appMenu.getMenuItems(), RoleType.SYSTEM_ADMIN));
        filteredMenu.setBottomMenu(filterMenuItems(appMenu.getBottomMenu(), RoleType.SYSTEM_ADMIN));
        return filteredMenu;
    }

    // Filter menu items based on role exclusions

    private List<MenuItem> filterMenuItems(List<MenuItem> menuItems, String roleType) {
        if (!Utils.isNotEmpty(menuItems)) {
            return menuItems;
        }

        List<MenuItem> filteredItems = new ArrayList<>();
        for (MenuItem menuItem : menuItems) {
            if (menuItem.getSlug().equals("/logout")) {
                filteredItems.add(menuItem);
                continue;
            }

            if (Utils.isNotEmpty(menuItem.getSubMenu())) {
                String baseSlug = menuItem.getSlug();
                boolean anyAdded = false;
                List<MenuItem> subMenu = new ArrayList<>();

                for (MenuItem subMenuItem : menuItem.getSubMenu()) {
                    String fullPath = baseSlug + subMenuItem.getSlug();
                    if (!isExcludedForRole(fullPath, roleType)) {
                        subMenu.add(subMenuItem);
                        anyAdded = true;
                    }
                }

                if (anyAdded) {
                    MenuItem filteredMenuItem = MenuItem.builder()
                            .isSubmenu(menuItem.getIsSubmenu())
                            .icon(menuItem.getIcon())
                            .page(menuItem.getPage())
                            .slug(menuItem.getSlug())
                            .value(menuItem.getValue())
                            .key(menuItem.getKey())
                            .subMenu(subMenu)
                            .build();
                    filteredItems.add(filteredMenuItem);
                }
            } else {
                if (!isExcludedForRole(menuItem.getSlug(), roleType)) {
                    filteredItems.add(menuItem);
                }
            }
        }

        return filteredItems;
    }

    // Check if a path is excluded for a specific role

    private boolean  isExcludedForRole(String slug, String roleType) {
        if (RoleType.SYSTEM_ADMIN.equals(roleType)) {
            return Api.ADMIN_ACCOUNT.equals(slug);
        }
        return false;
    }

    // Filter menu by permissions using existing logic

    private AppMenu filterMenuByPermissions(AppMenu appMenu, User user) {
        // Pre-compute all allowed paths for this user (single DB query approach)
        Set<String> allowedPaths = getUserAllowedPaths(user);

        // filter menu items based on pre-computed permissions
        List<MenuItem> menuItems = new ArrayList<>();
        for (MenuItem menuItem : appMenu.getMenuItems()) {
            if (menuItem.getSlug().equals("/logout")) {
                menuItems.add(menuItem);
                continue;
            }
            if (Utils.isNotEmpty(menuItem.getSubMenu())) {
                String baseSlug = menuItem.getSlug();
                boolean anyAdded = false;
                List<MenuItem> subMenu = new ArrayList<>();
                for (MenuItem subMenuItem : menuItem.getSubMenu()) {
                    String fullPath = baseSlug + subMenuItem.getSlug();
                    if (hasAccessToPath(fullPath, allowedPaths)) {
                        subMenu.add(subMenuItem);
                        anyAdded = true;
                    }
                }
                if (anyAdded) {
                    menuItem.setSubMenu(subMenu);
                    menuItems.add(menuItem);
                }
            } else {
                if (hasAccessToPath(menuItem.getSlug(), allowedPaths)) {
                    menuItems.add(menuItem);
                }
            }
        }
        appMenu.setMenuItems(menuItems);

        if (Utils.isNotEmpty(appMenu.getBottomMenu())) {
            List<MenuItem> bottoMenuItems = new ArrayList<>();
            for (MenuItem menuItem : appMenu.getBottomMenu()) {
                if (menuItem.getSlug().equals("/logout")) {
                    bottoMenuItems.add(menuItem);
                    continue;
                }
                if (Utils.isNotEmpty(menuItem.getSubMenu())) {
                    String baseSlug = menuItem.getSlug();
                    boolean anyAdded = false;
                    List<MenuItem> subMenu = new ArrayList<>();
                    for (MenuItem subMenuItem : menuItem.getSubMenu()) {
                        String fullPath = baseSlug + subMenuItem.getSlug();
                        if (hasAccessToPath(fullPath, allowedPaths)) {
                            subMenu.add(subMenuItem);
                            anyAdded = true;
                        }
                    }
                    if (anyAdded) {
                        menuItem.setSubMenu(subMenu);
                        bottoMenuItems.add(menuItem);
                    }
                } else {
                    if (hasAccessToPath(menuItem.getSlug(), allowedPaths)) {
                        bottoMenuItems.add(menuItem);
                    }
                }
            }
            appMenu.setBottomMenu(bottoMenuItems);
        }

        return appMenu;
    }

    private Set<String> getUserAllowedPaths(User user) {
        Set<String> allowedPaths = new HashSet<>();

        // Always allow public endpoints
        allowedPaths.add("/admin/public/");

        // Collect all role IDs (direct + user group roles)
        Set<String> allRoleIds = new HashSet<>();
        if (Utils.isNotEmpty(user.getAppRoleId())) {
            allRoleIds.add(user.getAppRoleId());
        }

        // Add user group roles
        if (Utils.isNotEmpty(user.getUserGroupId())) {
            CrmModel userGroupCrmModel = crmModelService.getModelByCollectionName(CollectionNames.USER_GROUP);
            Document userGroupDoc = entityOrmDao.getById(user.getUserGroupId(), userGroupCrmModel, null);
            if (userGroupDoc != null) {
                List<String> groupRoles = userGroupDoc.getList("roles", String.class);
                if (Utils.isNotEmpty(groupRoles)) {
                    allRoleIds.addAll(groupRoles);
                }
            }
        }

        if (allRoleIds.isEmpty()) {
            return allowedPaths;
        }

        // Bulk fetch all role documents
        CrmModel appRoleCrmModel = crmModelService.getModelByCollectionName(CollectionNames.APP_ROLE);
        List<ObjectId> roleObjectIds = allRoleIds.stream().map(ObjectId::new)
                .collect(java.util.stream.Collectors.toList());
        DaoQuery roleQuery = DaoQuery.fromCriteria(Criteria.where("_id").in(roleObjectIds));
        List<Document> roleDocuments = entityOrmDao.findByQuery(roleQuery, appRoleCrmModel);

        // Collect all permission IDs from all roles
        Set<String> allPermissionIds = new HashSet<>();
        for (Document roleDoc : roleDocuments) {
            List<String> permissionIds = roleDoc.getList("permissions", String.class);
            if (Utils.isNotEmpty(permissionIds)) {
                allPermissionIds.addAll(permissionIds);
            }
        }

        if (allPermissionIds.isEmpty()) {
            return allowedPaths;
        }

        // Bulk fetch all permission documents
        CrmModel appPermissionModel = crmModelService.getModelByCollectionName(CollectionNames.APP_PERMISSION);
        List<ObjectId> permissionObjectIds = allPermissionIds.stream().map(ObjectId::new)
                .collect(java.util.stream.Collectors.toList());
        DaoQuery permissionQuery = DaoQuery.fromCriteria(Criteria.where("_id").in(permissionObjectIds));
        List<Document> permissionDocuments = entityOrmDao.findByQuery(permissionQuery, appPermissionModel);

        // Extract all allowed paths from permission entries using hierarchical access logic
        for (Document permissionDoc : permissionDocuments) {
            List<Document> entries = permissionDoc.getList("entries", Document.class);
            if (Utils.isNotEmpty(entries)) {
                for (Document entry : entries) {
                    String entrySlug = entry.getString("slug");
                    if (Utils.isNotEmpty(entrySlug) && entry.getBoolean("read", false)) {
                        allowedPaths.add(entrySlug);
                    }
                }
            }
        }

        return allowedPaths;
    }

    private boolean hasAccessToPath(String path, Set<String> allowedPaths) {
        // Check for exact matches or prefix matches
        for (String allowedPath : allowedPaths) {
            if (path.startsWith(allowedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Optimized method that accepts pre-computed permission data
     * Use this when you already have the user's permissions computed
     */
    // public AppMenu getAppMenu(User user, Set<String> allowedPaths) {
    // if (user == null) {
    // return null;
    // }
    // AppMenu appMenu = metaOrmDao.findOneByQuery(null, AppMenu.class);
    // // if (Utils.isTrue(user.getAdmin())) {
    // // return appMenu;
    // // }

    // // filter menu items based on pre-computed permissions
    // List<MenuItem> menuItems = new ArrayList<>();
    // for (MenuItem menuItem : appMenu.getMenuItems()) {
    // if(menuItem.getSlug().equals("/logout")) {
    // menuItems.add(menuItem);
    // continue;
    // }
    // if (Utils.isNotEmpty(menuItem.getSubMenu())) {
    // String baseSlug = menuItem.getSlug();
    // boolean anyAdded = false;
    // List<MenuItem> subMenu = new ArrayList<>();
    // for (MenuItem subMenuItem : menuItem.getSubMenu()) {
    // String fullPath = baseSlug + subMenuItem.getSlug();
    // if (hasAccessToPath(fullPath, allowedPaths)) {
    // subMenu.add(subMenuItem);
    // anyAdded = true;
    // }
    // }
    // if (anyAdded) {
    // menuItem.setSubMenu(subMenu);
    // menuItems.add(menuItem);
    // }
    // } else {
    // if (hasAccessToPath(menuItem.getSlug(), allowedPaths)) {
    // menuItems.add(menuItem);
    // }
    // }
    // }
    // appMenu.setMenuItems(menuItems);
    // return appMenu;
    // }

    /**
     * Pre-computes all allowed paths for a user with optimized bulk queries
     * This method significantly reduces database calls by fetching all required
     * data at once
     */

    /**
     * Fast path checking using pre-computed allowed paths
     * Uses efficient set lookup instead of database queries
     */

    /**
     * Public method to get user's allowed paths - useful for caching or other
     * services
     */
    // public Set<String> getUserPermittedPaths(User user) {
    // if (user == null) {
    // return new HashSet<>();
    // }
    // // if (Utils.isTrue(user.getAdmin())) {
    // // // Admin has access to everything - return a special marker
    // // Set<String> adminPaths = new HashSet<>();
    // // adminPaths.add("/"); // Root path means access to everything
    // // return adminPaths;
    // // }
    // return getUserAllowedPaths(user);
    // }
}
