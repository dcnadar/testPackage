package com.radyfy.common.service.crm.config.model;

@FunctionalInterface
public interface CrmModelConsumer {
    void apply(CrmModelConsumerProps props);
}