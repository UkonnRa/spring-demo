package com.ukonnra.whiterabbit.core;

import org.springframework.lang.Nullable;

public interface Command<C extends Command<C>> {
  String type();

  @Nullable
  String targetId();

  C withTargetId(@Nullable String targetId);
}
