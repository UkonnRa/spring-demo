package com.ukonnra.whiterabbit.endpoint.graphql.model;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.query.Page;
import java.util.List;

public record GraphQlPage<E extends AbstractEntity<D>, D>(Page.Info pageInfo, List<Edge<D>> edges) {
  public GraphQlPage(Page<E> page) {
    this(
        page.info(),
        page.items().stream().map(item -> new Edge<>(item.cursor(), item.data().toDto())).toList());
  }

  public record Edge<E>(String cursor, E node) {
    public Edge(Page.Item<E> item) {
      this(item.cursor(), item.data());
    }
  }
}
