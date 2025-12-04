package com.radyfy.common.model.crm.role;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.BaseEntityModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "permission")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission extends BaseEntityModel{
  private String name;
  private String key;
  private String description;
  private List<String> apisAccess;
}
