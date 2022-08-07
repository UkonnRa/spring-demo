package com.ukonnra.whiterabbit.core.domain.record;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.CoreError;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Transient;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = RecordEntity.TYPE)
@Table(
    name = RecordEntity.PLURAL,
    uniqueConstraints = {@UniqueConstraint(columnNames = {"journal_id", "name"})})
public class RecordEntity extends AbstractEntity<RecordEntity.Dto> {
  public static final int ITEM_MIN_LENGTH = 2;
  public static final int ITEM_MAX_LENGTH = 16;

  public static final String TYPE = "record";
  public static final String PLURAL = "records";

  @ManyToOne(optional = false)
  private JournalEntity journal;

  @NotBlank
  @Length(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH)
  @Column(nullable = false, length = NAME_MAX_LENGTH)
  private String name;

  @Lob
  @Length(max = DESCRIPTION_MAX_LENGTH)
  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private RecordType type;

  @Column(nullable = false)
  private LocalDate date;

  @ElementCollection
  @Size(max = TAG_MAX_LENGTH)
  @CollectionTable(
      name = "record_tags",
      uniqueConstraints = {@UniqueConstraint(columnNames = {"record_id", "tag"})})
  @Column(name = "tag")
  private Set<@Length(min = TAG_ITEM_MIN_LENGTH, max = TAG_ITEM_MAX_LENGTH) String> tags;

  @ElementCollection
  @Size(min = ITEM_MIN_LENGTH, max = ITEM_MAX_LENGTH)
  @CollectionTable(
      name = "record_items",
      uniqueConstraints = {@UniqueConstraint(columnNames = {"record_id", "account_id"})})
  private Set<RecordItemValue> items;

  public void setTags(final Collection<String> tags) {
    this.tags = new HashSet<>(tags);
  }

  public Set<CoreError> validate() {
    final var notMatchAccountIds = new HashSet<UUID>();
    final var emptyAccountIds = new HashSet<UUID>();
    for (final var item : items) {
      if (!item.getAccount().getUnit().equals(journal.getUnit())
          && item.getAmount() != null
          && item.getPrice() == null) {
        notMatchAccountIds.add(item.getAccount().getId());
      }
      if (item.getAmount() == null) {
        emptyAccountIds.add(item.getAccount().getId());
      }
    }

    final var errors =
        new HashSet<CoreError>(
            notMatchAccountIds.stream()
                .map(id -> new CoreError.RecordUnitNotMatch(this, id))
                .toList());
    if (emptyAccountIds.size() > 1) {
      errors.add(new CoreError.RecordWithMultipleEmptyItems(this, emptyAccountIds));
    }

    return errors;
  }

  @Transient
  public boolean isValid() {
    var assets = BigDecimal.ZERO;
    var liabilities = BigDecimal.ZERO;
    var emptyItems = 0;
    for (final var item : items) {
      if (item.getAmount() == null) {
        emptyItems += 1;
        continue;
      }

      final var value =
          Optional.ofNullable(item.getPrice())
              .map(price -> price.multiply(item.getAmount()))
              .orElse(item.getAmount());
      // ASSET = LIABILITY + EQUALITY + INCOME - EXPENSE
      switch (item.getAccount().getType()) {
        case ASSET, EXPENSE -> assets = assets.add(value);
        default -> liabilities = liabilities.add(value);
      }
    }
    return emptyItems == 1 || (emptyItems == 0 && assets.compareTo(liabilities) == 0);
  }

  @Override
  public Dto toDto() {
    return new Dto(
        this.getId(),
        this.getVersion(),
        this.getJournal().getId(),
        this.getName(),
        this.getDescription(),
        this.getType(),
        this.getDate(),
        this.getTags(),
        RecordItemValue.Dto.of(this.getItems()),
        this.isValid());
  }

  public record Dto(
      UUID id,
      int version,
      UUID journal,
      String name,
      String description,
      RecordType type,
      LocalDate date,
      Set<String> tags,
      Set<RecordItemValue.Dto> items,
      boolean isValid)
      implements Serializable {}
}
