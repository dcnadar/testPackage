package com.radyfy.common.service.crm.config.menu;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CrmMenuConfig {
  
    public static enum Event{
      BEFORE_LOAD, ON_LOAD
    }

    private static final Logger logger = LoggerFactory.getLogger(CrmMenuConfig.class);

    private final Map<Event, Map<String, CrmMenuConsumer>> consumers;

    public CrmMenuConfig(){
        consumers = new HashMap<>();
        for (Event event: Event.values()) {
            consumers.put(event, new HashMap<>());
        }
    }

    public void addEventListener(Event event, String crmMenuId, CrmMenuConsumer crmMenuConsumer){
        if(consumers.get(event).containsKey(crmMenuId)){
            throw new RuntimeException("Menu consumer already exists for id: " + crmMenuId);
          }
        consumers.get(event).put(crmMenuId, crmMenuConsumer);
    }

    public void runConsumer(Event event, CrmMenuConsumerProps props){
        CrmMenuConsumer consumer = consumers.get(event).get(props.getCrmMenu().getId());
        if(consumer != null){
            consumer.apply(props);
        }
    }
}
