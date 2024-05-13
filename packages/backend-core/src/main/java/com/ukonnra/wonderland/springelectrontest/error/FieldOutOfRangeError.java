package com.ukonnra.wonderland.springelectrontest.error;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class FieldOutOfRangeError extends ResponseStatusException {
  private final String entityType;
  private final String field;
  private final int min;
  private final int max;

  public FieldOutOfRangeError(String entityType, String field, int min, int max) {
    super(
        HttpStatus.BAD_REQUEST,
        String.format("%s[%s] should between %d and %d", entityType, field, min, max));
    this.setTitle("FieldOutOfRangeError");

    this.entityType = entityType;
    this.field = field;
    this.min = min;
    this.max = max;

    this.setType(URI.create("urn:wonderland:white-rabbit:errors:fields-out-of-range"));
    this.getBody()
        .setProperties(
            Map.of(
                "entityType", this.entityType,
                "field", this.field,
                "min", this.min,
                "max", this.max));
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
}
