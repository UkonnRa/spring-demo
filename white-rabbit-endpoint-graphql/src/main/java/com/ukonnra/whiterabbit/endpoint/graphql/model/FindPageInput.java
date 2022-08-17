package com.ukonnra.whiterabbit.endpoint.graphql.model;

import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.endpoint.graphql.GraphQlApplicationError;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

public record FindPageInput(
    @Nullable String query,
    List<GraphQlOrder> sort,
    @Nullable Integer first,
    @Nullable String after,
    @Nullable Integer last,
    @Nullable String before,
    @Nullable Integer offset) {

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
