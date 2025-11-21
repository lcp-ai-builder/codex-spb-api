package com.example.demo.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void findAllEmitsUsers() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");

        when(userMapper.selectList(null)).thenReturn(List.of(user));

        StepVerifier.create(userService.findAll())
                .expectNextMatches(found -> "Alice".equals(found.getName()))
                .verifyComplete();

        verify(userMapper).selectList(null);
    }

    @Test
    void saveEmitsInsertedUser() {
        User user = new User();
        user.setName("Bob");
        user.setEmail("bob@example.com");

        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return 1;
        });

        StepVerifier.create(userService.save(user))
                .expectNextMatches(saved -> saved.getId() == 2L && "Bob".equals(saved.getName()))
                .verifyComplete();

        verify(userMapper).insert(any(User.class));
    }
}

