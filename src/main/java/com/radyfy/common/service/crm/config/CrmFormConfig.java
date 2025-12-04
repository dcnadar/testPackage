package com.radyfy.common.service.crm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CrmFormConfig {

    public static enum Event{
        BEFORE_LOAD, ON_LOAD, BEFORE_CREATE, AFTER_CREATE, BEFORE_UPDATE, AFTER_UPDATE
    }

    private static final Logger logger = LoggerFactory.getLogger(CrmFormConfig.class);

    private final Map<Event, Map<String, CrmFormConsumer>> consumers;

    public CrmFormConfig(){
        consumers = new HashMap<>();
        for (Event event: Event.values()) {
            consumers.put(event, new HashMap<>());
        }
    }

    public void addEventListener(Event event, String crmFormId, CrmFormConsumer crmFormConsumer){
        if(consumers.get(event).containsKey(crmFormId)){
            throw new RuntimeException("Form consumer already exists for id: " + crmFormId);
        }
        consumers.get(event).put(crmFormId, crmFormConsumer);
    }

    public Object runConsumer(Event event, CrmFormConsumerProps props){
        CrmFormConsumer consumer = consumers.get(event).get(props.getCrmForm().getId());
        if(consumer != null){
            consumer.apply(props);
            return props.getReturnValue();
        }
        return null;
    }
}
