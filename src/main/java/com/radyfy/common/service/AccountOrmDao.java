package com.radyfy.common.service;
// package com.radyfy.common.service;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Date;
// import java.util.List;
// import java.util.stream.Collectors;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.data.mongodb.core.aggregation.*;
// import org.springframework.data.mongodb.core.query.*;
// import org.springframework.stereotype.Component;

// import com.radyfy.common.exception.AuthException;
// import com.radyfy.common.model.*;
// import com.radyfy.common.model.dao.DaoQuery;
// import com.radyfy.common.request.table.TableRequest;
// import com.radyfy.common.response.TableResult;
// import com.radyfy.common.utils.Utils;

// /**
//  *
//  * @author pintu
//  * @throw AuthException if not found
//  *        this service is only used for valid request
//  */

// @Component
// public class AccountOrmDao {

// 	private static final Logger logger = LoggerFactory.getLogger(AccountOrmDao.class);

// 	private final TableService tableService;
// 	private final MongoTemplate mongoTemplate;
// 	private final CurrentUserSession currentUserSession;

// 	public AccountOrmDao(
// 		TableService tableService,
// 		@Qualifier("metaMongoTemplate")
// 		MongoTemplate mongoTemplate,
// 		CurrentUserSession currentUserSession) {
// 		this.tableService = tableService;
// 		this.mongoTemplate=mongoTemplate;
// 		this.currentUserSession = currentUserSession;
// 	}

// 	public <T extends BaseEntityModel> T getById(String id, Class<T> klass, DaoQuery daoQuery) {
// 		mongoTemplate.getCollection("").find();
// 		T document = mongoTemplate.findOne(getAccountQuery(id, daoQuery, klass), klass);
// 		if (document != null) {
// 			return document;
// 		}
// 		// TODO add auth history
// 		throw new AuthException();
// 	}

// 	public <T extends BaseEntityModel> List<T> findByQuery(DaoQuery daoQuery, Class<T> klass) {

// 		Query query = getAccountQuery(null, daoQuery, klass);

// 		return mongoTemplate.find(query, klass);
// 	}

// 	public <T extends BaseEntityModel> T findOneByQuery(DaoQuery daoQuery, Class<T> klass) {

// 		Query query = getAccountQuery(null, daoQuery, klass);

// 		return mongoTemplate.findOne(query, klass);
// 	}

// 	public <T extends BaseEntityModel> TableResult<T> table(TableRequest tableRequest, Class<T> klass) {

// 		Query query = new Query();
// 		appendSessionAbsentCriteria(klass, query, tableRequest.getAdditionalCriterias());
// //		return this.tableService.table(tableRequest, klass, query);
// 		return null;
// 	}

// 	public <T extends BaseEntityModel> T create(T obj) {

// 		if (obj.getId() != null) {
// 			obj.setId(null);
// 		}

// 		if(!Utils.isNotEmpty(obj.getAccountId())) {
// 			obj.setAccountId(currentUserSession.getAccount().getId());
// 		}

// 		obj.setCreated(new Date());

// 		return mongoTemplate.save(obj);
// 	}

// 	public <T extends BaseEntityModel> void update(String id, Update update, Class<T> klass) {

// 		mongoTemplate.updateFirst(getAccountQuery(id, null, klass), update, klass);
// 	}

// //	public <T extends BaseEntityModel> T saveFullDocument(
// //			String id,
// //			Class<T> klass,
// //			UpdateAction<T> action
// //	) {
// //
// //		T obj = getById(id, klass, null);
// //		action.run(obj);
// //		return mongoTemplate.save(obj);
// //	}

// 	public <T extends BaseEntityModel> void updateMulti(DaoQuery daoQuery, Update update, Class<T> klass) {

// 		mongoTemplate.updateMulti(getAccountQuery(null, daoQuery, klass), update, klass);
// 	}

// 	public <T extends BaseEntityModel> void upsert(DaoQuery query, Update update, Class<T> klass) {

// 		mongoTemplate.upsert(getAccountQuery(null, query, klass), update, klass);
// 	}

// 	public <T extends BaseEntityModel> void delete(String id, Class<T> klass) {
// 		mongoTemplate.findAndRemove(getAccountQuery(id, null, klass), klass);
// 	}

// 	public <T extends BaseEntityModel> void delete(DaoQuery daoQuery, Class<T> klass) {
// 		mongoTemplate.findAndRemove(getAccountQuery(null, daoQuery, klass), klass);
// 	}

// 	private <T extends BaseEntityModel> ModelLevel getModelLevel(Class<T> klass){
// 		return ModelLevel.bash;
// 	}

// 	private<T extends BaseEntityModel> void appendSessionAbsentCriteria(Class<T> klass, Query query, List<CriteriaDefinition> criteriaDefinitions){
// 		List<String> keys = new ArrayList<>();
// 		if(Utils.isNotEmpty(criteriaDefinitions)){
// 			keys = criteriaDefinitions.stream().map(CriteriaDefinition::getKey).collect(Collectors.toList());
// 		}

// 		// condition only when calling from search method of dynamic server
// 		if(keys.contains("id")){
// 			return;
// 		}

// 		ModelLevel modelLevel = getModelLevel(klass);
// 		if(currentUserSession.getUserSession() != null) {

// 			switch (modelLevel) {
// 				// user query or data should be provided
// 				case user:
// 					if (!keys.contains("userId")) {
// 						query.addCriteria(Criteria.where("userId").is(currentUserSession.getUser().getId()));
// 					}
// //				case session:
// //					if (!keys.contains("sessionId")) {
// //						query.addCriteria(Criteria.where("sessionId").is(currentUserSession.getAcademicSession().getId()));
// //					}
// //				case branch:
// //					if (!keys.contains("workLocation")) {
// //						query.addCriteria(Criteria.where("workLocation").is(currentUserSession.getBranch().getId()));
// //					}
// 			}
// 			if (!keys.contains("accountId")) {
// 				query.addCriteria(Criteria.where("accountId").is(currentUserSession.getAccount().getId()));
// 			}
// 		}
// 	}

// 	private <T extends BaseEntityModel> Query getAccountQuery(String objId, DaoQuery daoQuery, Class<T> klass) {
// 		Query query = new Query();
// 		if (Utils.isNotEmpty(objId)) {
// 			query.addCriteria(Criteria.where("id").is(objId));
// 		}
// 		appendSessionAbsentCriteria(klass, query, daoQuery != null ? daoQuery.getCriteriaList() : null);
// 		if (daoQuery != null) {
// 			if (Utils.isNotEmpty(daoQuery.getCriteriaList())) {
// 				for (CriteriaDefinition criteria : daoQuery.getCriteriaList()) {
// 					query.addCriteria(criteria);
// 				}
// 			}
// 			if (Utils.isNotEmpty(daoQuery.getFields())) {
// 				for (String field : daoQuery.getFields()) {
// 					query.fields().include(field);
// 				}
// 			}
// 			if (daoQuery.getLimit() != null) {
// 				query.limit(daoQuery.getLimit());
// 			} else {
// 				if(!daoQuery.isGetAll()) {
// 					query.limit(10);
// 				}
// 			}
// 			if(daoQuery.getSort() != null){
// 				query.with(daoQuery.getSort());
// 			}
// 		}
// 		logger.debug("Final account query: {}", query);
// 		return query;
// 	}

// 	public <T extends BaseEntityModel> long getCount(Class<T> klass) {
// 		return mongoTemplate.count(getAccountQuery(null, null, klass), klass);
// 	}

// 	public <T extends BaseEntityModel> long getCount(DaoQuery daoQuery, Class<T> klass) {
// 		Query query = getAccountQuery(null, daoQuery, klass);
// 		return mongoTemplate.count(query, klass);
// 	}

// 	public <T> AggregationResults<T> aggregate(Class<? extends BaseEntityModel> inputType, Class<T> outputType, AggregationOperation... operations){
// 		List<AggregationOperation> accountOperations = new ArrayList<>();
// 		ModelLevel modelLevel = getModelLevel(inputType);
// 		switch (modelLevel) {
// 			case user:
// 				accountOperations.add(Aggregation.match(Criteria.where("userId").is(currentUserSession.getUser().getId())));
// //			case session:
// //				accountOperations.add(Aggregation.match(Criteria.where("sessionId").is(currentUserSession.getAcademicSession().getId())));
// //			case branch:
// //				accountOperations.add(Aggregation.match(Criteria.where("workLocation").is(currentUserSession.getBranch().getId())));
// 		}
// 		accountOperations.add(Aggregation.match(Criteria.where("accountId").is(currentUserSession.getAccount().getId())));
// 		accountOperations.addAll(Arrays.asList(operations));
// 		return mongoTemplate.aggregate(Aggregation.newAggregation(accountOperations), inputType, outputType);
// 	}

// }

// enum ModelLevel{
// 	user, session, branch, bash
// }