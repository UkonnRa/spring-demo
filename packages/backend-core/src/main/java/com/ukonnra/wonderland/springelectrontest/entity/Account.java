package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
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
    name = Account.TYPE,
    indexes = {
      @Index(columnList = "journal_id, name", unique = true),
      @Index(columnList = "unit"),
      @Index(columnList = "type"),
    })
public class Account extends AbstractEntity {
  public static final String TYPE = "accounts";

  @ManyToOne(optional = false)
  private Journal journal;

  @Column(nullable = false, length = MAX_NAMELY)
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  private String name;

  @Column(nullable = false, length = MAX_LONG_TEXT)
  @Size(max = MAX_LONG_TEXT)
  private String description = "";

  @Column(nullable = false, length = MAX_NAMELY)
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  private String unit;

  @Column(nullable = false)
  private Type type;

  @ElementCollection
  @CollectionTable(
      name = "account_tags",
      indexes = @Index(unique = true, columnList = "account_id, tag"))
  @Column(name = "tag", nullable = false, length = MAX_NAMELY)
  private Set<String> tags;

  public Account(
      Journal journal, String name, String description, String unit, Type type, Set<String> tags) {
    this.journal = journal;
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.type = type;
    this.tags = new HashSet<>(tags);
  }

  public enum Type {
    INCOME,
    EXPENSE,
    ASSET,
    LIABILITY,
    EQUITY,
  }
}
