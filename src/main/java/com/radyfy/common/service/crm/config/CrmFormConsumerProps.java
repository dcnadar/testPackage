package com.radyfy.common.service.crm.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Update;

import com.radyfy.common.model.crm.grid.CrmForm;
import com.radyfy.common.model.crm.grid.GridRequestParams;
import com.radyfy.common.model.crm.model.CrmModel;
import com.radyfy.common.model.dao.DaoQuery;

@Getter
@AllArgsConstructor
public class CrmFormConsumerProps {
    private final CrmForm crmForm;
    private final CrmModel crmModel;
    private final GridRequestParams gridRequestParams;
    private final Document finalDoc;
    private final DaoQuery daoQuery;
    private final Update update;
    private Object returnValue;

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
