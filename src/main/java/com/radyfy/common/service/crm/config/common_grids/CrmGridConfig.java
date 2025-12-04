package com.radyfy.common.service.crm.config.common_grids;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrmGridConfig {
  
    public static enum Event{
      ON_LOAD
    }

    private static final Logger logger = LoggerFactory.getLogger(CrmGridConfig.class);

    private final Map<Event, Map<String, CrmGridConsumer>> consumers;

    public CrmGridConfig(){
        consumers = new HashMap<>();
        for (Event event: Event.values()) {
            consumers.put(event, new HashMap<>());
        }
    }

    public void addEventListener(Event event, String crmFormId, CrmGridConsumer crmFormConsumer){
        if(consumers.get(event).containsKey(crmFormId)){
            throw new RuntimeException("Grid consumer already exists for id: " + crmFormId);
        }
        consumers.get(event).put(crmFormId, crmFormConsumer);
    }

    public void runConsumer(Event event, CrmGridConsumerProps props){
        CrmGridConsumer consumer = consumers.get(event).get(props.getCrmGrid().getId());
        if(consumer != null){
            consumer.apply(props);
        }
    }
}
