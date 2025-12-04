package com.radyfy.common.model.crm.api;

import java.io.Serializable;
import java.util.List;

import com.radyfy.common.model.crm.api.CrmApi.CrmApiAction;
import com.radyfy.common.model.crm.api.CrmApi.FileType;
import com.radyfy.common.model.crm.grid.table.GridParam;
import com.radyfy.common.model.enums.grid.GridType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EcomApiMapping implements Serializable {
  private String ecomAccountId;
  private CrmApiAction action;
  private GridType gridType;
  private FileType downloadFileType;
  private Boolean getAllRecords;
  private Integer recordsLimit;

  // only for action != ENTITY_DELETE
  private String sourceId;
  // only for action == ENTITY_DELETE
  private String modelId;

  // for gridType menu
  private GridType innerGridType;
  private String innerSourceId;

  // for internal use
  private String name;
  private String path;
  private ApiType apiType;

    // only for action == ENTITY_DELETE or ENTITY_GET
    List<GridParam> gridParams;

    // only for action == ENTITY_GET
    private Boolean fetchDocWithoutId;
}
