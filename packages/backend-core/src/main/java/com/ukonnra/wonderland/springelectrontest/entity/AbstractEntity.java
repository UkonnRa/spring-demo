package com.ukonnra.wonderland.springelectrontest.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.AbstractPersistable;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class AbstractEntity extends AbstractPersistable<UUID> {
  public static final int MIN_NAMELY = 2;
  public static final int MAX_NAMELY = 127;
  public static final int MAX_LONG_TEXT = 1023;
  public static final int MAX_TAGS = 15;

  @Column(nullable = false)
  @CreatedDate
  private Instant createdDate = Instant.now();

  @Version private int version;

  @Override
  public void setId(@Nullable UUID id) {
    super.setId(id);
  }
}
