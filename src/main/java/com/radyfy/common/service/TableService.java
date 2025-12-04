package com.radyfy.common.service;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.radyfy.common.config.mongo.CollectionMongoTemplateFactory;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.request.table.ColumnFilter;
import com.radyfy.common.request.table.ColumnSort;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.response.TableResult;
import com.radyfy.common.utils.Utils;
import com.radyfy.common.utils.ValidationUtils;

@Component
public class TableService {

    private static final Logger logger = LoggerFactory.getLogger(TableService.class);

    private final CollectionMongoTemplateFactory collectionMongoTemplateFactory;

    @Autowired
    public TableService(
            CollectionMongoTemplateFactory collectionMongoTemplateFactory) {
        this.collectionMongoTemplateFactory = collectionMongoTemplateFactory;
    }

    public TableResult<Document> table(TableRequest tableRequest, CrmModel crmModel) {
        return table(tableRequest, crmModel, new Query());
    }

    public TableResult<Document> table(TableRequest tableRequest, CrmModel crmModel, Query query) {

        MongoTemplate mongoTemplate = collectionMongoTemplateFactory.forCollection(crmModel.getCollectionName());
        tableRequest.removeDuplicateFilterKeys();

        if (Utils.isNotEmpty(tableRequest.getAdditionalCriterias())) {
            tableRequest.getAdditionalCriterias().forEach(query::addCriteria);
        }

        if (Utils.isNotEmpty(tableRequest.getFilters())) {
            for (Entry<String, List<ColumnFilter>> e : tableRequest.getFilters().entrySet()) {
                String k = e.getKey();
                List<ColumnFilter> v = e.getValue();
                if (Utils.isNotEmpty(v) && Utils.isNotEmpty(v)) {
                    Criteria c = Criteria.where(k);
                    for (ColumnFilter f : v) {
                        if (ColumnFilter.valid(f)) {
                            Object value = f.getValue();
                            if (value instanceof String && k.equals("_id")
                                    && ValidationUtils.isValidHexID(value.toString())) {
                                value = new ObjectId((String) value);
                            }
                            switch (f.getOperator()) {

                                case EQUALS:
                                    c.is(value);
                                    break;

                                case NOT_EQUALS:
                                    c.ne(value);
                                    break;

                                case EXISTS:
                                    c.exists((Boolean) value);
                                    break;

                                case STARTS_WITH:
                                    c.regex("^" + value, "i");
                                    break;

                                case CONTAINS:
                                    c.regex(".*" + value + ".*", "si");
                                    break;

                                case IN:
                                    c.in((List<?>) value);
                                    break;

                                case NOT_IN:
                                    c.nin((List<?>) value);
                                    break;

                                case AFTER:
                                    c.gte(parseDate(value));
                                    break;

                                case BEFORE:
                                    c.lte(parseDate(value));
                                    break;

                                case FROM:
                                    c.gte(parseDate(value));
                                    break;

                                case TO:
                                    c.lte(parseDate(value));
                                    break;

                                case GREATER_THAN:
                                    c.gt(value);
                                    break;

                                case LESS_THAN:
                                    c.lt(value);
                                    break;
                            }
                        }
                    }
                    query.addCriteria(c);
                }
            }
        }

        if (Utils.isNotEmpty(tableRequest.getFields())) {
            tableRequest.getFields().forEach(f -> query.fields().include(f));
        }

        logger.debug("final table Query  collectionName: {}, search: {}", crmModel.getCollectionName(),
                query.toString());
        if (tableRequest.isScroll()) {

            if (ColumnSort.valid(tableRequest.getSort())) {
                query.with(tableRequest.getSort().get());
            }

            int modLimit = tableRequest.getS() + 1;
            query.limit(modLimit);
            query.skip(((long) tableRequest.getP() * tableRequest.getS()));
            List<Document> data = mongoTemplate.find(query, Document.class, crmModel.getCollectionName());
            boolean hasMore = false;
            if (data.size() == modLimit) {
                hasMore = true;
                data.remove(tableRequest.getS());
            }
            return new TableResult<>(data, hasMore);

        } else {
            long total;
            List<Document> data;
            if (ColumnSort.valid(tableRequest.getSort())) {
                query.with(tableRequest.getSort().get());
            } else {
                query.with(Sort.by(Sort.Direction.DESC, "created"));
            }
            if (tableRequest.getS() > 0 && tableRequest.getP() >= 0) {
                total = mongoTemplate.count(query, crmModel.getCollectionName());
                query.limit(tableRequest.getS());
                query.skip((long) tableRequest.getP() * tableRequest.getS());
                data = mongoTemplate.find(query, Document.class, crmModel.getCollectionName());
            } else if ((tableRequest.getS() == 0 && tableRequest.getP() == -1)) {
                // CONTINUING: This case is only valid to get all data
                data = mongoTemplate.find(query, Document.class, crmModel.getCollectionName());
                total = data.size();
            } else {
                throw new RuntimeException("Table pagination not valid");
            }

            return new TableResult<>(data, total);
        }
    }

    private Date parseDate(Object value) {
        Date date = Utils.parseDate(value);
        if (date == null) {
            throw new RuntimeException("Filter date not valid");
        }
        return date;
    }
}
