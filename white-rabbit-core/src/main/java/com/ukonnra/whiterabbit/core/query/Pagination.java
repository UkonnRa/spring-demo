package com.ukonnra.whiterabbit.core.query;

import org.springframework.lang.Nullable;

public record Pagination(@Nullable String after, @Nullable String before, int size, int offset) {
  public static final int DEFAULT_SIZE = 10;

  public static final Pagination DEFAULT = new Pagination(null, null, DEFAULT_SIZE, 0);
}
