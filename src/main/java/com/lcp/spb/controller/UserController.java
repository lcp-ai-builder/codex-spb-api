package com.lcp.spb.controller;

import com.lcp.spb.bean.User;
import com.lcp.spb.logic.services.UserService;
import com.lcp.spb.logic.services.impls.UserServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserServiceImpl userService) {
    this.userService = userService;
  }

  @GetMapping("/getUsers")
  public Flux<User> getUsers() {
    return userService.findAll();
  }

  @PostMapping
  public Mono<User> createUser(@RequestBody User user) {
    return userService.save(user);
  }
}
