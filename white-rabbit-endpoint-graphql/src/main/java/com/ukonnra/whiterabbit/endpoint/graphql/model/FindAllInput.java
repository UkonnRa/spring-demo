package com.ukonnra.whiterabbit.endpoint.graphql.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.query.Pagination;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

@Slf4j
public record FindAllInput<Q>(
    @Nullable String query, @Nullable List<GraphQlOrder> sort, @Nullable Integer size) {
  public Optional<Q> parsedQuery(final ObjectMapper objectMapper) {
    return Optional.ofNullable(this.query)
        .flatMap(
            q -> {
              try {
                return Optional.ofNullable(objectMapper.readValue(q, new TypeReference<>() {}));
              } catch (JsonProcessingException e) {
                log.error("Error when parsing Query: {}", e.getMessage(), e);
                return Optional.empty();
              }
            });
  }

  public Sort parsedSort() {
    return this.sort == null ? Sort.unsorted() : GraphQlOrder.parseToModel(this.sort);
  }

  public int parsedSize() {
    return Optional.ofNullable(this.size).orElse(Pagination.DEFAULT_SIZE);
  }
}
