package com.radyfy.common.service.crm.config.main;

@FunctionalInterface
public interface CrmMainConsumer {
    void apply(CrmMainConsumerProps props);
}