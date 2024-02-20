package com.ukonnra.wonderland.springelectrontest.error;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class FieldOutOfRangeError extends AbstractError {
  private final String type;
  private final String field;
  private final int min;
  private final int max;

  public FieldOutOfRangeError(String type, String field, int min, int max) {
    super(String.format("%s[%s] should between %d and %d", type, field, min, max));
    this.type = type;
    this.field = field;
    this.min = min;
    this.max = max;
  }

  public static @Nullable FieldOutOfRangeError of(
      String type, final ConstraintViolation<?> violation) {
    final var descriptor = violation.getConstraintDescriptor();
    if (descriptor.getAnnotation().annotationType() == Size.class) {
      final var values = descriptor.getAttributes();
      return new FieldOutOfRangeError(
          type,
          violation.getPropertyPath().toString(),
          values.get("min") instanceof Integer i ? i : 0,
          values.get("max") instanceof Integer i ? i : 0);
    }
    return null;
  }

  @Override
  public String getTitle() {
    return "FieldOutOfRangeError";
  }

  @Override
  public Map<String, Object> getProperties() {
    return Map.of(
        "type", this.type,
        "field", this.field,
        "min", this.min,
        "max", this.max);
  }
}
