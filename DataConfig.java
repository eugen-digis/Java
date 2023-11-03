package com.acme.processor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.aggregator.dao.telemetry.TelemetryValueRepository;
import com.acme.common.mapper.AttributeMapper;
import com.acme.common.mapper.TelemetryValueMapper;
import com.acme.processor.service.attribute.AttributeConfigMapper;
import com.acme.processor.service.telemetry.TelemetryMapper;
import com.acme.processor.task.ScheduledTask;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
@EnableScheduling
public class DataConfig {

  @Value("${server.ws.attribute_config}")
  private Resource attributeConfigMapper;
  @Value("${server.ws.telemetry_config}")
  private Resource telemetryConfigMapper;

  @Bean
  public AttributeConfigMapper attributeConfigMapper(ObjectMapper objectMapper) {
    return new AttributeConfigMapper(objectMapper, attributeConfigMapper);
  }

  @Bean
  public TelemetryMapper telemetryMapper(ObjectMapper objectMapper) {
    return new TelemetryMapper(objectMapper, telemetryConfigMapper);
  }

  @Bean
  public AttributeMapper attributeMapper() {
    return new AttributeMapper();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public TelemetryValueMapper telemetryValueMapper() {
    return new TelemetryValueMapper();
  }

  @Bean
  public TaskScheduler taskScheduler() {
    return new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
  }

  @Bean
  public ScheduledTask scheduledTask(TelemetryValueRepository telemetryValueRepository) {
    return new ScheduledTask(telemetryValueRepository);
  }
}
