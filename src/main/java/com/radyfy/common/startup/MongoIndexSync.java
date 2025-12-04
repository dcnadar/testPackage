package com.radyfy.common.startup;
// package com.doonvalley.startup;

// import com.doonvalley.model.crm.model.CrmBasicModel;
// import com.doonvalley.model.crm.model.CrmModel;
// import com.doonvalley.model.crm.model.CrmModelIndex;
// import com.doonvalley.model.crm.model.CrmModelType;
// import com.doonvalley.utils.Utils;
// import com.mongodb.client.ListIndexesIterable;
// import com.mongodb.client.MongoCollection;
// import com.mongodb.client.model.IndexOptions;
// import com.mongodb.client.model.Indexes;
// import org.bson.BsonDocument;
// import org.bson.BsonInt32;
// import org.bson.Document;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.data.mongodb.core.query.Criteria;
// import org.springframework.data.mongodb.core.query.Query;
// import org.springframework.stereotype.Component;

// import javax.annotation.PostConstruct;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// @Component
// public class MongoIndexSync {

//     @Autowired
//     private MongoTemplate mongoTemplate;

// //    @PostConstruct
//     public void init() {
//         String accountId = "6440564214f06d99ab952c28";
//         Query query = new Query();
//         query.addCriteria(Criteria.where("accountId").is(accountId));
//         query.addCriteria(Criteria.where("modelType").is(CrmModelType.COLLECTION));
//         query.addCriteria(Criteria.where("modelStatus").is(CrmBasicModel.ModelStatus.ACTIVE));
//         List<CrmModel> collectionModels = mongoTemplate.find(query, CrmModel.class);
//         if(Utils.isNotEmpty(collectionModels)){
//             collectionModels.forEach(crmModel -> {
//                 MongoCollection<Document> collection = mongoTemplate.getCollection(crmModel.getCollectionName());
//                 ListIndexesIterable<Document> indexes = collection.listIndexes();
//                 if(Utils.isNotEmpty(crmModel.getIndexes())){
//                     for (Document index : indexes) {
//                         Optional<CrmModelIndex> crmModelIndex = crmModel.getIndexes().stream().filter(cmi -> cmi.getName().equals(index.getString("name"))).findAny();
//                         if(crmModelIndex.isPresent()){
//                             CrmModelIndex modelIndex = crmModelIndex.get();
//                             Document dbIndexKey = index.get("key", Document.class);
//                             boolean matched = true;
//                             for (Map.Entry<String, Integer> keyField: modelIndex.getKeys().entrySet()) {
//                                 String field = keyField.getKey();
//                                 if (dbIndexKey.containsKey(field)) {
//                                     if(!dbIndexKey.getInteger(field).equals(keyField.getValue())){
//                                         matched = false;
//                                         break;
//                                     }
//                                 } else {
//                                     matched = false;
//                                     break;
//                                 }
//                             }
//                             if(!matched){
//                                 collection.dropIndex(modelIndex.getName());
//                                 createIndex(collection, modelIndex);
//                             }

//                         } else {
//                             collection.dropIndex(index.getString("name"));
//                         }
//                     }
//                 } else {
//                     if(Utils.isNotEmpty(crmModel.getIndexes())){
//                         crmModel.getIndexes().forEach(modelIndex -> createIndex(collection, modelIndex));
//                     }
//                 }
//             });
//         }
//     }

//     private void createIndex(MongoCollection<Document> collection, CrmModelIndex modelIndex){
//         IndexOptions indexOptions = new IndexOptions().name(modelIndex.getName());
//         indexOptions.unique(Utils.isTrue(modelIndex.getUnique()));
//         Map<String, Integer> keys = modelIndex.getKeys();
//         if(keys.size() == 1){
//             modelIndex.getKeys().forEach((key, value) -> {
//                 if(value == 1){
//                     collection.createIndex(Indexes.ascending(key), indexOptions);
//                 } else if (value == -1){
//                     collection.createIndex(Indexes.descending(key), indexOptions);
//                 } else {
//                     throw new RuntimeException("Invalid index sort");
//                 }
//             });
//         } else {
//             BsonDocument compoundIndex = new BsonDocument();
//             modelIndex.getKeys().forEach((key, value) -> {
//                 compoundIndex.append(key, new BsonInt32(value));
//             });
//             collection.createIndex(compoundIndex, indexOptions);
//         }
//     }
// }
