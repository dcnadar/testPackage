package com.radyfy.common.service.crm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.UpdateResult;
import com.radyfy.common.auth.AccountSession;
import com.radyfy.common.auth.UserSession;
import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.UpdateAction;
import com.radyfy.common.config.mongo.CollectionMongoTemplateFactory;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.crm.model.CrmModelType;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.response.TableResult;
import com.radyfy.common.service.CurrentUserSession;
import com.radyfy.common.service.TableService;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.utils.ValidationUtils;
import jakarta.validation.constraints.NotNull;

/**
 *
 * @author pintu
 * @throw AuthException if not found this service is only used for valid request
 */

@Component
public class EntityOrmDao {
    private static final Logger logger = LoggerFactory.getLogger(EntityOrmDao.class);

    private final CollectionMongoTemplateFactory collectionMongoTemplateFactory;
    private final TableService tableService;
    private final CurrentUserSession currentUserSession;
    private final CrmModelService crmModelService;

    @Autowired
    public EntityOrmDao(CollectionMongoTemplateFactory collectionMongoTemplateFactory,
            TableService tableService, CurrentUserSession currentUserSession,
            CrmModelService crmModelService) {
        this.tableService = tableService;
        this.collectionMongoTemplateFactory = collectionMongoTemplateFactory;
        this.currentUserSession = currentUserSession;
        this.crmModelService = crmModelService;
    }

    private List<Document> updateObjectId(List<Document> documents) {
        for (Document document : documents) {
            updateObjectId(document);
        }
        return documents;
    }

    private Document updateObjectId(Document document) {
        if (document != null)
            document.put("_id", document.getObjectId("_id").toHexString());
        return document;
    }

    public Document getById(String id, CrmModel crmModel, DaoQuery daoQuery) {
        if (id == null) {
            throw new RuntimeException("ID is required");
        }
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        Document document = mongoTemplate.findOne(getAccountQuery(id, daoQuery, crmModel),
                Document.class, crmModel.getCollectionName());
        if (document != null) {
            updateObjectId(document);
            return document;
        }
        return null;
        // throw new AuthException();
    }

    public List<Document> findByQuery(DaoQuery daoQuery, CrmModel crmModel) {

        Query query = getAccountQuery(null, daoQuery, crmModel);
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        List<Document> list =
                mongoTemplate.find(query, Document.class, crmModel.getCollectionName());
        updateObjectId(list);
        return list;
    }

    public Document findOneByQuery(DaoQuery daoQuery, CrmModel crmModel) {

        Query query = getAccountQuery(null, daoQuery, crmModel);
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        Document document =
                mongoTemplate.findOne(query, Document.class, crmModel.getCollectionName());
        updateObjectId(document);
        return document;
    }

    public Document findFirstByQuery(DaoQuery daoQuery, CrmModel crmModel) {

        Query query = getAccountQuery(null, daoQuery, crmModel);
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        query.limit(1);
        List<Document> data =
                mongoTemplate.find(query, Document.class, crmModel.getCollectionName());
        if (Utils.isNotEmpty(data)) {
            return updateObjectId(data.get(0));
        }
        return null;
    }

    public TableResult<Document> table(TableRequest tableRequest, CrmModel crmModel) {

        Query query = new Query();
        appendSessionAbsentCriteria(crmModel,
                tableRequest != null ? tableRequest.getFilterKeys() : null, query);
        TableResult<Document> tableResult = this.tableService.table(tableRequest, crmModel, query);
        updateObjectId(tableResult.getData());
        return tableResult;
    }

    private Document createBaseDocument(Document obj, CrmModel crmModel) {
        if (obj.getString("_id") != null) {
            obj.put("_id", null);
        }

        // crmModelService.forEachBaseModels(cm -> {
        // String value = getBaseFilterValue(cm.getFieldName(), crmModel.getCollectionName());
        // if (Utils.isNotEmpty(value)) {
        // obj.put(cm.getFieldName(), value);
        // }
        // }, crmModel);
        // adding accountId field, if not exist
        if (!obj.containsKey(Constants.ACCOUNT_ID)) {

            if (Constants.RADYFY_ACCOUNT_ID.equals(currentUserSession.getAccount().getId())) {

                if(Constants.RADYFY_META_COLLECTIONS.contains(crmModel.getCollectionName())) {
                    obj.put(Constants.ACCOUNT_ID, currentUserSession.getUserSession().getFeFilters()
                            .get(Constants.ACCOUNT_ID));
                }
            }
        }

        obj.put("created", new Date());
        obj.put("updated", new Date());
        if (!obj.containsKey("createdBy") && currentUserSession.getUser() != null) {
            obj.put("createdBy", currentUserSession.getUser().getId());
        }
        return obj;
    }

    public Document create(Document obj, CrmModel crmModel) {
        obj = createBaseDocument(obj, crmModel);
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        Document document = mongoTemplate.save(obj, crmModel.getCollectionName());
        updateObjectId(document);
        return document;
    }

    public List<Document> insert(List<Document> objs, CrmModel crmModel) {
        List<Document> documents = new ArrayList<>();
        for (Document obj : objs) {
            documents.add(createBaseDocument(obj, crmModel));
        }
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        return (List<Document>) mongoTemplate.insert(documents, crmModel.getCollectionName());
    }

    public boolean updateByQuery(DaoQuery daoQuery, Update update, CrmModel crmModel) {

        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        update.set("updated", new Date());
        if (currentUserSession.getUser() != null) {
            update.set("updatedBy", currentUserSession.getUser().getId());
        }
        UpdateResult result = mongoTemplate.updateFirst(getAccountQuery(null, daoQuery, crmModel),
                update, crmModel.getCollectionName());
        return result.getMatchedCount() > 0;
    }

    public Document findAndModify(DaoQuery daoQuery, Update update, CrmModel crmModel) {

        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        Document document = mongoTemplate.findAndModify(getAccountQuery(null, daoQuery, crmModel),
                update, new FindAndModifyOptions().returnNew(daoQuery.isReturnNewOnFindAndModify()),
                Document.class, crmModel.getCollectionName());
        if (document != null) {
            updateObjectId(document);
        }
        return document;
    }

    public Document saveFullDocument(String id, CrmModel crmModel, UpdateAction action) {
        Document obj = getById(id, crmModel, null);
        action.run(obj);
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        Document document = mongoTemplate.save(obj, crmModel.getCollectionName());
        return updateObjectId(document);
    }

    public void updateMulti(DaoQuery daoQuery, Update update, CrmModel crmModel) {

        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        mongoTemplate.updateMulti(getAccountQuery(null, daoQuery, crmModel), update,
                crmModel.getCollectionName());
    }

    public void upsert(DaoQuery query, Update update, CrmModel crmModel) {
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        update.setOnInsert("created", new Date());
        update.setOnInsert("updated", new Date());
        if (currentUserSession.getUser() != null) {
            update.setOnInsert("createdBy", currentUserSession.getUser().getId());
        }
        mongoTemplate.upsert(getAccountQuery(null, query, crmModel), update,
                crmModel.getCollectionName());
    }

    public void delete(String id, CrmModel crmModel) {
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        mongoTemplate.findAndRemove(getAccountQuery(id, null, crmModel), Document.class,
                crmModel.getCollectionName());
    }

    public void delete(DaoQuery daoQuery, CrmModel crmModel) {
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        mongoTemplate.findAndRemove(getAccountQuery(null, daoQuery, crmModel), Document.class,
                crmModel.getCollectionName());
    }

    public void appendSessionAbsentCriteria(CrmModel crmModel, Set<String> existingFilterKeys,
            Query query) {

        // filters for the query
        Map<String, Criteria> filters = new LinkedHashMap<>();
        if (crmModel.getModelType() == CrmModelType.COLLECTION
                && !crmModel.getCollectionName().equals(CollectionNames.user)) {
            boolean isOrgModel = Utils.isNotEmpty(crmModel.getBaseModelId())
                    || (Utils.isTrue(crmModel.getIsOrg())
                            && Utils.isNotEmpty(crmModel.getParent()));
            boolean isOrgScope = Utils.isTrue(crmModel.getIsOrgScopeApplicable());
            boolean isUserAccount = Utils.isTrue(crmModel.getIsUserAccount());
            if (isOrgModel || isOrgScope || isUserAccount) {
                crmModelService.forEachBaseModels(bm -> {
                    String fieldName = bm.getFieldName();
                    String filterKey =
                            bm.getModelId().equals(crmModel.getId()) ? "_id" : bm.getFieldName();
                    if (existingFilterKeys != null && existingFilterKeys.contains(filterKey)) {
                        return;
                    }
                    Criteria value =
                            getBaseFilterValue(fieldName, filterKey, crmModel.getCollectionName());
                    if (Utils.isNotEmpty(value)) {
                        filters.put(filterKey, value);
                    }
                }, crmModel, true);
            }
        }
        // checking crmModel is not for account collection
        // if ((existingFilterKeys == null || !existingFilterKeys.contains(Constants.ACCOUNT_ID))) {
        // if (filters.containsKey(Constants.ACCOUNT_ID)) {
        // query.addCriteria(filters.get(Constants.ACCOUNT_ID));
        // filters.remove(Constants.ACCOUNT_ID);
        // } else {
        // // checking account id is present in the userSession.getFeFilters()
        // // if (currentUserSession.getUserSession() != null
        // // && Utils.isNotEmpty(currentUserSession.getUserSession().getFeFilters())
        // // &&
        // // currentUserSession.getUserSession().getFeFilters().containsKey(Constants.ACCOUNT_ID))
        // // {
        // // query.addCriteria(Criteria.where(Constants.ACCOUNT_ID)
        // // .is(currentUserSession.getUserSession().getFeFilters().get(Constants.ACCOUNT_ID)));
        // // } else {
        // query.addCriteria(Criteria.where(Constants.ACCOUNT_ID)
        // .is(currentUserSession.getAccount().getId()));
        // // }
        // }
        // }
        filters.forEach((k, v) -> query.addCriteria(v));
    }

    // public static void addBaseFilters(
    // BaseCrmModel baseModel,
    // Query query,
    // AccountSession accountSession,
    // UserSession userSession,
    // String collectionName) {

    // String fieldName = baseModel.getFieldName();
    // String value = getBaseFilterValue(
    // fieldName,
    // accountSession,
    // userSession,
    // collectionName);
    // if (Utils.isNotEmpty(value)) {
    // query.addCriteria(Criteria.where(fieldName).is(value));
    // }
    // }

    private Criteria getBaseFilterValue(String fieldName, String filterKey, String collectionName) {

        AccountSession accountSession = currentUserSession.getAccountSession();
        UserSession userSession = currentUserSession.getUserSession();

        boolean checkFeFilter = true;
        boolean isRadyfyAccount =
                Constants.RADYFY_ACCOUNT_ID.equals(accountSession.getAccount().getId());
        if (isRadyfyAccount) {
            checkFeFilter = false;
            if (Constants.RADYFY_META_COLLECTIONS.contains(collectionName)) {
                checkFeFilter = true;
            }
        }
        if (checkFeFilter && userSession != null && Utils.isNotEmpty(userSession.getFeFilters())
                && userSession.getFeFilters().containsKey(fieldName)) {
            return Criteria.where(filterKey).is(userSession.getFeFilters().get(fieldName));
        }

        // if (fieldName.equals("appId")) {
        // return accountSession.getApp() != null
        // ? Criteria.where(filterKey).is(accountSession.getApp().getId())
        // : null;
        // }

        // if (fieldName.equals(Constants.ECOM_ACCOUNT_ID)) {
        // return accountSession.getEcomAccount() != null
        // ? Criteria.where(filterKey).is(accountSession.getEcomAccount().getId())
        // : null;
        // }

        // if (fieldName.equals(Constants.ACCOUNT_ID)) {
        // return accountSession.getAccount() != null
        // ? Criteria.where(filterKey).is(accountSession.getAccount().getId())
        // : null;
        // }

        if (userSession != null && userSession.getUser() != null) {
            Document userDocument = userSession.getUser().getDocument();
            if (userDocument.containsKey(fieldName)) {
                Object accessObject = userDocument.get(fieldName);
                if (accessObject instanceof List) {
                    List<String> access = userDocument.getList(fieldName, String.class);
                    if (Utils.isNotEmpty(access)) {
                        if (!access.contains("All")) {
                            if ("_id".equals(filterKey)) {
                                return Criteria.where("_id")
                                        .in(access.stream().map(ObjectId::new).toList());
                            } else {
                                List<String> accessCriteria = new ArrayList<>(access);
                                accessCriteria.add("All");
                                return Criteria.where(filterKey).in(accessCriteria);
                            }
                        }
                    }
                } else if (accessObject instanceof String) {
                    if (Utils.isNotEmpty(accessObject) && !accessObject.equals("All")) {
                        if (ValidationUtils.isValidHexID(accessObject.toString())) {
                            return Criteria.where(filterKey).is(accessObject.toString());
                        } else {
                            throw new RuntimeException("Invalid access object: " + accessObject);
                        }
                    }
                }
            }
        }

        return null;

        // throw new RuntimeException(Errors.CRM_MODEL_BASE_FILTER_REQUIRED + ": " + fieldName + ",
        // collection: " + collectionName);
    }

    private Query getAccountQuery(String objId, DaoQuery daoQuery, @NotNull CrmModel crmModel) {
        Query query = new Query();
        if (Utils.isNotEmpty(objId)) {
            query.addCriteria(Criteria.where("_id").is(new ObjectId(objId)));
        }
        appendSessionAbsentCriteria(crmModel, daoQuery != null ? daoQuery.getFilterKeys() : null,
                query);
        if (daoQuery != null) {
            if (Utils.isNotEmpty(daoQuery.getCriteriaList())) {
                for (CriteriaDefinition criteria : daoQuery.getCriteriaList()) {
                    query.addCriteria(criteria);
                }
            }
            if (Utils.isNotEmpty(daoQuery.getFields())) {
                for (String field : daoQuery.getFields()) {
                    query.fields().include(field);
                }
            }
            if (daoQuery.getLimit() != null) {
                query.limit(daoQuery.getLimit());
            } else {
                if (!daoQuery.isGetAll()) {
                    query.limit(100);
                }
            }
            if (daoQuery.getSort() != null) {
                query.with(daoQuery.getSort());
            }
        }
        logger.debug("Final account collection: {} query: {}", crmModel.getCollectionName(), query);
        return query;
    }

    public long getCount(CrmModel crmModel) {
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        return mongoTemplate.count(getAccountQuery(null, null, crmModel),
                crmModel.getCollectionName());
    }

    public long getCount(DaoQuery daoQuery, CrmModel crmModel) {
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        return mongoTemplate.count(getAccountQuery(null, daoQuery, crmModel),
                crmModel.getCollectionName());
    }

    public AggregationResults<Document> aggregate(CrmModel crmModel,
            AggregationOperation... operations) {
        MongoTemplate mongoTemplate =
                collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        List<AggregationOperation> accountOperations = new ArrayList<>();
        AtomicBoolean accountIdFilter = new AtomicBoolean();
        if (crmModel.getModelType() == CrmModelType.COLLECTION) {
            boolean isOrgModel = Utils.isNotEmpty(crmModel.getBaseModelId())
                    || (Utils.isTrue(crmModel.getIsOrg())
                            && Utils.isNotEmpty(crmModel.getParent()));
            boolean isOrgScope = Utils.isTrue(crmModel.getIsOrgScopeApplicable());
            boolean isUserAccount = Utils.isTrue(crmModel.getIsUserAccount());
            if (isOrgModel || isOrgScope || isUserAccount) {
                crmModelService.forEachBaseModels(cm -> {
                    String fieldName = cm.getFieldName();
                    String filterKey =
                            cm.getModelId().equals(crmModel.getId()) ? "_id" : cm.getFieldName();
                    Criteria value =
                            getBaseFilterValue(fieldName, filterKey, crmModel.getCollectionName());
                    if (Utils.isNotEmpty(value)) {
                        accountOperations.add(Aggregation.match(value));
                        if (filterKey.equals(Constants.ACCOUNT_ID)) {
                            accountIdFilter.set(true);
                        }
                    }
                }, crmModel, true);
            }
        }
        accountOperations.addAll(Arrays.asList(operations));
        return mongoTemplate.aggregate(Aggregation.newAggregation(accountOperations),
                crmModel.getCollectionName(), Document.class);
    }

}
