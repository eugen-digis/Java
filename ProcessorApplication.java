package com.acme.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.acme.*")
@EntityScan(basePackages = "com.acme.aggregator.dao.*")
@EnableJpaRepositories(basePackages= "com.acme.aggregator.dao.*")
public class ProcessorApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessorApplication.class, args);
  }
}
