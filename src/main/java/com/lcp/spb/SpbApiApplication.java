package com.lcp.spb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.lcp.spb")
public class SpbApiApplication {

  private static final Logger log = LoggerFactory.getLogger(SpbApiApplication.class);

  public static void main (String[] args) {
    SpringApplication.run(SpbApiApplication.class, args);
  }

  @Bean
  CommandLineRunner logStartup () {
    return args -> log.info("api service started successfully");
  }
}
