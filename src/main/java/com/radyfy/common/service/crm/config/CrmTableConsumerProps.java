package com.radyfy.common.service.crm.config;

import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.request.table.TableRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrmTableConsumerProps {

  private CrmTable crmTable;
  private CrmModel crmModel;
  private GridRequestParams gridRequestParams;
  private TableRequest tableRequest;
}
