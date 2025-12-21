package com.lcp.spb.logic.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import reactor.core.publisher.Flux;
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

    /**
     * 从 Elasticsearch 查询响应中提取命中结果
     * 
     * <p>通用的方法，用于从 SearchResponse 中提取 hits 列表并转换为 Flux 流。
     * 如果响应中没有 hits 或 hits 为空，则返回空的 Flux。
     * 
     * @param <T> 文档类型
     * @param response Elasticsearch 查询响应
     * @return Flux 流式返回命中结果
     */
    protected <T> Flux<Hit<T>> extractHits (
            co.elastic.clients.elasticsearch.core.SearchResponse<T> response) {
        return Flux.fromIterable(
                Optional.ofNullable(response.hits())
                        .map(searchHits -> searchHits.hits())
                        .orElseGet(List::of));
    }

    /**
     * 通用的保存文档方法
     * 
     * <p>将文档保存到 Elasticsearch 索引中，支持自动ID生成和ID回填。
     * 
     * <p>使用方式：
     * <pre>
     * return saveDocument(INDEX, document, 
     *     document::getId,           // 获取ID的方法
     *     (doc, id) -> doc.setId(id)  // 设置ID的方法
     * );
     * </pre>
     * 
     * @param <T> 文档类型
     * @param indexName 索引名称
     * @param document 要保存的文档对象
     * @param getIdFunc 从文档中获取ID的函数，如果返回null则自动生成ID
     * @param setIdFunc 设置文档ID的函数，用于回填ES生成的ID
     * @return Mono 包装的文档对象，包含保存后的ID
     */
    protected <T> Mono<T> saveDocument (
            String indexName,
            T document,
            Function<T, String> getIdFunc,
            BiConsumer<T, String> setIdFunc) {
        return fromBlocking(() -> elasticsearchClient.index(builder -> {
            builder.index(indexName).document(document);
            String id = getIdFunc.apply(document);
            if (id != null && !id.isEmpty()) {
                builder.id(id);
            }
            return builder;
        })).map(response -> {
            setIdFunc.accept(document, response.id());
            return document;
        });
    }

    /**
     * 通用的根据ID查询文档方法
     * 
     * <p>使用 Elasticsearch 的 get API 直接根据文档ID获取文档信息。
     * 
     * @param <T> 文档类型
     * @param indexName 索引名称
     * @param id 文档ID
     * @param documentClass 文档类型
     * @param setIdFunc 设置文档ID的函数，用于回填ES返回的ID
     * @return Mono 包装的文档对象，如果文档不存在则返回空 Mono
     */
    protected <T> Mono<T> findDocumentById (
            String indexName,
            String id,
            Class<T> documentClass,
            BiConsumer<T, String> setIdFunc) {
        return fromBlocking(() -> elasticsearchClient.get(
                g -> g.index(indexName).id(id), documentClass))
                .flatMap(response -> response.found()
                        ? Mono.justOrEmpty(applyId(response.source(), response.id(), setIdFunc))
                        : Mono.empty());
    }

    /**
     * 通用的删除文档方法
     * 
     * <p>根据文档ID从 Elasticsearch 索引中删除文档。
     * 
     * @param indexName 索引名称
     * @param id 文档ID
     * @return Mono 包装的布尔值，true 表示删除成功，false 表示文档不存在
     */
    protected Mono<Boolean> deleteDocumentById (String indexName, String id) {
        return fromBlocking(() -> elasticsearchClient.delete(d -> d.index(indexName).id(id)))
                .map(response -> response.result() == co.elastic.clients.elasticsearch._types.Result.Deleted);
    }

    /**
     * 应用ID到文档对象
     * 
     * @param <T> 文档类型
     * @param document 文档对象，可能为 null
     * @param id 文档ID
     * @param setIdFunc 设置ID的函数
     * @return 文档对象，如果输入为 null 则返回 null
     */
    private <T> T applyId (T document, String id, BiConsumer<T, String> setIdFunc) {
        if (document != null) {
            setIdFunc.accept(document, id);
        }
        return document;
    }

    /**
     * 从 Elasticsearch Hit 中提取文档并附加ID
     * 
     * <p>通用的方法，用于从 Hit 对象中提取文档，并将文档ID设置到文档对象中。
     * 如果文档对象中已有ID，则不会覆盖。
     * 
     * @param <T> 文档类型
     * @param hit Elasticsearch 查询命中结果
     * @param getIdFunc 从文档中获取ID的函数，用于判断是否已有ID
     * @param setIdFunc 设置文档ID的函数
     * @return 包含ID的文档对象，如果文档为 null 则返回 null
     */
    protected <T> T attachIdFromHit (
            Hit<T> hit,
            Function<T, String> getIdFunc,
            BiConsumer<T, String> setIdFunc) {
        T document = hit.source();
        if (document != null) {
            String existingId = getIdFunc.apply(document);
            if (existingId == null || existingId.isEmpty()) {
                setIdFunc.accept(document, hit.id());
            }
        }
        return document;
    }
}
