package com.lcp.spb.controller;

import com.lcp.spb.bean.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {

  @GetMapping("/getUsers")
  public Flux<User> getUsers() {
    return userService.findAll();
  }

  @PostMapping
  public Mono<User> createUser(@RequestBody User user) {
    return userService.save(user);
  }
}
