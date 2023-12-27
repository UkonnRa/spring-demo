package com.ukonnra.wonderland.springelectrontest.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record AccountDto(
    UUID id,
    Instant createdDate,
    int version,
    UUID journalId,
    String name,
    String description,
    String unit,
    Account.Type type,
    Set<String> tags)
    implements Serializable {
  public AccountDto(final Account account) {
    this(
        Objects.requireNonNull(account.getId()),
        account.getCreatedDate(),
        account.getVersion(),
        Objects.requireNonNull(account.getJournal().getId()),
        account.getName(),
        account.getDescription(),
        account.getUnit(),
        account.getType(),
        account.getTags());
  }
}
