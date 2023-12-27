package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
@Entity
@Table(
    name = Entry.TYPE,
    indexes = {
      @Index(columnList = "journal_id, name", unique = true),
      @Index(columnList = "journal_id, date"),
      @Index(columnList = "type"),
    })
public class Entry extends AbstractEntity {
  public static final String TYPE = "entries";

  @ManyToOne(optional = false)
  @ToString.Exclude
  private Journal journal;

  @Column(nullable = false, length = MAX_NAMELY)
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  private String name;

  @Column(nullable = false, length = MAX_LONG_TEXT)
  @Size(max = MAX_LONG_TEXT)
  private String description = "";

  @Column(nullable = false)
  private Type type;

  @Column(nullable = false)
  private LocalDate date;

  @ElementCollection
  @CollectionTable(
      name = "entry_tags",
      indexes = @Index(unique = true, columnList = "entry_id, tag"))
  @Column(name = "tag", nullable = false, length = MAX_NAMELY)
  private Set<String> tags;

  @ElementCollection
  @CollectionTable(
      name = "entry_items",
      indexes = @Index(unique = true, columnList = "entry_id, account_id"))
  private Set<Item> items;

  public Entry(
      Journal journal,
      String name,
      String description,
      Type type,
      LocalDate date,
      Set<String> tags,
      Set<Item> items) {
    this.journal = journal;
    this.name = name;
    this.description = description;
    this.type = type;
    this.date = date;
    this.tags = new HashSet<>(tags);
    this.items = new HashSet<>(items);
  }

  public enum Type {
    RECORD,
    CHECK,
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @ToString(callSuper = true)
  @Embeddable
  public static class Item {
    @ManyToOne(optional = false)
    @ToString.Exclude
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal price;
  }
}
