package com.ukonnra.whiterabbit.endpoint.graphql.model;

import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.endpoint.graphql.GraphQlApplicationError;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

public record FindPageInput(
    String query,
    List<Order> sort,
    @Nullable Integer first,
    @Nullable String after,
    @Nullable Integer last,
    @Nullable String before,
    @Nullable Integer offset) {
  public record Order(String property, Sort.Direction direction) {}

  public Sort parsedSort() {
    return Sort.by(
        sort.stream().map(order -> new Sort.Order(order.direction, order.property)).toList());
  }

  public Pagination pagination() {
    if (first != null && last != null) {
      throw new GraphQlApplicationError.FirstLastBothExist();
    }

    return new Pagination(
        after,
        before,
        Optional.ofNullable(first)
            .or(() -> Optional.ofNullable(last))
            .orElse(Pagination.DEFAULT_SIZE),
        Optional.ofNullable(offset).orElse(0));
  }
}
