package com.lcp.spb;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.lcp.spb.bean.UserDoc;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ElasticsearchSeedTest {

  private static final String INDEX = "user-data";

  @Autowired
  private ElasticsearchClient elasticsearchClient;

  @BeforeEach
  void ensureElasticsearchIsAvailable() throws IOException {
    assumeTrue(elasticsearchClient.ping().value(), "Elasticsearch not reachable");
    createIndexIfMissing();
  }

  @Test
  void insertRandomUserDocuments() throws IOException {
    int docsToInsert = 30;
    long beforeCount = elasticsearchClient.count(c -> c.index(INDEX)).count();

    for (int i = 0; i < docsToInsert; i++) {
      elasticsearchClient.index(builder -> builder
          .index(INDEX)
          .id(UUID.randomUUID().toString())
          .document(buildRandomUser()));
    }

    long afterCount = elasticsearchClient.count(c -> c.index(INDEX)).count();
    assertEquals(docsToInsert, afterCount - beforeCount,
        "Should have added the expected number of documents");
  }

  private UserDoc buildRandomUser() {
    UserDoc doc = new UserDoc();
    doc.setUserName(randomChineseName());
    doc.setAge(ThreadLocalRandom.current().nextInt(20, 60));
    doc.setEmail(randomEmail());
    doc.setIsVip(ThreadLocalRandom.current().nextBoolean());
    doc.setDescription(randomChineseDescription());
    return doc;
  }

  private void createIndexIfMissing() throws IOException {
    boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
    if (exists) {
      return;
    }

    elasticsearchClient.indices().create(c -> c
        .index(INDEX)
        .mappings(m -> m
            .properties("user_name", p -> p.text(t -> t
                .fields("keyword", kb -> kb.keyword(k -> k))))
            .properties("age", p -> p.integer(i -> i))
            .properties("email", p -> p.keyword(k -> k))
            .properties("is_vip", p -> p.boolean_(b -> b))
            .properties("description", p -> p.text(t -> t))));
  }

  private String randomChineseName() {
    List<String> familyNames = List.of("张", "李", "王", "赵", "刘", "陈", "杨", "黄", "周", "吴");
    List<String> givenNames = List.of("晨", "杰", "华", "琳", "芳", "静", "翔", "凯", "敏", "倩");
    String family = familyNames.get(ThreadLocalRandom.current().nextInt(familyNames.size()));
    String given = givenNames.get(ThreadLocalRandom.current().nextInt(givenNames.size()));
    if (ThreadLocalRandom.current().nextBoolean()) {
      given += givenNames.get(ThreadLocalRandom.current().nextInt(givenNames.size()));
    }
    return family + given;
  }

  private String randomEmail() {
    String local = randomAlphaLower(6, 10);
    String domain = List.of("example.com", "mail.com", "lcp.com")
        .get(ThreadLocalRandom.current().nextInt(3));
    return local + "@" + domain;
  }

  private String randomChineseDescription() {
    List<String> hobbies = List.of("喜欢看书", "热爱编程", "享受旅游", "偶尔运动", "学习烹饪", "听音乐", "摄影记录");
    List<String> traits = List.of("乐观", "好学", "专注", "开朗", "细心", "爱分享");
    String hobby = hobbies.get(ThreadLocalRandom.current().nextInt(hobbies.size()));
    String trait = traits.get(ThreadLocalRandom.current().nextInt(traits.size()));
    return hobby + "，" + trait + "的技术爱好者";
  }

  private String randomAlphaLower(int minLen, int maxLen) {
    int len = ThreadLocalRandom.current().nextInt(minLen, maxLen + 1);
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = (char) ('a' + ThreadLocalRandom.current().nextInt(26));
      sb.append(c);
    }
    return sb.toString();
  }
}
