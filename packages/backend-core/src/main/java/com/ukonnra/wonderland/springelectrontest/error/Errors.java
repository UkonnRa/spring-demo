package com.ukonnra.wonderland.springelectrontest.error;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class Errors extends AbstractError {
  public final Collection<? extends AbstractError> errors;

  private static <T extends AbstractError> Stream<AbstractError> flat(T error) {
    if (error instanceof Errors errors) {
      return errors.errors.stream().flatMap(Errors::flat);
    }
    return Stream.of(error);
  }

  public static @Nullable AbstractError of(Collection<? extends AbstractError> errors) {
    final var flatten = errors.stream().flatMap(Errors::flat).toList();
    if (flatten.isEmpty()) {
      return null;
    } else if (flatten.size() == 1) {
      return flatten.getFirst();
    } else {
      return new Errors(flatten);
    }
  }

  protected Errors(Collection<? extends AbstractError> errors) {
    super("Multiple errors found");
    this.errors = errors;
  }

  @Override
  public String getTitle() {
    return "Errors";
  }

  @Override
  public Map<String, Object> getProperties() {
    final var details = this.errors.stream().map(AbstractError::getJson).toList();
    return Map.of("errors", details);
  }
}
