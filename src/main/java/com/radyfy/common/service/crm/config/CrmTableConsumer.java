package com.radyfy.common.service.crm.config;

@FunctionalInterface
public interface CrmTableConsumer {
    void apply(CrmTableConsumerProps props);
}
