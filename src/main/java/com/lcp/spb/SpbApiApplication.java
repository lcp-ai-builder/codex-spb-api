package com.lcp.spb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 应用程序主入口类
 * 
 * <p>这是一个基于 Spring Boot 3.5.7 和 WebFlux 的响应式 API 服务，
 * 主要用于处理加密货币交易数据的存储、查询和实时推送功能。
 * 
 * <p>主要功能包括：
 * <ul>
 *   <li>Elasticsearch 集成：提供交易数据和用户数据的 CRUD 操作</li>
 *   <li>WebSocket 支持：实时推送交易汇总信息</li>
 *   <li>定时任务：定期计算并推送最近一小时的交易汇总</li>
 *   <li>响应式编程：基于 Reactor 的非阻塞异步处理</li>
 * </ul>
 * 
 * @author lcp
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication(scanBasePackages = "com.lcp.spb")
@EnableScheduling
public class SpbApiApplication {

  /** 日志记录器 */
  private static final Logger log = LoggerFactory.getLogger(SpbApiApplication.class);

  /**
   * 应用程序主入口方法
   * 
   * @param args 命令行参数
   */
  public static void main (String[] args) {
    SpringApplication.run(SpbApiApplication.class, args);
  }

  /**
   * 应用程序启动完成后的回调 Bean
   * 
   * <p>在 Spring Boot 应用完全启动后执行，用于记录启动成功的日志信息。
   * 
   * @return CommandLineRunner 实例，在应用启动后执行日志记录
   */
  @Bean
  CommandLineRunner logStartup () {
    return args -> log.info("api service started successfully");
  }
}
