package com.ukonnra.whiterabbit.core.domain.group;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
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
@Entity(name = GroupEntity.TYPE)
@Table(name = GroupEntity.PLURAL)
public class GroupEntity extends AbstractEntity<GroupEntity.Dto> {
  public static final int ITEM_MAX_LENGTH = 8;

  public static final String TYPE = "group";
  public static final String PLURAL = "groups";

  @NotBlank
  @Length(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH)
  @Column(nullable = false, unique = true, length = NAME_MAX_LENGTH)
  private String name;

  @Lob
  @Length(max = DESCRIPTION_MAX_LENGTH)
  @Column(nullable = false)
  private String description;

  @ManyToMany
  @Size(max = ITEM_MAX_LENGTH)
  private Set<UserEntity> admins;

  @ManyToMany
  @Size(max = ITEM_MAX_LENGTH)
  private Set<UserEntity> members;

  public void setAdmins(Set<UserEntity> admins) {
    this.admins = admins;
    this.members.removeAll(admins);
  }

  public void setMembers(Set<UserEntity> members) {
    this.members = members;
    this.admins.removeAll(members);
  }

  public boolean isContainingUser(final UUID userId) {
    return this.admins.stream().anyMatch(user -> user.getId().equals(userId))
        || this.members.stream().anyMatch(user -> user.getId().equals(userId));
  }

  @Override
  public Dto toDto() {
    return new Dto(
        this.getId(),
        this.getVersion(),
        this.getName(),
        this.getDescription(),
        this.getAdmins().stream().map(AbstractEntity::getId).collect(Collectors.toSet()),
        this.getMembers().stream().map(AbstractEntity::getId).collect(Collectors.toSet()));
  }

  public record Dto(
      UUID id, int version, String name, String description, Set<UUID> admins, Set<UUID> members)
      implements Serializable {}
}
