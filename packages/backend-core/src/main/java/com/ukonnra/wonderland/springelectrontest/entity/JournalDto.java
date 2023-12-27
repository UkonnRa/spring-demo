package com.ukonnra.wonderland.springelectrontest.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record JournalDto(
    UUID id,
    Instant createdDate,
    int version,
    String name,
    String description,
    String unit,
    Set<String> tags)
    implements Serializable {
  public JournalDto(final Journal journal) {
    this(
        Objects.requireNonNull(journal.getId()),
        journal.getCreatedDate(),
        journal.getVersion(),
        journal.getName(),
        journal.getDescription(),
        journal.getUnit(),
        journal.getTags());
  }
}
