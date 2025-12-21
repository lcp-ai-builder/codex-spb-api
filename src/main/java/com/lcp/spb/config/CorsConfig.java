package com.lcp.spb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS（跨域资源共享）配置类
 * 
 * <p>配置跨域请求策略，允许前端应用从不同域名访问后端 API。
 * 
 * <p>配置内容：
 * <ul>
 *   <li>允许所有来源：支持所有域名的跨域请求</li>
 *   <li>允许所有请求头：支持所有 HTTP 请求头</li>
 *   <li>允许的 HTTP 方法：GET、POST、PUT、DELETE、OPTIONS</li>
 *   <li>允许携带凭证：支持 Cookie 等认证信息</li>
 *   <li>预检请求缓存时间：3600秒（1小时）</li>
 * </ul>
 * 
 * <p>适用范围：所有路径（/**）
 * 
 * <p>注意：生产环境建议根据实际需求限制允许的来源域名，提高安全性。
 * 
 * @author lcp
 */
@Configuration
public class CorsConfig {

  /**
   * 配置 CORS 过滤器
   * 
   * <p>创建并配置 CORS 过滤器，用于处理跨域请求。
   * 该过滤器会拦截所有请求，根据配置的 CORS 策略添加相应的响应头。
   * 
   * @return CorsWebFilter 对象，用于处理跨域请求
   */
  @Bean
  public CorsWebFilter corsWebFilter () {
    CorsConfiguration config = new CorsConfiguration();
    // 允许所有来源的跨域请求
    config.setAllowedOriginPatterns(List.of(CorsConfiguration.ALL));
    // 允许所有请求头
    config.setAllowedHeaders(List.of(CorsConfiguration.ALL));
    // 允许的 HTTP 方法
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    // 允许携带凭证（如 Cookie）
    config.setAllowCredentials(true);
    // 预检请求的缓存时间（秒）
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // 对所有路径应用 CORS 配置
    source.registerCorsConfiguration("/**", config);
    return new CorsWebFilter(source);
  }
}
