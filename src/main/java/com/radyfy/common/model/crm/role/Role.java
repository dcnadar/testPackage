package com.radyfy.common.model.crm.role;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.BaseEntityModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "role")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role extends BaseEntityModel{
  
  private String name;
  private List<String> permissions;
}
