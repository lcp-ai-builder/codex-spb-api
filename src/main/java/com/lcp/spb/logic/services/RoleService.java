package com.lcp.spb.logic.services;

import com.lcp.spb.bean.Role;
import com.lcp.spb.bean.response.PageResponse;
import reactor.core.publisher.Mono;

public interface RoleService {

  Mono<PageResponse<Role>> findPage(long page, long pageSize);

  Mono<Role> create(Role role);

  Mono<Role> update(Role role);

  Mono<Role> updateIsOpen(Long roleId, Integer isOpen);
}
