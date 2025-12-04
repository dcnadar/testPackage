package com.radyfy.common.service.crm.media;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.request.CrmMediaCreateRequest;
import com.radyfy.common.request.table.ColumnSort;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.request.table.ColumnSort.SortOrder;
import com.radyfy.common.response.TableResult;
import com.radyfy.common.service.crm.EntityOrmDao;

import java.util.Arrays;

@Service
public class CrmMediaService {

  private final EntityOrmDao entityOrmDao;

  public CrmMediaService(EntityOrmDao entityOrmDao) {
    this.entityOrmDao = entityOrmDao;
  }

  public Document createMedia(CrmMediaCreateRequest media) {
    CrmModel crmModel = new CrmModel();
    crmModel.setCollectionName(CollectionNames.media);

    Document mediaDoc = new Document();
    mediaDoc.append("name", media.getName());
    mediaDoc.append("type", media.getType());
    mediaDoc.append("size", media.getSize());
    mediaDoc.append("path", media.getPath());

    return entityOrmDao.create(mediaDoc, crmModel);
  }

  public CrmTable getMedia(TableRequest tableRequest) {
    tableRequest.setFields(Arrays.asList("id", "name", "type", "size", "path", "created"));
    tableRequest.setSort(new ColumnSort("created", SortOrder.descend));

    CrmModel crmModel = new CrmModel();
    crmModel.setCollectionName(CollectionNames.media);

    TableResult<Document> tableResult = entityOrmDao.table(tableRequest, crmModel);
    CrmTable crmTable = new CrmTable();
    crmTable.setData(tableResult.getData());
    crmTable.setTotal(tableResult.getTotal());
    crmTable.setMeta(tableResult.getMeta());
    return crmTable;
  }

}
