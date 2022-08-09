package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.ReadService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.ReadTaskHandler;
import com.ukonnra.whiterabbit.testsuite.ReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import java.util.Map;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

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
  protected void doHandle(final S suite, final Task.Read.FindOne<S, E, Q> task) {}

  @Override
  protected void doHandle(final S suite, final Task.Read.FindPage<S, E, Q> task) {}

  public static enum TaskType {
    FIND_ONE,
    FIND_ALL;
  }
}
