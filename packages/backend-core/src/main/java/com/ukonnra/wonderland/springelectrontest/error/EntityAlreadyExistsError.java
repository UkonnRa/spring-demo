package com.ukonnra.wonderland.springelectrontest.error;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class EntityAlreadyExistsError extends ResponseStatusException {
  private final String type;
  private final Map<String, String> values;

  public EntityAlreadyExistsError(String type, Map<String, String> values) {
    super(
        HttpStatus.CONFLICT,
        String.format(
            "%s[%s] already exists",
            type,
            values.entrySet().stream()
                .map(e -> String.format("%s = %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", "))));
    this.setTitle("EntityAlreadyExistError");

    this.type = type;
    this.values = values;
    this.getBody()
        .setProperties(
            Map.of(
                "type", this.type,
                "values", this.values));
  }

  public EntityAlreadyExistsError(String type, String field, String value) {
    this(type, Map.of(field, value));
  }

  public EntityAlreadyExistsError(String type, String id) {
    this(type, "id", id);
  }
}
