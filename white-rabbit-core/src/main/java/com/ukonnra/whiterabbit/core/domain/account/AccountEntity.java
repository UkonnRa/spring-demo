package com.ukonnra.whiterabbit.core.domain.account;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = AccountEntity.TYPE)
@Table(
    name = AccountEntity.PLURAL,
    uniqueConstraints = {@UniqueConstraint(columnNames = {"journal_id", "name"})})
public class AccountEntity extends AbstractEntity<AccountEntity.Dto> {
  public static final String TYPE = "account";
  public static final String PLURAL = "accounts";

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
  AccountType type;

  @Column(nullable = false)
  AccountStrategy strategy;

  @NotBlank
  @Length(max = NAME_MAX_LENGTH)
  @Column(nullable = false, length = NAME_MAX_LENGTH)
  private String unit;

  @Column(nullable = false)
  private boolean archived;

  @Override
  public Dto toDto() {
    return new Dto(
        this.getId(),
        this.getVersion(),
        this.getJournal().getId(),
        this.getName(),
        this.getDescription(),
        this.getType(),
        this.getStrategy(),
        this.getUnit(),
        this.isArchived());
  }

  public record Dto(
      UUID id,
      int version,
      UUID journal,
      String name,
      String description,
      AccountType type,
      AccountStrategy strategy,
      String unit,
      boolean archived)
      implements Serializable {}
}
