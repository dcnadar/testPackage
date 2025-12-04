package com.radyfy.common.service;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.exception.AuthException;
import com.radyfy.common.model.BaseEntityModel;
import com.radyfy.common.model.commons.RedisData;
import com.radyfy.common.model.crm.model.BaseCrmModel;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.crm.model.CrmModelType;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.service.common.MemoryService;
import com.radyfy.common.service.crm.BaseModalConsumer;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.utils.ValidationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class MetaOrmDao {

    private static final Logger logger = LoggerFactory.getLogger(MetaOrmDao.class);

    private final MongoTemplate mongoTemplate;
    private final CurrentUserSession currentUserSession;
    private final MemoryService memoryService;

    @Autowired
    public MetaOrmDao(@Qualifier("metaMongoTemplate") MongoTemplate mongoTemplate,
            CurrentUserSession currentUserSession, MemoryService memoryService) {
        this.mongoTemplate = mongoTemplate;
        this.currentUserSession = currentUserSession;
        this.memoryService = memoryService;
    }

    public <T extends BaseEntityModel> T getById(String id, Class<T> klass, DaoQuery daoQuery) {
        if (id == null) {
            throw new RuntimeException("ID is required");
        }
        Query query = getAccountQuery(id, daoQuery, klass);

        String redisKey =
                "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
        RedisData<T> data = (RedisData<T>) memoryService.getData(redisKey);
        if (data != null) {
            logger.debug("Fetched data from redis, key: {}, data: {} ", redisKey, data.getData());
            return data.getData();
        }

        T document = mongoTemplate.findOne(query, klass);
        if (document != null) {
            memoryService.saveData(redisKey, new RedisData<>(document), klass);
            return document;
        }
        // TODO add auth history
        throw new AuthException();
    }

    public <T extends BaseEntityModel> List<T> findByQuery(DaoQuery daoQuery, Class<T> klass) {

        Query query = getAccountQuery(null, daoQuery, klass);
        String redisKey =
                "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
        RedisData<List<T>> data = (RedisData<List<T>>) memoryService.getData(redisKey);
        if (data != null) {
            logger.debug("Fetched data from redis, key: {}, data: {} ", redisKey, data.getData());
            return data.getData();
        }
        List<T> fetchedData = mongoTemplate.find(query, klass);

        memoryService.saveData(redisKey, new RedisData<>(fetchedData), klass);

        return fetchedData;
    }

    public <T extends BaseEntityModel> T findOneByQuery(DaoQuery daoQuery, Class<T> klass) {

        Query query = getAccountQuery(null, daoQuery, klass);

        // getting from redis
        String redisKey =
                "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
        RedisData<T> data = (RedisData<T>) memoryService.getData(redisKey);
        if (data != null) {
            logger.debug("Fetched data from redis, key: {}, data: {} ", redisKey, data.getData());
            return data.getData();
        }

        T document = mongoTemplate.findOne(query, klass);

        // saving to redis
        memoryService.saveData(redisKey, new RedisData<>(document), klass);

        return document;
    }

    public <T extends BaseEntityModel> T findFirstByQuery(DaoQuery daoQuery, Class<T> klass) {

        Query query = getAccountQuery(null, daoQuery, klass);
        query.limit(1);

        // getting from redis
        String redisKey =
                "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
        RedisData<T> redisData = (RedisData<T>) memoryService.getData(redisKey);
        if (redisData != null) {
            logger.debug("Fetched data from redis, key: {}, data: {} ", redisKey,
                    redisData.getData());
            return redisData.getData();
        }

        List<T> data = mongoTemplate.find(query, klass);
        T document = null;
        if (Utils.isNotEmpty(data)) {
            document = data.get(0);
        }

        // saving to redis
        memoryService.saveData(redisKey, new RedisData<>(document), klass);

        return document;
    }

    public <T extends BaseEntityModel> void upsert(DaoQuery query, Update update, Class<T> klass) {

        update.setOnInsert(Constants.ACCOUNT_ID, currentUserSession.getAccount().getId());
        update.setOnInsert("created", new Date());
        update.setOnInsert("updated", new Date());
        mongoTemplate.upsert(getAccountQuery(null, query, klass), update, klass);
    }

    // public <T extends BaseEntityModel> void updateById(String id, Update update, Class<T> klass)
    // {

    // Query query = getAccountQuery(id, null, klass);
    // mongoTemplate.updateFirst(query, update, klass);

    // // deleting redis data
    // String redisKey = "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
    // memoryService.invalidateData(redisKey);
    // }

    // public <T extends BaseEntityModel> void updateByQuery(DaoQuery daoQuery, Update update,
    // Class<T> klass) {

    // Query query = getAccountQuery(null, daoQuery, klass);
    // UpdateResult result = mongoTemplate.updateFirst(query, update, klass);
    // if (result.getMatchedCount() == 0) {
    // throw new AuthException();
    // }

    // // deleting redis data
    // String redisKey = "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
    // memoryService.invalidateData(redisKey);
    // }

    public <T extends BaseEntityModel> long count(DaoQuery daoQuery, Class<T> klass) {

        Query query = getAccountQuery(null, daoQuery, klass);
        // getting from redis
        String redisKey =
                "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
        RedisData<Long> redisData = (RedisData<Long>) memoryService.getData(redisKey);
        if (redisData != null) {
            logger.debug("Fetched data from redis, key: {}, data: {} ", redisKey,
                    redisData.getData());
            return redisData.getData();
        }

        long count = mongoTemplate.count(query, klass);

        // saving to redis
        memoryService.saveData(redisKey, new RedisData<>(count), klass);

        return count;
    }

    private <T extends BaseEntityModel> CrmModel getCrmModelByClass(String collectionName) {
        return findOneByQuery(DaoQuery.builder()
                .criteriaList(Collections
                        .singletonList(Criteria.where("collectionName").is(collectionName)))
                .build(), CrmModel.class);
    }

    // public <T extends BaseEntityModel> T create(T obj, Class<T> klass) {

    // if (obj.getId() != null) {
    // obj.setId(null);
    // }

    // boolean addedBaseFilter = false;
    // String collectionName = klass.getAnnotation(Document.class).collection();
    // if (!collectionName.equals(CollectionNames.crmModel)) {
    // CrmModel crmModel = getCrmModelByClass(collectionName);
    // if (crmModel != null) {
    // forEachBaseModels(bm -> {
    // if (Utils.isNotEmpty(ObjectUtils.getFormFieldValue(bm.getFieldName(), obj))) {
    // return;
    // }
    // String value = EntityOrmDao.getBaseFilterValue(
    // bm.getFieldName(),
    // currentUserSession.getAccountSession(),
    // currentUserSession.getUserSession(),
    // collectionName);
    // if (Utils.isNotEmpty(value)) {
    // ObjectUtils.setFormFieldValue(bm.getFieldName(), value, obj);
    // }
    // }, crmModel);
    // addedBaseFilter = true;
    // }
    // }

    // if (!addedBaseFilter) {
    // // check klass extends EcomBaseEntityModel
    // if (EcomBaseEntityModel.class.isAssignableFrom(klass)) {
    // EcomBaseEntityModel ecomObj = (EcomBaseEntityModel) obj;
    // ecomObj.setEcomAccountId(currentUserSession.getEcomAccount().getId());
    // }
    // }

    // obj.setAccountId(currentUserSession.getAccount().getId());
    // obj.setCreated(new Date());
    // return mongoTemplate.save(obj);
    // }

    // public <T extends BaseEntityModel> void delete(String id, Class<T> klass) {
    // Query query = getAccountQuery(id, null, klass);
    // mongoTemplate.findAndRemove(query, klass);

    // // deleting redis data
    // String redisKey = "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
    // memoryService.invalidateData(redisKey);
    // }

    // public <T extends BaseEntityModel> void delete(DaoQuery daoQuery, Class<T> klass) {
    // Query query = getAccountQuery(null, daoQuery, klass);
    // mongoTemplate.findAndRemove(query, klass);

    // // deleting redis data
    // String redisKey = "_class_" + klass.getAnnotation(Document.class).collection() + "_" + query;
    // memoryService.invalidateData(redisKey);
    // }

    private <T extends BaseEntityModel> Query getAccountQuery(String objId, DaoQuery daoQuery,
            Class<T> klass) {
        Query query = new Query();
        if (ValidationUtils.isValidHexID(objId)) {
            query.addCriteria(Criteria.where("_id").is(new ObjectId(objId)));
        }
        // else {
        if (currentUserSession.getAccountSession() != null
                && (daoQuery == null || !daoQuery.hasFilterKey(Constants.ACCOUNT_ID))) {
            // if (!klass.equals(Account.class)) {
            String accountId = currentUserSession.getAccountIdOrNull();

            boolean isRadyfyAccount = Constants.RADYFY_ACCOUNT_ID.equals(accountId);

            boolean isPreview = false;
            if (currentUserSession.getRequestSession() != null && Boolean.TRUE
                    .equals(currentUserSession.getRequestSession().getPreviewData())) {
                isPreview = true;
            }
            if (isRadyfyAccount && isPreview) {
                query.addCriteria(Criteria.where(Constants.ACCOUNT_ID).is(currentUserSession
                        .getUserSession().getFeFilters().get(Constants.ACCOUNT_ID)));
            } else {
                query.addCriteria(Criteria.where(Constants.ACCOUNT_ID)
                        .is(currentUserSession.getAccount().getId()));
            }

        }

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
                    query.limit(10);
                }
            }
            if (daoQuery.getSort() != null) {
                query.with(daoQuery.getSort());
            }
        }
        logger.debug("Final account query for class {}: {}", klass.getSimpleName(), query);
        return query;
    }

    public List<BaseCrmModel> getBaseModels() {

        String accountId = currentUserSession.getAccount().getId();

        if (memoryService.getBaseCrmModels(accountId) != null) {
            return memoryService.getBaseCrmModels(accountId).getData();
        }

        DaoQuery daoQuery = DaoQuery.builder()
                .criteriaList(Arrays.asList(Criteria.where("modelType").is(CrmModelType.BASE)))
                .sort(Sort.by(Sort.Order.asc("order"))).build();
        List<BaseCrmModel> baseModels = findByQuery(daoQuery, BaseCrmModel.class);
        memoryService.setBaseCrmModels(accountId, baseModels);
        return baseModels;

    }

    public List<BaseCrmModel> getBaseModels(String accountId) {

        if (memoryService.getBaseCrmModels(accountId) != null) {
            return memoryService.getBaseCrmModels(accountId).getData();
        }
        
        DaoQuery daoQuery = DaoQuery.builder()
                .criteriaList(Arrays.asList(Criteria.where("accountId").is(accountId),
                        Criteria.where("modelType").is(CrmModelType.BASE)))
                .sort(Sort.by(Sort.Order.asc("order"))).build();
        List<BaseCrmModel> baseModels = findByQuery(daoQuery, BaseCrmModel.class);
        memoryService.setBaseCrmModels(accountId, baseModels);
        return baseModels;
    }



    public void forEachBaseModels(BaseModalConsumer consumer, CrmModel model,
            boolean includeCurrentModel) {


        if (Constants.ACCOUNT.equals(model.getParent()) && !includeCurrentModel) {
            return;
        }

        List<BaseCrmModel> baseModels = getBaseModels();

        boolean take = true;
        boolean lastTake = false;

        for (int i = 0; i < baseModels.size(); i++) {
            BaseCrmModel baseModel = baseModels.get(i);
            if (take) {
                consumer.apply(baseModel);
            }
            if (lastTake) {
                take = false;
            } else {
                if (baseModel.getId().equals(model.getBaseModelId())
                        || baseModel.getId().equals(model.getParent())) {
                    if (includeCurrentModel) {
                        lastTake = true;
                    } else {
                        take = false;
                    }
                } else if (Constants.ACCOUNT.equals(model.getParent())) {
                    take = false;
                }
            }
        }
    }
}
