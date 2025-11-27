package com.lcp.spb.logic.services.impls;

import com.lcp.spb.bean.Role;
import com.lcp.spb.bean.response.PageResponse;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.RoleService;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("roleServices")
public class RoleServiceImpl extends AbstractMapperService implements RoleService {

  @Override
  public Mono<PageResponse<Role>> findPage(long page, long pageSize) {
    long safePage = Math.max(page, 1);
    long safePageSize = Math.max(pageSize, 1);
    long offset = (safePage - 1) * safePageSize;

    return fromBlocking(
        () -> {
          List<Role> records = roleMapper.findPage(offset, safePageSize);
          long total = roleMapper.countAll();
          return new PageResponse<>(records, total, safePage, safePageSize);
        });
  }

  @Override
  public Mono<Role> create(Role role) {
    return fromBlocking(
        () -> {
          roleMapper.insert(role);
          return role;
        });
  }
}
