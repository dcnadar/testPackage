package com.radyfy.common.service.crm.config.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrmModelConfig {

  public static enum Event {
    BEFORE_GET, AFTER_GET, BEFORE_DELETE, AFTER_DELETE;
  }

  private static final Logger logger = LoggerFactory.getLogger(CrmModelConfig.class);

  private final Map<Event, Map<String, CrmModelConsumer>> consumers;

  public CrmModelConfig() {
    consumers = new HashMap<>();
    for (Event event : Event.values()) {
      consumers.put(event, new HashMap<>());
    }
  }

  public void addEventListener(Event event, String modelId, CrmModelConsumer crmFormConsumer) {
    if(consumers.get(event).containsKey(modelId)){
      throw new RuntimeException("Model consumer already exists for id: " + modelId);
    }
    consumers.get(event).put(modelId, crmFormConsumer);
  }

  public void runConsumer(Event event, CrmModelConsumerProps props) {
    CrmModelConsumer consumer = consumers.get(event).get(props.getCrmModel().getId());
    if (consumer != null) {
      consumer.apply(props);
    }
  }
}
