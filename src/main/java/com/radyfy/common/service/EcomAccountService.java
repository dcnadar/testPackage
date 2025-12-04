// package com.radyfy.common.service;

// import org.bson.Document;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.mongodb.core.query.Update;
// import org.springframework.stereotype.Component;

// import com.radyfy.common.commons.CollectionNames;
// import com.radyfy.common.model.crm.model.CrmModel;
// import com.radyfy.common.model.dao.DaoQuery;
// import com.radyfy.common.service.crm.CrmModelService;
// import com.radyfy.common.service.crm.EntityOrmDao;

// @Component
// @Deprecated
// public class EcomAccountService {
//   private static final Logger logger = LoggerFactory.getLogger(EcomAccountService.class);

//   private final EntityOrmDao entityOrmDao;
//   private final CrmModelService crmModelService;

//   @Autowired
//   public EcomAccountService(
//       EntityOrmDao crmAccountOrmDao,
//       CrmModelService crmModelService) {
//     this.entityOrmDao = crmAccountOrmDao;
//     this.crmModelService = crmModelService;
//   }

//   public Document getSettings() {

//     CrmModel accountSettingsCrmModel = crmModelService.getModelByCollectionName(CollectionNames.accountSettings);
//     if (accountSettingsCrmModel == null) {
//       return null;
//     }
//     Document settings = this.entityOrmDao.findOneByQuery(
//         null, accountSettingsCrmModel);
//     if (settings == null) {
//       return entityOrmDao.create(new Document(), accountSettingsCrmModel);
//     }
//     return settings;
//   }

//   public void updateSettings(Update update) {
//     Document settings = this.getSettings();
//     updateSettingsById(settings.getString("_id"), update);
//   }

//   public void updateSettingsById(String id, Update update) {
//     CrmModel accountSettingsCrmModel = crmModelService.getModelByCollectionName(CollectionNames.accountSettings);
//     this.entityOrmDao.updateByQuery(DaoQuery.fromId(id), update, accountSettingsCrmModel);
//   }
// }
