package com.radyfy.common.service.crm.config.search;

@FunctionalInterface
public interface CrmSearchConsumer {
    void apply(CrmSearchConsumerProps props);
}