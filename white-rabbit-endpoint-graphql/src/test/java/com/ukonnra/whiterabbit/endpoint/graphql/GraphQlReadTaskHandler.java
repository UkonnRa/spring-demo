package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.ReadService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlOrder;
import com.ukonnra.whiterabbit.testsuite.ReadTaskHandler;
import com.ukonnra.whiterabbit.testsuite.ReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@Slf4j
public class GraphQlReadTaskHandler<S extends ReadTestSuite<S, E, Q, D>, E, Q extends Query, D>
    extends ReadTaskHandler<S, E, Q, D> {
  private final ObjectMapper objectMapper;
  private final HttpGraphQlTester tester;
  private final GraphQlParams<D> graphQlParams;

  GraphQlReadTaskHandler(
      HttpGraphQlTester tester,
      ReadService<E, Q, D> service,
      ObjectMapper objectMapper,
      GraphQlParams<D> graphQlParams) {
    super(service);
    this.tester = tester;
    this.objectMapper = objectMapper;
    this.graphQlParams = graphQlParams;
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindOne<S, Q, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    try {
      final var response =
          tester
              .documentName(this.graphQlParams.operatorNames().get(GraphQlParams.TaskType.FIND_ONE))
              .variable("query", this.objectMapper.writeValueAsString(input.query()))
              .execute();
      response.errors().verify();
      task.checker()
          .accept(new CheckerInput<>(input, this.graphQlParams.findOneMapper().apply(response)));
    } catch (JsonProcessingException e) {
      Assertions.fail(e);
    }
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindPage<S, Q, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    try {
      final var request =
          tester
              .documentName(this.graphQlParams.operatorNames().get(GraphQlParams.TaskType.FIND_ALL))
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
      task.checker().accept(new CheckerInput<>(input, this.graphQlParams.getPage(response)));
    } catch (JsonProcessingException e) {
      Assertions.fail(e);
    }
  }
}
