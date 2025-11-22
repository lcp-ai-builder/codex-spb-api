package com.lcp.spb.controller;

import com.lcp.spb.bean.request.LoginRequest;
import com.lcp.spb.bean.response.LoginResponse;
import com.lcp.spb.logic.services.LoginService;
import com.lcp.spb.logic.services.impls.LoginServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
public class LoginController {

  private final LoginService loginService;

  public LoginController(LoginServiceImpl loginService) {
    this.loginService = loginService;
  }

  @PostMapping
  public Mono<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    return loginService
        .authenticate(loginRequest.getUserId(), loginRequest.getPassword())
        .map(login -> new LoginResponse(true, "Login successful"))
        .defaultIfEmpty(new LoginResponse(false, "Invalid userId or password"));
  }
}
