package com.lcp.spb.logic.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lcp.spb.bean.Login;
import com.lcp.spb.logic.dao.LoginMapper;
import com.lcp.spb.logic.services.impls.LoginServiceImpl;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

  @Mock private LoginMapper loginMapper;

  @InjectMocks private LoginServiceImpl loginService;

  @Test
  void createPersistsLogin() {
    Login login = new Login();
    login.setUserId("zhang3");
    String hashedPassword = sha256("aaaaaa");
    login.setPassword(hashedPassword);

    when(loginMapper.insert(any(Login.class))).thenReturn(1);

    StepVerifier.create(loginService.create(login))
        .expectNextMatches(saved
                           -> "zhang3".equals(saved.getUserId()) &&
                                  hashedPassword.equals(saved.getPassword()))
        .verifyComplete();

    verify(loginMapper).insert(any(Login.class));
  }

  @Test
  void insertLoginReturnsRowCount() {
    Login login = new Login();
    login.setUserId("zhang3");
    login.setPassword(sha256("aaaaaa"));

    when(loginMapper.insert(any(Login.class))).thenReturn(1);

    StepVerifier.create(loginService.insertLogin(login))
        .expectNext(1)
        .verifyComplete();

    verify(loginMapper).insert(any(Login.class));
  }

  @Test
  void authenticateReturnsLoginWhenMatch() {
    Login login = new Login();
    login.setUserId("10");
    login.setPassword("hashed-pass");

    when(loginMapper.selectById("10")).thenReturn(login);

    StepVerifier.create(loginService.authenticate("10", "hashed-pass"))
        .expectNextMatches(found -> "10".equals(found.getUserId()))
        .verifyComplete();

    verify(loginMapper).selectById("10");
  }

  @Test
  void authenticateEmptyWhenUserMissing() {
    when(loginMapper.selectById("11")).thenReturn(null);

    StepVerifier.create(loginService.authenticate("11", "any"))
        .verifyComplete();

    verify(loginMapper).selectById("11");
  }

  @Test
  void authenticateEmptyWhenPasswordMismatch() {
    Login login = new Login();
    login.setUserId("12");
    login.setPassword("stored-hash");

    when(loginMapper.selectById("12")).thenReturn(login);

    StepVerifier.create(loginService.authenticate("12", "wrong-hash"))
        .verifyComplete();

    verify(loginMapper).selectById("12");
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hashed.length * 2);
      for (byte b : hashed) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Missing SHA-256 algorithm", e);
    }
  }
}
