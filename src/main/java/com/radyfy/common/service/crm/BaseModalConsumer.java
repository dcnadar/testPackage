package com.radyfy.common.service.crm;

import com.radyfy.common.model.crm.model.BaseCrmModel;

@FunctionalInterface
public interface BaseModalConsumer{
    void apply(BaseCrmModel crmModel);
}