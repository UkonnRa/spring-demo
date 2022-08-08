package com.ukonnra.whiterabbit.endpoint.graphql.model;

import java.util.List;
import org.springframework.data.domain.Sort;

public record GraphQlOrder(String property, Sort.Direction direction) {
  public static Sort parseToModel(final List<GraphQlOrder> sort) {
    return Sort.by(
        sort.stream().map(order -> new Sort.Order(order.direction, order.property)).toList());
  }
}
