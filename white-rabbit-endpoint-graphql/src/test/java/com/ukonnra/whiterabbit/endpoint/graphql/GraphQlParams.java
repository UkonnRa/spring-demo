package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.query.Page;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.graphql.test.tester.GraphQlTester;

public record GraphQlParams<D>(
    Map<TaskType, String> operatorNames,
    Function<GraphQlTester.Response, Optional<D>> findOneMapper,
    Function<GraphQlTester.Response, GraphQlPage<D>> findPageMapper) {
  public Page<D> getPage(final GraphQlTester.Response response) {
    final var page = this.findPageMapper.apply(response);
    return new Page<>(
        page.pageInfo(),
        page.edges().stream().map(edge -> new Page.Item<>(edge.cursor(), edge.node())).toList());
  }

  public enum TaskType {
    FIND_ONE,
    FIND_ALL;
  }
}
