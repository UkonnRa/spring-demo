package com.ukonnra.whiterabbit.core.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public record RangeQuery<T extends Comparable<?>>(
    @Nullable @JsonProperty(TYPE_GT) T gt, @Nullable @JsonProperty(TYPE_LT) T lt) {
  public static final String TYPE_GT = "$gt";
  public static final String TYPE_LT = "$lt";
}
