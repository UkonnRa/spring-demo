package com.ukonnra.wonderland.springelectrontest.error;

import jakarta.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;

public final class Errors extends ResponseStatusException {
  private final Collection<? extends ResponseStatusException> errors;

  private static <T extends ResponseStatusException> Stream<ResponseStatusException> flat(T error) {
    if (error instanceof Errors errors) {
      return errors.errors.stream().flatMap(Errors::flat);
    }
    return Stream.of(error);
  }

  public static @Nullable ResponseStatusException of(
      Collection<? extends ResponseStatusException> errors) {
    final var flatten = errors.stream().flatMap(Errors::flat).toList();
    if (flatten.isEmpty()) {
      return null;
    } else if (flatten.size() == 1) {
      return flatten.getFirst();
    } else {
      return new Errors(flatten);
    }
  }

  protected Errors(Collection<? extends ResponseStatusException> errors) {
    super(HttpStatus.BAD_REQUEST, "Multiple errors found");
    this.setTitle("Errors Found");

    this.errors = errors;
    this.setType(URI.create("urn:wonderland:white-rabbit:errors:errors"));
    this.getBody()
        .setProperty(
            "errors",
            errors.stream().map(ErrorResponseException::getBody).collect(Collectors.toSet()));
  }
}
