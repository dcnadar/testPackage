package com.radyfy.common.service.crm.config;

@FunctionalInterface
public interface CrmFormConsumer {
    void apply(CrmFormConsumerProps props);
}
