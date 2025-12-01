package com.lcp.spb.controller;

import com.lcp.spb.bean.Operator;
import com.lcp.spb.bean.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/operators")
public class OperatorController extends AbstractController {

  @GetMapping
  public Mono<PageResponse<Operator>> listOperators(
      @RequestParam(name = "page", defaultValue = "1") long page,
      @RequestParam(name = "pageSize", defaultValue = "10") long pageSize) {
    logger.info("Listing operators - page: {}, pageSize: {}", page, pageSize);
    return operatorService.findPage(page, pageSize);
  }

  @PostMapping
  public Mono<Operator> createOperator(@RequestBody Operator operator) {
    logger.info("Creating operator {}", operator.getLoginName());
    return operatorService.create(operator);
  }

  @PutMapping("/{id}")
  public Mono<Operator> updateOperator(@PathVariable("id") Long operatorId,
      @RequestBody Operator operator) {
    logger.info("Updating operator {}", operatorId);
    operator.setId(operatorId);
    return operatorService.update(operator);
  }

  @PutMapping("/{id}/is-open")
  public Mono<Operator> updateOperatorIsOpen(@PathVariable("id") Long operatorId,
      @RequestParam("isOpen") Integer isOpen) {
    logger.info("Updating operator {} isOpen to {}", operatorId, isOpen);
    return operatorService.updateIsOpen(operatorId, isOpen);
  }
}
