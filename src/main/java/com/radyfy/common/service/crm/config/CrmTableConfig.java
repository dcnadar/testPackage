package com.radyfy.common.service.crm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CrmTableConfig {

    private static final Logger logger = LoggerFactory.getLogger(CrmTableConfig.class);

    private final Map<String, CrmTableConsumer> onTableConsumers;
    private final Map<String, CrmTableConsumer> beforeTableConsumers;

    public CrmTableConfig(){
        onTableConsumers = new HashMap<>();
        beforeTableConsumers = new HashMap<>();
    }

    public void onTableLoad(String id, CrmTableConsumer crmTableConsumer){
        if(onTableConsumers.containsKey(id)){
            throw new RuntimeException("Table consumer already exists for id: " + id);
        }
        onTableConsumers.put(id, crmTableConsumer);
    }


    public void beforeTableLoad(String id, CrmTableConsumer crmTableConsumer){
        if(onTableConsumers.containsKey(id)){
            throw new RuntimeException("Table consumer already exists for id: " + id);
        }
        beforeTableConsumers.put(id, crmTableConsumer);
    }

    public CrmTableConsumer getOnTableConsumer(String id){
       return onTableConsumers.get(id);
    }

    public CrmTableConsumer getBeforeTableConsumer(String id){
        return beforeTableConsumers.get(id);
     }
}
