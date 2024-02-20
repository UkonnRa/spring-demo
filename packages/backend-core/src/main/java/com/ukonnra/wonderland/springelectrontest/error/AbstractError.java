package com.ukonnra.wonderland.springelectrontest.error;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public abstract class AbstractError extends RuntimeException {
  protected AbstractError(String message) {
    super(message);
  }

  public abstract String getTitle();

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  public Map<String, Object> getProperties() {
    return Map.of();
  }

  public ProblemDetail getProblemDetail() {
    final var detail = ProblemDetail.forStatusAndDetail(this.getStatus(), this.getMessage());
    detail.setTitle(this.getTitle());
    detail.setProperties(this.getProperties());
    return detail;
  }

  public Map<String, Object> getJson() {
    return Map.of(
        "title", this.getTitle(),
        "status", this.getStatus().value(),
        "message", this.getMessage(),
        "properties", this.getProperties());
  }
}
