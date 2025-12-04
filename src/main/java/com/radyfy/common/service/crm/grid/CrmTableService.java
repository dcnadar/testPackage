package com.radyfy.common.service.crm.grid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.stereotype.Component;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.grid.table.CrmTable;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;
import com.radyfy.common.model.enums.grid.GridType;
import com.radyfy.common.request.table.TableRequest;
import com.radyfy.common.service.MetaOrmDao;
import com.radyfy.common.service.crm.config.ConfigBuilder;
import com.radyfy.common.service.crm.config.CrmTableConfig;
import com.radyfy.common.service.crm.config.CrmTableConsumer;
import com.radyfy.common.service.crm.config.CrmTableConsumerProps;

import java.util.ArrayList;
import java.util.List;

@Component
public class CrmTableService {

    private final MetaOrmDao metaOrmDao;
    private final CrmTableConfig crmTableConfig;

    @Autowired
    public CrmTableService(
            MetaOrmDao metaOrmDao,
            ConfigBuilder configBuilder
    ){
        this.metaOrmDao = metaOrmDao;
        this.crmTableConfig = configBuilder.getTableConfig();
    }


    public CrmTable getById(String id, boolean isCommonApi){
        if(isCommonApi){
            return metaOrmDao.getById(id, CrmTable.class, DaoQuery.keyValue(Constants.ACCOUNT_ID, Constants.RADYFY_ACCOUNT_ID));
        }
        return metaOrmDao.getById(id, CrmTable.class, null);
    }

    public CrmTable getByModelId(String modelId, boolean isCommonApi){

        List<CriteriaDefinition> criteriaList = new ArrayList<>();
        if(isCommonApi){
            criteriaList.add(Criteria.where(Constants.ACCOUNT_ID).is(Constants.RADYFY_ACCOUNT_ID));
        }
        criteriaList.add(Criteria.where("crmModelId").is(modelId));
        criteriaList.add(Criteria.where("gridType").is(GridType.table));
        DaoQuery daoQuery = DaoQuery.builder().criteriaList(criteriaList).build();

        return metaOrmDao.findOneByQuery(daoQuery, CrmTable.class);
    }

    public void runOnLoadConsumer(CrmTable crmTable, CrmModel crmModel, GridRequestParams gridRequestParams, TableRequest tableRequest){
        CrmTableConsumer crmTableConsumer = crmTableConfig.getOnTableConsumer(crmTable.getId());
        if(crmTableConsumer != null){
            crmTableConsumer.apply(new CrmTableConsumerProps(crmTable, crmModel, gridRequestParams, tableRequest));
        }
    }

    public void runBeforeLoadConsumer(CrmTable crmTable, CrmModel crmModel, GridRequestParams gridRequestParams, TableRequest tableRequest){
        CrmTableConsumer crmTableConsumer = crmTableConfig.getBeforeTableConsumer(crmTable.getId());
        if(crmTableConsumer != null){
            crmTableConsumer.apply(new CrmTableConsumerProps(crmTable, crmModel, gridRequestParams, tableRequest));
        }
    }
}
