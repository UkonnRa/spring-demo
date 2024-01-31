package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record HierarchyReport(
    String id, UUID journalId, String prefix, String unit, Map<UUID, BigDecimal> values) {

  public record Query(
      Set<String> id, Set<UUID> journal, @Nullable LocalDate start, @Nullable LocalDate end) {
    public Query {
      if (id == null) {
        id = Set.of();
      }

      if (journal == null) {
        journal = Set.of();
      }
    }
  }
}
