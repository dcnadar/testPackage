package com.radyfy.common.request.table;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radyfy.common.utils.Utils;

@Getter
@Setter
@ToString
@Slf4j
public class TableRequest {

	private int p = 0;
	private int s = 10;
	private String q;
	private boolean isScroll;

	private ColumnSort sort;
	private Map<String, List<ColumnFilter>> filters;
	private Map<String, Object> meta;

	// non api data
	// private String accountId;
	// private String workLocation;
	// private String sessionId;
	private List<CriteriaDefinition> additionalCriterias;
	private List<String> fields;

	public TableRequest() {
		super();
	}

	public Set<String> getFilterKeys() {
		Set<String> keys = new HashSet<>();
		if (Utils.isNotEmpty(additionalCriterias)) {
			keys.addAll(additionalCriterias.stream().map(CriteriaDefinition::getKey).collect(Collectors.toSet()));
		}
		if (Utils.isNotEmpty(filters)) {
			keys.addAll(filters.keySet());
		}
		return keys;
	}

	public void removeDuplicateFilterKeys() {
		Set<String> keys = new HashSet<>();
		if (Utils.isNotEmpty(additionalCriterias)) {
			keys.addAll(additionalCriterias.stream().map(CriteriaDefinition::getKey).collect(Collectors.toSet()));
		}
		if (Utils.isNotEmpty(filters)) {
			keys.forEach(k -> {
				if (filters.containsKey(k)) {
					filters.remove(k);
				}
			});
		}
	}

	public void appendCriteria(CriteriaDefinition criteria) {
		if (additionalCriterias == null) {
			additionalCriterias = new ArrayList<>();
		}
		additionalCriterias.add(criteria);
	}

	public static TableRequest fromPayload(String payload) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(payload, new TypeReference<TableRequest>() {
			});
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("Failed to decode payload: " + e.getMessage());
		}
	}

	public static Map<String, List<ColumnFilter>> decodeFilters(String filters) {

		String decoded = URLDecoder.decode(filters, StandardCharsets.UTF_8);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(decoded, new TypeReference<Map<String, List<ColumnFilter>>>() {
			});
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("Failed to decode filters: " + e.getMessage());
		}
	}

	public static ColumnSort decodeSort(String sort) {

		String decoded = URLDecoder.decode(sort, StandardCharsets.UTF_8);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(decoded, new TypeReference<ColumnSort>() {
			});
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("Failed to decode sort: " + e.getMessage());
		}
	}
}
