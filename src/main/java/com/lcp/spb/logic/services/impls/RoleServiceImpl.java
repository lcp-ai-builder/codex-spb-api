package com.lcp.spb.logic.services.impls;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcp.spb.bean.Role;
import com.lcp.spb.bean.response.PageResponse;
import com.lcp.spb.logic.services.AbstractMapperService;
import com.lcp.spb.logic.services.RoleService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("roleServices")
public class RoleServiceImpl extends AbstractMapperService implements RoleService {

  @Override
  public Mono<PageResponse<Role>> findPage(long page, long pageSize) {
    var bounds = normalizePage(page, pageSize);

    return fromBlocking(
        () -> {
          Page<Role> pageRequest = new Page<>(bounds.getPage(), bounds.getPageSize());
          Page<Role> resultPage = roleMapper.selectPage(pageRequest, null);
          return new PageResponse<>(
              resultPage.getRecords(),
              resultPage.getTotal(),
              resultPage.getCurrent(),
              resultPage.getSize());
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
  public Mono<Role> updateIsOpen(Long roleId, Integer isOpen) {
    return fromBlocking(
        () -> {
          roleMapper.updateIsOpenById(roleId, isOpen);
          return roleMapper.selectById(roleId);
        });
  }
}
