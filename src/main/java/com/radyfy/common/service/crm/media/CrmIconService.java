package com.radyfy.common.service.crm.media;

import java.util.Arrays;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.radyfy.common.commons.CollectionNames;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.request.CrmIconRequest;
import com.radyfy.common.request.table.ColumnSort;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.request.table.ColumnSort.SortOrder;
import com.radyfy.common.response.TableResult;
import com.radyfy.common.service.crm.EntityOrmDao;

@Service
public class CrmIconService {

    private final EntityOrmDao entityOrmDao;

    public CrmIconService(EntityOrmDao entityOrmDao) {
        this.entityOrmDao = entityOrmDao;
    }

    public Document createIcon(CrmIconRequest iconRequest) {

        CrmModel iconModel = new CrmModel();
        iconModel.setCollectionName(CollectionNames.ICON);

        Document iconDoc = new Document();
        iconDoc.append("name", iconRequest.getName());
        iconDoc.append("type", iconRequest.getType());
        iconDoc.append("icon", iconRequest.getIcon());

        return entityOrmDao.create(iconDoc, iconModel);


    }

    public CrmTable getIcons(TableRequest tableRequest) {


        tableRequest.setFields(Arrays.asList("id", "name", "type", "icon", "created"));
        tableRequest.setSort(new ColumnSort("created", SortOrder.descend));

        CrmModel iconModel = new CrmModel();
        iconModel.setCollectionName(CollectionNames.ICON);

        TableResult<Document> tableResult = entityOrmDao.table(tableRequest, iconModel);
        CrmTable crmTable = new CrmTable();
        crmTable.setData(tableResult.getData());
        crmTable.setTotal(tableResult.getTotal());
        crmTable.setMeta(tableResult.getMeta());
        return crmTable;
    }

}
