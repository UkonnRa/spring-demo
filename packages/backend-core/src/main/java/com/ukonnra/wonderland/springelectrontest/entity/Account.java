package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
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
    name = Account.TYPE,
    indexes = {
      @Index(columnList = "journal_id, name", unique = true),
      @Index(columnList = "unit"),
      @Index(columnList = "type"),
    })
public class Account extends AbstractEntity {
  public static final String TYPE = "accounts";

  @ManyToOne(optional = false)
  @ToString.Exclude
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

  @NoArgsConstructor
  @ToString(callSuper = true)
  public static class Query implements Specification<Account> {
    @Getter private Set<UUID> id = Set.of();

    @Getter private Set<UUID> journal = Set.of();

    private Set<String> name = Set.of();

    private Set<String> unit = Set.of();

    @Getter private Set<Type> type = Set.of();

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
        Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
      query.distinct(true);
      final var tagJoin = root.join(Account_.tags);

      final var predicates = new ArrayList<Predicate>();

      if (!id.isEmpty()) {
        predicates.add(root.get(Account_.id).in(id));
      }

      if (!journal.isEmpty()) {
        predicates.add(root.get(Account_.journal).in(journal));
      }

      if (!name.isEmpty()) {
        predicates.add(root.get(Account_.name).in(name));
      }

      if (!unit.isEmpty()) {
        predicates.add(root.get(Account_.unit).in(unit));
      }

      if (!type.isEmpty()) {
        predicates.add(root.get(Account_.type).in(type));
      }

      if (!tag.isEmpty()) {
        predicates.add(tagJoin.in(tag));
      }

      if (!fullText.isEmpty()) {
        final var fullTextValue = String.format("%%%s%%", fullText);

        predicates.add(
            builder.or(
                builder.like(builder.lower(root.get(Account_.name)), fullTextValue),
                builder.like(builder.lower(root.get(Account_.description)), fullTextValue),
                builder.like(builder.lower(tagJoin), fullTextValue)));
      }

      return builder.and(predicates.toArray(new Predicate[] {}));
    }
  }
}
