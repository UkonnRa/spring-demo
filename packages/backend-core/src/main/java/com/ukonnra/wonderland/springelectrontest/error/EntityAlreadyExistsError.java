package com.ukonnra.wonderland.springelectrontest.error;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

public final class EntityAlreadyExistsError extends AbstractError {
  public final String type;
  public final Map<String, String> values;

  public EntityAlreadyExistsError(String type, Map<String, String> values) {
    super(
        String.format(
            "%s[%s] already exists",
            type,
            values.entrySet().stream()
                .map(e -> String.format("%s = %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", "))));
    this.type = type;
    this.values = values;
  }

  public EntityAlreadyExistsError(String type, String field, String value) {
    this(type, Map.of(field, value));
  }

  public EntityAlreadyExistsError(String type, String id) {
    this(type, "id", id);
  }

  @Override
  public String getTitle() {
    return "EntityAlreadyExistError";
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  @Override
  public Map<String, Object> getProperties() {
    return Map.of(
        "type", this.type,
        "values", this.values);
  }
}
