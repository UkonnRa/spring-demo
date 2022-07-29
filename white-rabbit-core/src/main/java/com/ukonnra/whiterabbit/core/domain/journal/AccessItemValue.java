package com.ukonnra.whiterabbit.core.domain.journal;

import com.ukonnra.whiterabbit.core.domain.group.GroupEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AccessItemValue implements Serializable {
  @Column(columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(nullable = false)
  private Type itemType;

  public static List<AccessItemValue> ofUsers(final Collection<UserEntity> users) {
    return users.stream().map(user -> new AccessItemValue(user.getId(), Type.USER)).toList();
  }

  public static List<AccessItemValue> ofGroups(final Collection<GroupEntity> groups) {
    return groups.stream().map(user -> new AccessItemValue(user.getId(), Type.GROUP)).toList();
  }

  public AccessItemValue(final UserEntity user) {
    this.id = user.getId();
    this.itemType = Type.USER;
  }

  public enum Type {
    USER,
    GROUP
  }
}
