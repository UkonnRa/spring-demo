package com.ukonnra.whiterabbit.endpoint.graphql.model;

import com.ukonnra.whiterabbit.core.query.Page;
import java.util.List;

public record GraphQlPage<E>(Page.Info pageInfo, List<Edge<E>> edges) {
  public GraphQlPage(Page<E> page) {
    this(page.info(), page.items().stream().map(Edge::new).toList());
  }

  public record Edge<E>(String cursor, E node) {
    public Edge(Page.Item<E> item) {
      this(item.cursor(), item.data());
    }
  }
}
