package com.lcp.spb.controller;

import com.lcp.spb.bean.response.IndexResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class IndexController extends AbstractController {

  @GetMapping("/index")
  public Mono<IndexResponse> index() {
    logger.info("asdasdasdadasdasd");

    return Mono.just(new IndexResponse("success"));
  }
}
