package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public interface HierarchyReportArgs {
  record Filter(
      @Parameter(name = "filter[id]", description = "Filter Hierarchy Reports by IDs")
          @JsonProperty("id")
          Set<String> id,
      @Parameter(name = "filter[journal]", description = "Filter Hierarchy Reports by Journal IDs")
          @JsonProperty("journal")
          Set<UUID> journal,
      @Parameter(
              name = "filter[start]",
              description = "Filter Hierarchy Reports after the date, inclusive")
          @JsonProperty("start")
          @Nullable
          LocalDate start,
      @Parameter(
              name = "filter[end]",
              description = "Filter Hierarchy Reports before the date, inclusive")
          @JsonProperty("end")
          @Nullable
          LocalDate end) {}

  record FindAll(Filter filter, Set<Include> include) {}

  record FindById(Set<Include> include) {}

  enum Include {
    @JsonProperty("journal")
    JOURNAL,
    @JsonProperty("accounts")
    ACCOUNTS
  }
}
