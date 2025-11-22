package com.lcp.spb.logic.services;

public abstract class AbstractMapperService<M> extends BaseService {

    protected final M mapper;

    // 子类直接复用持久层 mapper
    protected AbstractMapperService(M mapper) {
        this.mapper = mapper;
    }
}
