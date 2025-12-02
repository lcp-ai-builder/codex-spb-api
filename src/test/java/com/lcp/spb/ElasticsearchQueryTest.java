package com.lcp.spb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.lcp.spb.bean.UserDoc;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ElasticsearchQueryTest {

  private final Logger logging = LoggerFactory.getLogger(ElasticsearchQueryTest.class);

  private static final String INDEX = "user-data";

  @Autowired
  private ElasticsearchClient elasticsearchClient;

  @Test
  void queryUserDataIndex() throws IOException {
    assumeTrue(elasticsearchClient.ping().value(), "Elasticsearch not reachable");
    assumeTrue(elasticsearchClient.indices().exists(r -> r.index(INDEX)).value(),
        "Index user-data is missing");

    var response = elasticsearchClient.search(
        s -> s.index(INDEX).query(q -> q.matchAll(m -> m)).size(100),
        UserDoc.class);

    List<UserDoc> users = response.hits().hits().stream()
        .map(Hit::source)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    users.forEach(user -> {
      logging.info("User: {}", user.toString());
    });

    assertNotNull(response);
    assertEquals(0, response.shards().failed(), "Search should not have failed shards");
    assertTrue(users.size() >= 0, "Should convert search hits to user list");
  }
}
