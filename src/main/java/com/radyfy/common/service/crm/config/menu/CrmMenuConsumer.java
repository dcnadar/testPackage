package com.radyfy.common.service.crm.config.menu;

@FunctionalInterface
public interface CrmMenuConsumer {
    void apply(CrmMenuConsumerProps props);
}
