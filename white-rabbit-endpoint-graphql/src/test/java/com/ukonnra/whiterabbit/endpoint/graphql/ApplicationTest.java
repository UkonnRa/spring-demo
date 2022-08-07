package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.query.Page;
import com.ukonnra.whiterabbit.endpoint.graphql.model.FindPageInput;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureHttpGraphQlTester
@ContextConfiguration(
    classes = {TestSuiteApplicationConfiguration.class, GraphQlApplicationConfiguration.class})
class ApplicationTest {
  private final HttpGraphQlTester tester;
  private final DataGenerator dataGenerator;

  @Autowired
  public ApplicationTest(HttpGraphQlTester tester, DataGenerator dataGenerator) {
    this.tester = tester;
    this.dataGenerator = dataGenerator;
  }

  @Test
  void testGraphQl() {
    this.dataGenerator.prepareData();

    {
      final var response =
        tester.documentName("findUser").variable("query", "{\"role\": \"ADMIN\"}").execute();

      response.errors().verify();
      response.path("user.id").entity(String.class).satisfies(UUID::fromString);
      response
        .path("user.version")
        .entity(Integer.class)
        .satisfies(i -> Assertions.assertTrue(i >= 0));
    }

    {
      final var response =
        tester.documentName("findUsers").variable("query", "{\"role\": \"ADMIN\"}")
          .variable("sort", List.of(new FindPageInput.Order("name", Sort.Direction.ASC))).execute();

      response.errors().verify();
      response.path("users.pageInfo").entity(Page.Info.class).satisfies(info -> {
        Assertions.assertFalse(info.hasPreviousPage());
        Assertions.assertTrue(info.hasNextPage());
      });
      response.path("users.edges").entityList(GraphQlPage.Edge.class).satisfies(edges -> {
        Assertions.assertFalse(edges.isEmpty());
        for (final var edge : edges) {
          Assertions.assertFalse(edge.cursor().isEmpty());
          Assertions.assertNotNull(edge.node());
        }
      });
    }
  }
}
