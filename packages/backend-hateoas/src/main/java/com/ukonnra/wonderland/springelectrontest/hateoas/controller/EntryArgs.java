package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public interface EntryArgs {
  record Filter(
      @Parameter(name = "filter[id]", description = "Filter Entries by IDs") @JsonProperty("id")
          Set<UUID> id,
      @Parameter(name = "filter[journal]", description = "Filter Entries by Journal IDs")
          @JsonProperty("journal")
          Set<UUID> journal,
      @Parameter(name = "filter[account]", description = "Filter Entries by Account IDs")
          @JsonProperty("account")
          Set<UUID> account,
      @Parameter(
              name = "filter[name]",
              description = "Filter Entries by names with exactly matching")
          @JsonProperty("name")
          Set<String> name,
      @Parameter(name = "filter[type]", description = "Filter Entries by Entry type")
          @JsonProperty("type")
          @Nullable
          Entry.Type type,
      @Parameter(name = "filter[start]", description = "Filter Entries after the given date")
          @JsonProperty("start")
          @Nullable
          LocalDate start,
      @Parameter(name = "filter[end]", description = "Filter Entries before the given date")
          @JsonProperty("end")
          @Nullable
          LocalDate end,
      @Parameter(name = "filter[tag]", description = "Filter Entries by tags") @JsonProperty("tag")
          Set<String> tag,
      @Parameter(
              name = "filter[fullText]",
              description =
                  "Filter Entries by full-text searching on Field 'name', 'description', 'tags'")
          @JsonProperty("fullText")
          String fullText) {}

  record FindAll(Filter filter, Set<Include> include) {}

  record FindById(Set<Include> include) {}

  enum Include {
    @JsonProperty("journal")
    JOURNAL,
    @JsonProperty("accounts")
    ACCOUNTS,
  }
}
