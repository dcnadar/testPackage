package com.radyfy.common.service.crm.config.model;

import org.bson.Document;

import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.CrmModel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrmModelConsumerProps {
  private final CrmModel crmModel;
  private final GridRequestParams params;
  private final Document entity;
}
