package com.ukonnra.wonderland.springelectrontest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EntryDto.Record.class, name = "RECORD"),
  @JsonSubTypes.Type(value = EntryDto.Check.class, name = "CHECK")
})
public sealed interface EntryDto {
  UUID id();

  Instant createdDate();

  int version();

  UUID journalId();

  String name();

  String description();

  @JsonProperty
  Entry.Type type();

  LocalDate date();

  Set<String> tags();

  Set<Item> items();

  record Record(
      UUID id,
      Instant createdDate,
      int version,
      UUID journalId,
      String name,
      String description,
      LocalDate date,
      Set<String> tags,
      Set<Item> items,
      EntryState state)
      implements EntryDto {
    public Record(final Entry entry, final EntryState state) {
      this(
          Objects.requireNonNull(entry.getId()),
          entry.getCreatedDate(),
          entry.getVersion(),
          Objects.requireNonNull(entry.getJournal().getId()),
          entry.getName(),
          entry.getDescription(),
          entry.getDate(),
          entry.getTags(),
          Item.of(entry.getItems()),
          state);
    }

    @Override
    @JsonProperty
    public Entry.Type type() {
      return Entry.Type.RECORD;
    }
  }

  record Check(
      UUID id,
      Instant createdDate,
      int version,
      UUID journalId,
      String name,
      String description,
      LocalDate date,
      Set<String> tags,
      Set<Item> items,
      Map<UUID, EntryState> state)
      implements EntryDto {
    public Check(final Entry entry, final Map<UUID, EntryState> state) {
      this(
          Objects.requireNonNull(entry.getId()),
          entry.getCreatedDate(),
          entry.getVersion(),
          Objects.requireNonNull(entry.getJournal().getId()),
          entry.getName(),
          entry.getDescription(),
          entry.getDate(),
          entry.getTags(),
          Item.of(entry.getItems()),
          state);
    }

    @Override
    @JsonProperty
    public Entry.Type type() {
      return Entry.Type.CHECK;
    }
  }

  record Item(UUID accountId, BigDecimal amount, BigDecimal price) {
    public Item(final EntryItem item) {
      this(Objects.requireNonNull(item.getAccount().getId()), item.getAmount(), item.getPrice());
    }

    public static Set<Item> of(final Collection<EntryItem> items) {
      return items.stream().map(Item::new).collect(Collectors.toSet());
    }
  }
}
