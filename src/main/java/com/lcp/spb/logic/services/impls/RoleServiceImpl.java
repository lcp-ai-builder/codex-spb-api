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
    PageBounds bounds = normalizePage(page, pageSize);

    return fromBlocking(
        () -> {
          List<Role> records = roleMapper.findPage(bounds.getOffset(), bounds.getPageSize());
          long total = roleMapper.countAll();
          return new PageResponse<>(records, total, bounds.getPage(), bounds.getPageSize());
        });
  }

  @Override
  public Mono<Role> create(Role role) {
    return fromBlocking(
        () -> {
          // Let database auto-generate primary key
          role.setId(null);
          roleMapper.insert(role);
          return role;
        });
  }

  @Override
  public Mono<Role> update(Role role) {
    return fromBlocking(
        () -> {
          roleMapper.updateById(role);
          return roleMapper.selectById(role.getId());
        });
  }

  @Override
  public Mono<Role> updateStatus(Long roleId, String status) {
    return fromBlocking(
        () -> {
          roleMapper.updateStatusById(roleId, status);
          return roleMapper.selectById(roleId);
        });
  }
}
