package com.lcp.spb.controller;

import com.lcp.spb.bean.Role;
import com.lcp.spb.bean.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/roles")
public class RoleController extends AbstractController {

  @GetMapping
  public Mono<PageResponse<Role>> listRoles(
      @RequestParam(name = "page", defaultValue = "1") long page,
      @RequestParam(name = "pageSize", defaultValue = "10") long pageSize) {
    logger.info("Listing roles - page: {}, pageSize: {}", page, pageSize);
    return roleService.findPage(page, pageSize);
  }

  @PostMapping
  public Mono<Role> createRole(@RequestBody Role role) {
    logger.info("Creating role {}", role.getCode());
    return roleService.create(role);
  }
}
