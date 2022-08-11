package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.ReadService;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlOrder;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.ReadTaskHandler;
import com.ukonnra.whiterabbit.testsuite.ReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@Slf4j
public class GraphQlReadTaskHandler<S extends ReadTestSuite<S, E, Q>, E, Q extends Query>
    extends ReadTaskHandler<S, E, Q> {
  private final ObjectMapper objectMapper;
  private final HttpGraphQlTester tester;
  private final Map<TaskType, String> graphQlNames;

  GraphQlReadTaskHandler(
      HttpGraphQlTester tester,
      ReadService<E, Q> service,
      ObjectMapper objectMapper,
      Map<TaskType, String> graphQlNames) {
    super(service);
    this.tester = tester;
    this.objectMapper = objectMapper;
    this.graphQlNames = graphQlNames;
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindOne<S, E, Q> task) {
    final var input = task.input().apply(suite);
    final var operatorName = this.graphQlNames.get(TaskType.FIND_ONE);
    suite.setAuthentication(input.authUser());
    try {
      final var response =
          tester
              .documentName(operatorName)
              .variable("query", this.objectMapper.writeValueAsString(input.query()))
              .execute();
      response.errors().verify();

      log.info("{} result: {}", operatorName, response.path("user").hasValue());
    } catch (JsonProcessingException e) {
      Assertions.fail(e);
    }
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindPage<S, E, Q> task) {
    final var input = task.input().apply(suite);
    final var operatorName = this.graphQlNames.get(TaskType.FIND_ALL);
    suite.setAuthentication(input.authUser());
    try {
      final var request =
          tester
              .documentName(operatorName)
              .variable("query", this.objectMapper.writeValueAsString(input.query()))
              .variable(
                  "sort",
                  input.sort().stream()
                      .map(sort -> new GraphQlOrder(sort.getProperty(), sort.getDirection()))
                      .toList())
              .variable("offset", input.pagination().offset());

      if (input.pagination().after() == null && input.pagination().before() == null) {
        request.variable("first", input.pagination().size());
      } else if (input.pagination().after() == null) {
        request.variable("last", input.pagination().size());
        request.variable("before", input.pagination().before());
      } else if (input.pagination().before() == null) {
        request.variable("first", input.pagination().size());
        request.variable("after", input.pagination().after());
      } else {
        request.variable("first", input.pagination().size());
        request.variable("after", input.pagination().after());
        request.variable("before", input.pagination().before());
      }

      final var response = request.execute();
      response.errors().verify();
      log.info(
          "{} result: {}",
          operatorName,
          response
              .path("users")
              .entity(
                  new ParameterizedTypeReference<GraphQlPage<UserEntity, UserEntity.Dto>>() {}));
    } catch (JsonProcessingException e) {
      Assertions.fail(e);
    }
  }

  public enum TaskType {
    FIND_ONE,
    FIND_ALL;
  }
}
