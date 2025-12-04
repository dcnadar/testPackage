package com.radyfy.common.model.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import com.radyfy.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class DaoQuery {

    private List<CriteriaDefinition> criteriaList;
    private Integer limit;
    private List<String> fields;
    private boolean getAll;
    private Sort sort;
    private boolean returnNewOnFindAndModify;
    private List<String> excludeFilters;

    public static DaoQuery fromId(String _id) {
        return DaoQuery.builder().criteriaList(
                new ArrayList<>(Collections.singletonList(
                        Criteria.where("_id").is(new ObjectId(_id)))))
                .build();
    }

    public static DaoQuery fromCriteria(Criteria criteria) {
        return DaoQuery.builder().criteriaList(
                new ArrayList<>(Collections.singletonList(criteria))).build();
    }

    public static DaoQuery keyValue(String key, Object value) {
        return DaoQuery.builder().criteriaList(
                new ArrayList<>(Collections.singletonList(
                        Criteria.where(key).is(value))))
                .build();
    }

    public Set<String> getFilterKeys() {
        Set<String> keys = new HashSet<>();
        if (Utils.isNotEmpty(criteriaList)) {
            keys.addAll(criteriaList.stream().map(CriteriaDefinition::getKey).collect(Collectors.toSet()));
        }
        if(Utils.isNotEmpty(excludeFilters)){
            keys.addAll(excludeFilters);
        }
        return keys;
    }

    public boolean hasFilterKey(String key) {
        if (Utils.isNotEmpty(criteriaList)) {
            return criteriaList.stream().anyMatch(c -> c.getKey().equals(key));
        }
        return false;
    }

    public void addCriteria(Criteria criteria) {
        if (criteriaList == null) {
            criteriaList = new ArrayList<>();
        }
        criteriaList.add(criteria);
    }
}
