package com.ukonnra.whiterabbit.core.domain.user;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
@Entity(name = UserEntity.TYPE)
@Table(name = UserEntity.PLURAL)
public final class UserEntity extends AbstractEntity<UserEntity.Dto> {
  private static final int AUTH_ID_MIN_LENGTH = 1;
  private static final int AUTH_ID_MAX_LENGTH = 16;

  public static final String TYPE = "user";
  public static final String PLURAL = "users";

  @NotBlank
  @Length(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH)
  @Column(nullable = false, unique = true, length = NAME_MAX_LENGTH)
  private String name;

  @Enumerated
  @Column(nullable = false)
  @Builder.Default
  private RoleValue role = RoleValue.USER;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = false)
  @Builder.Default
  @Size(min = AUTH_ID_MIN_LENGTH, max = AUTH_ID_MAX_LENGTH)
  private Set<AuthIdValue> authIds = new HashSet<>();

  public record Dto(UUID id, int version, String name, RoleValue role, Set<AuthIdValue> authIds)
      implements Serializable {}

  @Override
  public Dto toDto() {
    return new Dto(
        this.getId(), this.getVersion(), this.getName(), this.getRole(), this.getAuthIds());
  }
}
