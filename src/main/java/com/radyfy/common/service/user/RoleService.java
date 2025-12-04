package com.radyfy.common.service.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.radyfy.common.model.crm.api.CrmApi;
import com.radyfy.common.model.crm.role.Permission;
import com.radyfy.common.model.crm.role.Role;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.user.User;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.utils.Utils;

@Component
@Deprecated
public class RoleService {

  private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

  private final MetaOrmDao metaOrmDao;

  @Autowired
  public RoleService(MetaOrmDao metaOrmDao) {
    this.metaOrmDao = metaOrmDao;
  }

  public List<CrmApi> getPermittedApis(User user) {
    List<CrmApi> crmApis = new ArrayList<>();
    // if (Utils.isNotEmpty(user.getRoleId())) {
    //   Role role = metaOrmDao.findOneByQuery(DaoQuery.fromCriteria(Criteria.where("id").is(user.getRoleId())),
    //       Role.class);
    //   if (role != null) {
    //     List<String> permissionIds = role.getPermissions();
    //     for (String permissionId : permissionIds) {
    //       Permission permission = metaOrmDao
    //           .findOneByQuery(DaoQuery.fromCriteria(Criteria.where("id").is(permissionId)), Permission.class);
    //       if (permission != null) {
    //         crmApis.addAll(metaOrmDao.findByQuery(
    //             DaoQuery.fromCriteria(Criteria.where("id").in(permission.getApisAccess())), CrmApi.class));
    //       }
    //     }
    //   }
    // }
    throw new RuntimeException("Not Allowed to get permitted APIs");
  }

  public List<Permission> getPermission(User user) {
    // if (Utils.isNotEmpty(user.getRoleId())) {
    //   Role role = metaOrmDao.findOneByQuery(DaoQuery.fromCriteria(Criteria.where("id").is(user.getRoleId())),
    //       Role.class);
    //   if (role != null) {
    //     List<String> permissionIds = role.getPermissions();
    //     return metaOrmDao
    //         .findByQuery(DaoQuery.fromCriteria(Criteria.where("id").in(permissionIds)), Permission.class);
    //   }
    // }
    throw new RuntimeException("Not Allowed to get permissions");
  }

  public boolean hasPermission(User user, String permissionKey) {
    // if (Utils.isNotEmpty(user.getRoleId())) {
    //   Role role = metaOrmDao.findOneByQuery(DaoQuery.fromCriteria(Criteria.where("id").is(user.getRoleId())),
    //       Role.class);
    //   if (role != null) {
    //     List<String> permissionIds = role.getPermissions();
    //     DaoQuery daoQuery = DaoQuery.builder().criteriaList(Arrays.asList(
    //       Criteria.where("id").in(permissionIds),
    //       Criteria.where("key").in(permissionKey)
    //     )).build();
    //     return metaOrmDao.count(daoQuery, Permission.class) > 0;
    //   }
    // }
    // return false;
    throw new RuntimeException("Not Allowed to check permission");
  }

}
