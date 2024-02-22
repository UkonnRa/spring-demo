package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Set;
import java.util.UUID;

public interface JournalArgs {
  record Filter(
      @Parameter(name = "filter[id]", description = "Filter Journals by IDs") @JsonProperty("id")
          Set<UUID> id,
      @Parameter(
              name = "filter[name]",
              description = "Filter Journals by names with exactly matching")
          @JsonProperty("name")
          Set<String> name,
      @Parameter(name = "filter[unit]", description = "Filter Journals by units")
          @JsonProperty("unit")
          Set<String> unit,
      @Parameter(
              name = "filter[tag]",
              description = "Filter Journals containing any of the given tags")
          @JsonProperty("tag")
          Set<String> tag,
      @Parameter(
              name = "filter[fullText]",
              description =
                  "Filter Journals by full-text searching on Field 'name', 'description', 'tags'")
          @JsonProperty("fullText")
          String fullText) {}

  record FindAll(Filter filter) {}
}
