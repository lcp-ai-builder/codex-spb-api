package com.lcp.spb.logic.services;

import java.util.concurrent.Callable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public abstract class BaseService {

    // 将阻塞调用包装到弹性线程池，避免堵塞事件线程
    protected <T> Mono<T> fromBlocking (Callable<T> action) {
        return Mono.fromCallable(action).subscribeOn(Schedulers.boundedElastic());
    }
}
