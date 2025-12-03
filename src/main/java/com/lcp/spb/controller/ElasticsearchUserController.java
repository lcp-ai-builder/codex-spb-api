package com.lcp.spb.controller;

import com.lcp.spb.bean.EsUser;
import com.lcp.spb.logic.services.ElasticsearchUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController @RequestMapping("/es/users")
public class ElasticsearchUserController {

  @Autowired
  private ElasticsearchUserService elasticsearchUserService;

  @GetMapping
  public Flux<EsUser> listUsers () {
    return elasticsearchUserService.findAll();
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<EsUser>> getUser (@PathVariable String id) {
    return elasticsearchUserService
        .findById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Mono<EsUser> createUser (@RequestBody EsUser user) {
    return elasticsearchUserService.save(user);
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<EsUser>> updateUser (
      @PathVariable String id, @RequestBody EsUser user) {
    return elasticsearchUserService
        .update(id, user)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteUser (@PathVariable String id) {
    return elasticsearchUserService
        .delete(id)
        .map(deleted -> deleted
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build());
  }
}
