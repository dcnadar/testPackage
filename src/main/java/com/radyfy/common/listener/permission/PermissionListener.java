package com.radyfy.common.listener.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Api;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.CrmForms;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.menu.AppMenu;
import com.radyfy.common.model.crm.menu.MenuItem;
import com.radyfy.common.model.dynamic.form.FormGroup;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.CrmFormConfig;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.CurrentUserSession;

@Component
public class PermissionListener {

  private final MetaOrmDao metaOrmDao;
  private final CurrentUserSession currentUserSession;

  @Autowired
  public PermissionListener(MetaOrmDao metaOrmDao, CurrentUserSession currentUserSession) {
    this.metaOrmDao = metaOrmDao;
    this.currentUserSession = currentUserSession;
  }

  @Autowired
  public void onAccountFormLoad(ConfigBuilder configBuilder) {
    configBuilder.getFormConfig().addEventListener(
        CrmFormConfig.Event.ON_LOAD,
        CrmForms.RADYFY_PERMISSION_FORM,
        props -> {

          String accountId = currentUserSession.getAccount().getId();
          boolean isRadyfyAccount = Constants.RADYFY_ACCOUNT_ID.equals(accountId);

          String roleKey = currentUserSession.getUser().getAppRoleId();

          AppMenu appMenuDoc = metaOrmDao.findOneByQuery(null, AppMenu.class);
          if (appMenuDoc == null)
            return;

          List<MenuItem> combinedMenuItems = new ArrayList<>();

          addMenuItems(appMenuDoc.getMenuItems(), combinedMenuItems);
          addMenuItems(appMenuDoc.getBottomMenu(), combinedMenuItems);

          CrmForm form = props.getCrmForm();
          FormGroup[] rows = form.getRows();

          Arrays.stream(rows)
              .flatMap(row -> row.getFields().stream())
              .filter(field -> "entries".equals(field.getId()))
              .findFirst()
              .ifPresent(field -> {

                @SuppressWarnings("unchecked")
                List<Document> existingEntries = field.getValue() != null
                    ? (List<Document>) field.getValue()
                    : null;

                List<Document> entries = new ArrayList<>();

                // Check if accountId matches the specific account
                if (isRadyfyAccount) {
                  if (RoleType.SUPER_ADMIN.equals(roleKey)) {
                    addAllPermissionEntry(existingEntries, entries);
                    addPermissionEntries(combinedMenuItems, existingEntries, entries, RoleType.SUPER_ADMIN);
                  } else if (RoleType.SYSTEM_ADMIN.equals(roleKey)) {
                    addPermissionEntries(combinedMenuItems, existingEntries, entries, RoleType.SYSTEM_ADMIN);
                  } else {
                    throw new RuntimeException("Invalid role for account " + accountId
                        + ". Expected super_admin or system_admin, but found: " + roleKey);
                  }
                } else {
                  // For other accounts
                  if (RoleType.ACCOUNT_ADMIN.equals(roleKey)) {
                    addAllPermissionEntry(existingEntries, entries);
                    addPermissionEntries(combinedMenuItems, existingEntries, entries, RoleType.ACCOUNT_ADMIN);
                  } else {
                    throw new RuntimeException(
                        "Invalid role for account " + accountId + ". Expected account_admin, but found: " + roleKey);
                  }
                }

                field.setValue(entries);
              });

        });
  }

  private void addMenuItems(List<MenuItem> menuItems, List<MenuItem> combinedMenuItems) {
    if (Utils.isNotEmpty(menuItems)) {
      for (MenuItem item : menuItems) {
        if (item != null && item.getKey() != null && !"logout".equalsIgnoreCase(item.getKey())) {
          String baseSlug = item.getSlug();
          if (Utils.isNotEmpty(item.getSubMenu())) {
            for (MenuItem subItem : item.getSubMenu()) {
              String fullPath = baseSlug + subItem.getSlug();
              subItem.setSlug(fullPath);
              combinedMenuItems.add(subItem);
            }
          } else {
            combinedMenuItems.add(item);
          }
        }
      }
    }
  }

  private void addAllPermissionEntry(List<Document> existingEntries, List<Document> entries) {
    // Add "All" permission
    Document allPermissionDoc = null;
    if (existingEntries != null) {
      allPermissionDoc = existingEntries.stream()
          .filter(d -> d.getString("slug").equals("/")).findFirst().orElse(null);
    }
    if (allPermissionDoc == null) {
      allPermissionDoc = new Document().append("name", "All").append("slug", "/");
    }
    entries.add(allPermissionDoc);

  }

  private void addPermissionEntries(List<MenuItem> combinedMenuItems, List<Document> existingEntries,
      List<Document> entries, String roleType) {
    // Add all menu items except excluded ones
    if (combinedMenuItems != null) {
      for (MenuItem item : combinedMenuItems) {
        String menuSlug = item.getSlug();
        if (!isSlugExcluded(menuSlug, roleType)) {
          String menuValue = item.getValue();
          Document doc = null;
          if (existingEntries != null) {
            doc = existingEntries.stream()
                .filter(d -> d.getString("slug").equals(menuSlug)).findFirst()
                .orElse(null);
          }
          if (doc == null) {
            doc = new Document().append("name", menuValue).append("slug", menuSlug);
          }
          entries.add(doc);
        }
      }
    }
  }

  private boolean isSlugExcluded(String slug, String roleType) {
    if (RoleType.SYSTEM_ADMIN.equals(roleType)) {
      return Arrays.asList(Api.ADMIN_ACCOUNT, Api.ADMIN_APP_ROLE, Api.ADMIN_APP_PERMISSION, Api.ADMIN_USER_GROUP)
          .contains(slug);
    }
    return Arrays.asList(Api.ADMIN_APP_ROLE, Api.ADMIN_APP_PERMISSION, Api.ADMIN_USER_GROUP).contains(slug);
  }

}
