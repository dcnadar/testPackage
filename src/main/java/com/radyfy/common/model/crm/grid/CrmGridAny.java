package com.radyfy.common.model.crm.grid;

import org.springframework.data.mongodb.core.mapping.Document;

import com.radyfy.common.model.dao.MemoryCached;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MemoryCached
@Document(collection = "crm_grid")
public class CrmGridAny extends CrmGrid{}
