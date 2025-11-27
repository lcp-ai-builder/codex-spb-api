package com.lcp.spb.controller;

import com.lcp.spb.bean.request.LoginRequest;
import com.lcp.spb.bean.response.LoginResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
public class LoginController extends AbstractController {

  @PostMapping
  public Mono<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    return loginService
        .authenticate(loginRequest.getUserId(), loginRequest.getPassword())
        .map(login -> new LoginResponse(true, "Login successful"))
        .defaultIfEmpty(new LoginResponse(false, "Invalid userId or password"));
  }
}
