package com.example.demo.user;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Flux<User> findAll() {
        return Flux.defer(() -> Flux.fromIterable(userMapper.selectList(null)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> {
            userMapper.insert(user);
            return user;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
