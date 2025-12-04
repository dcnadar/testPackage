package com.radyfy.common.service.crm.config.common_grids;

@FunctionalInterface
public interface CrmGridConsumer {
    void apply(CrmGridConsumerProps props);
}
