package com.radyfy.common.model;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.account.AccountDBCredentials;
import com.radyfy.common.model.dao.MemoryCached;

@Getter
@Setter
@Document(collection = "account")
@MemoryCached
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account extends BaseEntityModel {

	private String name;
	private String status;
	private String aId;
	private Map<String, Object> data;

	private AccountDBCredentials dbCredentials;
}