package com.lcp.spb.logic.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lcp.spb.bean.Login;
import com.lcp.spb.logic.dao.LoginMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock private LoginMapper loginMapper;

  @InjectMocks private LoginService loginService;

  @Test
  void createPersistsLogin() {
    Login login = new Login();
    login.setUserId(9L);
    login.setPassword("hash-9");

    when(loginMapper.insert(any(Login.class))).thenReturn(1);

    StepVerifier.create(loginService.createNewLoginInfo(login))
        .expectNextMatches(saved
                           -> saved.getUserId() == 9L &&
                                  "hash-9".equals(saved.getPassword()))
        .verifyComplete();

    verify(loginMapper).insert(any(Login.class));
  }

  @Test
  void authenticateReturnsLoginWhenMatch() {
    Login login = new Login();
    login.setUserId(10L);
    login.setPassword("hashed-pass");

    when(loginMapper.selectById(10L)).thenReturn(login);

    StepVerifier.create(loginService.authenticate(10L, "hashed-pass"))
        .expectNextMatches(found -> found.getUserId() == 10L)
        .verifyComplete();

    verify(loginMapper).selectById(10L);
  }

  @Test
  void authenticateEmptyWhenUserMissing() {
    when(loginMapper.selectById(11L)).thenReturn(null);

    StepVerifier.create(loginService.authenticate(11L, "any")).verifyComplete();

    verify(loginMapper).selectById(11L);
  }

  @Test
  void authenticateEmptyWhenPasswordMismatch() {
    Login login = new Login();
    login.setUserId(12L);
    login.setPassword("stored-hash");

    when(loginMapper.selectById(12L)).thenReturn(login);

    StepVerifier.create(loginService.authenticate(12L, "wrong-hash"))
        .verifyComplete();

    verify(loginMapper).selectById(12L);
  }
}
