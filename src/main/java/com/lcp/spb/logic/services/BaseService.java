package com.lcp.spb.logic.services;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 服务层抽象基类
 * 
 * <p>提供所有服务类共用的基础设施，包括：
 * <ul>
 *   <li>Elasticsearch 客户端：用于与 Elasticsearch 集群进行交互</li>
 *   <li>日志记录器：统一的日志记录能力</li>
 *   <li>阻塞调用包装：将同步阻塞操作转换为响应式非阻塞操作</li>
 * </ul>
 * 
 * <p>由于 Elasticsearch 客户端是同步阻塞的，而本应用使用响应式编程模型（WebFlux），
 * 因此需要通过 {@link #fromBlocking(Callable)} 方法将阻塞调用包装到弹性线程池中执行，
 * 避免阻塞事件循环线程，保证系统的响应性能。
 * 
 * @author lcp
 */
public abstract class BaseService {

    /** 日志记录器，子类可直接使用 */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Elasticsearch 客户端，用于执行索引、查询、删除等操作 */
    @Autowired
    protected ElasticsearchClient elasticsearchClient;

    /**
     * 将阻塞调用包装为响应式 Mono
     * 
     * <p>该方法用于将同步阻塞操作（如 Elasticsearch 客户端调用）转换为响应式非阻塞操作。
     * 通过将阻塞操作调度到弹性线程池（boundedElastic）中执行，避免阻塞事件循环线程，
     * 从而保证系统的整体响应性能。
     * 
     * <p>使用场景：
     * <ul>
     *   <li>Elasticsearch 的同步 API 调用</li>
     *   <li>其他可能阻塞的 I/O 操作</li>
     * </ul>
     * 
     * <p>注意事项：
     * <ul>
     *   <li>弹性线程池会根据负载动态调整线程数量</li>
     *   <li>适合处理可能阻塞但执行时间不确定的操作</li>
     *   <li>不应用于 CPU 密集型计算任务</li>
     * </ul>
     * 
     * @param <T> 返回值的类型
     * @param action 需要执行的阻塞操作，通过 Callable 接口封装
     * @return 包装后的 Mono，操作将在弹性线程池中异步执行
     */
    protected <T> Mono<T> fromBlocking (Callable<T> action) {
        return Mono.fromCallable(action).subscribeOn(Schedulers.boundedElastic());
    }
}
