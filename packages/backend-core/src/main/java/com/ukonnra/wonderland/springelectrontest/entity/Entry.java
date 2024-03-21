package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;

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
  @ToString
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

  @NoArgsConstructor
  @ToString
  public static class Query implements Specification<Entry> {
    private Set<UUID> id = Set.of();

    private Set<UUID> journal = Set.of();

    private Set<UUID> account = Set.of();

    private Set<String> name = Set.of();

    @Setter @Nullable private Type type;

    @Setter @Nullable private LocalDate start;

    @Setter @Nullable private LocalDate end;

    private Set<String> tag = Set.of();

    private String fullText = "";

    public void setId(@Nullable Collection<UUID> id) {
      if (id == null) {
        this.id = Set.of();
        return;
      }

      this.id = new HashSet<>(id);
    }

    public void setJournal(@Nullable Collection<UUID> journal) {
      if (journal == null) {
        this.journal = Set.of();
        return;
      }

      this.journal = new HashSet<>(journal);
    }

    public void setAccount(@Nullable Collection<UUID> account) {
      if (account == null) {
        this.account = Set.of();
        return;
      }

      this.account = new HashSet<>(account);
    }

    public void setName(@Nullable Collection<String> name) {
      if (name == null) {
        this.name = Set.of();
        return;
      }

      this.name =
          name.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    public void setTag(@Nullable Collection<String> tag) {
      if (tag == null) {
        this.tag = Set.of();
        return;
      }

      this.tag =
          tag.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    public void setFullText(@Nullable String fullText) {
      if (fullText == null) {
        this.fullText = "";
        return;
      }

      this.fullText = fullText.trim().toLowerCase();
    }

    @Override
    public Predicate toPredicate(
        Root<Entry> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
      query.distinct(true);
      final var tagJoin = root.join(Entry_.tags);
      final var itemJoin = root.join(Entry_.items);

      final var predicates = new ArrayList<Predicate>();

      if (!id.isEmpty()) {
        predicates.add(root.get(Entry_.id).in(id));
      }

      if (!journal.isEmpty()) {
        predicates.add(root.get(Entry_.journal).get(Journal_.id).in(journal));
      }

      if (!account.isEmpty()) {
        predicates.add(itemJoin.<Account>get("account").get(Account_.id).in(account));
      }

      if (!name.isEmpty()) {
        predicates.add(root.get(Entry_.name).in(name));
      }

      if (type != null) {
        predicates.add(builder.equal(root.get(Entry_.type), type));
      }

      if (start != null) {
        predicates.add(builder.greaterThanOrEqualTo(root.get(Entry_.date), start));
      }

      if (end != null) {
        predicates.add(builder.lessThanOrEqualTo(root.get(Entry_.date), end));
      }

      if (!tag.isEmpty()) {
        predicates.add(tagJoin.in(tag));
      }

      if (!fullText.isEmpty()) {
        final var fullTextValue = String.format("%%%s%%", fullText);

        predicates.add(
            builder.or(
                builder.like(builder.lower(root.get(Entry_.name)), fullTextValue),
                builder.like(builder.lower(root.get(Entry_.description)), fullTextValue),
                builder.like(builder.lower(tagJoin), fullTextValue)));
      }

      return builder.and(predicates.toArray(new Predicate[] {}));
    }
  }
}
