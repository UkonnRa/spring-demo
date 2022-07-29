package com.ukonnra.whiterabbit.endpoint.graphql;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class ApplicationTest {
  private final ApplicationContext context;

  @Autowired
  public ApplicationTest(ApplicationContext context) {
    this.context = context;
  }

  @Test
  void testGraphQl() {
    final var client =
        WebTestClient.bindToApplicationContext(this.context)
            .configureClient()
            .baseUrl("/graphql")
            .build();

    final var tester = HttpGraphQlTester.create(client);

    final var response =
        tester
            .document(
                """
      {
        user {
          id
          version
        }
      }
      """)
            .execute();

    response.path("user.id").entity(String.class).satisfies(UUID::fromString);
    response.path("user.version").entity(Integer.class);
  }
}
