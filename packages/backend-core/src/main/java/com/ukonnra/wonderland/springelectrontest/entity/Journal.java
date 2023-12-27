package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.jpa.domain.Specification;

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
  private String name;

  @Column(nullable = false, length = MAX_LONG_TEXT)
  @Size(max = MAX_LONG_TEXT)
  private String description = "";

  @Column(nullable = false, length = MAX_NAMELY)
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  private String unit;

  @ElementCollection
  @CollectionTable(
      name = "journal_tags",
      indexes = @Index(unique = true, columnList = "journal_id, tag"))
  @Column(name = "tag", nullable = false, length = MAX_NAMELY)
  private Set<String> tags;

  public Journal(String name, String description, String unit, Set<String> tags) {
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.tags = new HashSet<>(tags);
  }

  @NoArgsConstructor
  @ToString(callSuper = true)
  public static class Query implements Specification<Journal> {
    @Getter private Set<UUID> id = Set.of();

    private Set<String> name = Set.of();

    private Set<String> unit = Set.of();

    private Set<String> tag = Set.of();

    private String fullText = "";

    public void setName(Collection<String> name) {
      this.name =
          name.stream().map(String::trim).filter(Strings::isNotEmpty).collect(Collectors.toSet());
    }

    public void setUnit(Collection<String> unit) {
      this.unit =
          unit.stream().map(String::trim).filter(Strings::isNotEmpty).collect(Collectors.toSet());
    }

    public void setTag(Collection<String> tag) {
      this.tag =
          tag.stream().map(String::trim).filter(Strings::isNotEmpty).collect(Collectors.toSet());
    }

    public void setFullText(String fullText) {
      this.fullText = fullText.trim().toLowerCase();
    }

    @Override
    public Predicate toPredicate(
        Root<Journal> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
      query.distinct(true);
      final var tagJoin = root.join(Journal_.tags);

      final var predicates = new ArrayList<Predicate>();

      if (!id.isEmpty()) {
        predicates.add(root.get(Journal_.id).in(id));
      }

      if (!name.isEmpty()) {
        predicates.add(root.get(Journal_.name).in(name));
      }

      if (!unit.isEmpty()) {
        predicates.add(root.get(Journal_.unit).in(unit));
      }

      if (!tag.isEmpty()) {
        predicates.add(tagJoin.in(tag));
      }

      if (!fullText.isEmpty()) {
        final var fullTextValue = String.format("%%%s%%", fullText);

        predicates.add(
            builder.or(
                builder.like(builder.lower(root.get(Journal_.name)), fullTextValue),
                builder.like(builder.lower(root.get(Journal_.description)), fullTextValue),
                builder.like(builder.lower(tagJoin), fullTextValue)));
      }

      return builder.and(predicates.toArray(new Predicate[] {}));
    }
  }
}
