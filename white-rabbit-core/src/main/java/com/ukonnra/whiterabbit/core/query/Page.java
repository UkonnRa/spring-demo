package com.ukonnra.whiterabbit.core.query;

import java.util.List;
import org.springframework.lang.Nullable;

public record Page<E>(Info info, List<Item<E>> items) {
  public record Info(
      boolean hasPreviousPage,
      boolean hasNextPage,
      @Nullable String startCursor,
      @Nullable String endCursor) {}

  public record Item<E>(String cursor, E data) {}
}
