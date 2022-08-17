package com.ukonnra.whiterabbit.core.domain.journal;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
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

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = JournalEntity.TYPE)
@Table(name = JournalEntity.PLURAL)
public class JournalEntity extends AbstractEntity<JournalEntity.Dto> {
  public static final int ACCESS_MAX_LENGTH = 16;

  public static final String TYPE = "journal";
  public static final String PLURAL = "journals";

  @NotBlank
  @Length(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH)
  @Column(nullable = false, unique = true, length = NAME_MAX_LENGTH)
  private String name;

  @Lob
  @Length(max = DESCRIPTION_MAX_LENGTH)
  @Column(nullable = false)
  private String description;

  @ElementCollection(fetch = FetchType.EAGER)
  @Size(max = TAG_MAX_LENGTH)
  @CollectionTable(
      name = "journal_tags",
      uniqueConstraints = {@UniqueConstraint(columnNames = {"journal_id", "tag"})})
  @Column(name = "tag")
  @Builder.Default
  private Set<@Length(min = TAG_ITEM_MIN_LENGTH, max = TAG_ITEM_MAX_LENGTH) String> tags =
      new HashSet<>();

  @NotBlank
  @Length(max = NAME_MAX_LENGTH)
  @Column(nullable = false, length = NAME_MAX_LENGTH)
  private String unit;

  @Column(nullable = false)
  private boolean archived;

  @ElementCollection(fetch = FetchType.EAGER)
  @Size(max = ACCESS_MAX_LENGTH)
  @CollectionTable(
      name = "journal_admins",
      uniqueConstraints = @UniqueConstraint(columnNames = {"journal_id", "id"}))
  @Builder.Default
  private Set<AccessItemValue> admins = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @Size(max = ACCESS_MAX_LENGTH)
  @CollectionTable(
      name = "journal_members",
      uniqueConstraints = @UniqueConstraint(columnNames = {"journal_id", "id"}))
  @Builder.Default
  private Set<AccessItemValue> members = new HashSet<>();

  public void setTags(final Collection<String> tags) {
    this.tags = new HashSet<>(tags);
  }

  public void setAdmins(final Collection<AccessItemValue> admins) {
    this.admins = new HashSet<>(admins);
    this.getMembers().removeAll(admins);
  }

  public void setMembers(final Collection<AccessItemValue> members) {
    this.members = new HashSet<>(members);
    this.getAdmins().removeAll(members);
  }

  @Override
  public Dto toDto() {
    return new Dto(
        this.getId(),
        this.getVersion(),
        this.getName(),
        this.getDescription(),
        this.getTags(),
        this.getUnit(),
        this.isArchived(),
        this.getAdmins(),
        this.getMembers());
  }

  public record Dto(
      UUID id,
      int version,
      String name,
      String description,
      Set<String> tags,
      String unit,
      boolean archived,
      Set<AccessItemValue> admins,
      Set<AccessItemValue> members)
      implements Serializable {}
}
