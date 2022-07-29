package com.ukonnra.whiterabbit.core;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractEntity<D> {
  public static final int TAG_ITEM_MIN_LENGTH = 2;

  public static final int TAG_ITEM_MAX_LENGTH = 32;

  public static final int TAG_MAX_LENGTH = 8;

  public static final int NAME_MIN_LENGTH = 6;
  public static final int NAME_MAX_LENGTH = 64;
  public static final int DESCRIPTION_MAX_LENGTH = 512;

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  protected UUID id;

  @Version @NotNull protected int version;

  public abstract D toDto();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    return o instanceof AbstractEntity<?> that && getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  @Override
  public String toString() {
    return this.toDto().toString();
  }
}
