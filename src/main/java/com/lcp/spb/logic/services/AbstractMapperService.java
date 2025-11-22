package com.lcp.spb.logic.services;

public abstract class AbstractMapperService<M> extends BaseService {

    protected final M mapper;

    protected AbstractMapperService(M mapper) {
        this.mapper = mapper;
    }
}
