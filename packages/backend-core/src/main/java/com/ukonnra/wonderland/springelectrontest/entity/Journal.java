package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    name = Journal.TYPE,
    indexes = {@Index(columnList = "unit")})
public class Journal extends AbstractEntity {
  public static final String TYPE = "journals";

  @Column(unique = true, nullable = false, length = MAX_NAMELY)
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  @NotNull
  private String name;

  @Column(nullable = false, length = MAX_LONG_TEXT)
  @Size(max = MAX_LONG_TEXT)
  @NotNull
  private String description = "";

  @Column(nullable = false, length = MAX_NAMELY)
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  @NotNull
  private String unit;

  @ElementCollection
  @Column(name = "tag", nullable = false, length = MAX_NAMELY)
  @CollectionTable(indexes = @Index(unique = true, columnList = "journal_id, tag"))
  private Set<String> tags;

  public Journal(String name, String description, String unit, Set<String> tags) {
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.tags = new HashSet<>(tags);
  }
}
