package com.radyfy.common.service.crm.config.main;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrmMainConfig {

  public static enum Event {
    ON_CONFIG_LOAD
  }

  private static final Logger logger = LoggerFactory.getLogger(CrmMainConfig.class);

  private final Map<Event, CrmMainConsumer> mainConsumers;

  public CrmMainConfig() {
    mainConsumers = new HashMap<>();
  }

  public void addEventListener(Event event, CrmMainConsumer crmFormConsumer) {
    if(mainConsumers.containsKey(event)){
      throw new RuntimeException("Main consumer already exists for event: " + event);
    }
    mainConsumers.put(event, crmFormConsumer);
  }

  public void runConsumer(Event event, CrmMainConsumerProps props) {
    CrmMainConsumer consumer = mainConsumers.get(event);
    if (consumer != null) {
      consumer.apply(props);
    }
  }
}
