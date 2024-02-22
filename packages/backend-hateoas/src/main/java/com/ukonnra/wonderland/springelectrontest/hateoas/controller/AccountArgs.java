package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ukonnra.wonderland.springelectrontest.entity.Account;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Set;
import java.util.UUID;

public interface AccountArgs {
  record Filter(
      @Parameter(name = "filter[id]", description = "Filter Accounts by IDs") @JsonProperty("id")
          Set<UUID> id,
      @Parameter(name = "filter[journal]", description = "Filter Accounts by Journal IDs")
          @JsonProperty("journal")
          Set<UUID> journal,
      @Parameter(
              name = "filter[name]",
              description = "Filter Accounts by names with exactly matching")
          @JsonProperty("name")
          Set<String> name,
      @Parameter(name = "filter[unit]", description = "Filter Accounts by units")
          @JsonProperty("unit")
          Set<String> unit,
      @Parameter(name = "filter[type]", description = "Filter Accounts by types")
          @JsonProperty("type")
          Set<Account.Type> type,
      @Parameter(
              name = "filter[tag]",
              description = "Filter Accounts containing any of the given tags")
          @JsonProperty("tag")
          Set<String> tag,
      @Parameter(
              name = "filter[fullText]",
              description =
                  "Filter Accounts by full-text searching on Field 'name', 'description', 'tags'")
          @JsonProperty("fullText")
          String fullText) {}

  record FindAll(Filter filter, Set<Include> include) {}

  record FindById(Set<Include> include) {}

  enum Include {
    @JsonProperty("journal")
    JOURNAL
  }
}
