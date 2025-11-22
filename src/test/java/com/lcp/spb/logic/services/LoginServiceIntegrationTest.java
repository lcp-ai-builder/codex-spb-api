package com.lcp.spb.logic.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lcp.spb.bean.Login;
import com.lcp.spb.logic.dao.LoginMapper;
import com.lcp.spb.logic.services.impls.LoginServiceImpl;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LoginServiceIntegrationTest {

  private static final String ALLOWED =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  @Autowired private LoginServiceImpl loginService;

  @Autowired private LoginMapper loginMapper;

  @Test
  void insertLoginPersistsToDatabase() {

    // 批量写入真实数据库，便于人工核对 insertLogin 行为
    IntStream.range(0, 10).forEach(i -> {
      String userId = "zhang3-" + randomAlphaNumeric(10);
      String hashedPassword = sha256(randomAlphaNumeric(16));

      Login login = new Login();
      login.setUserId(userId);
      login.setPassword(hashedPassword);

      loginService.insertLogin(login).block();
    });

    // loginMapper.deleteById(userId);
  }

  private String randomAlphaNumeric(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int idx = RANDOM.nextInt(ALLOWED.length());
      sb.append(ALLOWED.charAt(idx));
    }
    return sb.toString();
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
