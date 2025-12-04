package com.radyfy.common.service.crm.config.search;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrmSearchConfig {
  
    public static enum Event{
      BEFORE_SEARCH, AFTER_SEARCH
    }

    private static final Logger logger = LoggerFactory.getLogger(CrmSearchConfig.class);

    private final Map<Event, Map<String, CrmSearchConsumer>> consumers;

    public CrmSearchConfig(){
        consumers = new HashMap<>();
        for (Event event: Event.values()) {
            consumers.put(event, new HashMap<>());
        }
    }

    public void addEventListener(Event event, String collectionName, CrmSearchConsumer crmFormConsumer){
        if(consumers.get(event).containsKey(collectionName)){
            throw new RuntimeException("Search consumer already exists for id: " + collectionName);
        }
        consumers.get(event).put(collectionName, crmFormConsumer);
    }

    public void runConsumer(Event event, CrmSearchConsumerProps props){
      CrmSearchConsumer consumer = consumers.get(event).get(props.getCrmModel().getCollectionName());
        if(consumer != null){
            consumer.apply(props);
        }
    }
}
