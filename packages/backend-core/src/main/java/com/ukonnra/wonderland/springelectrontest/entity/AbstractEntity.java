package com.ukonnra.wonderland.springelectrontest.entity;

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
  protected static final int MIN_NAMELY = 2;
  protected static final int MAX_NAMELY = 127;
  protected static final int MAX_LONG_TEXT = 1023;

  @CreatedDate private Instant createdDate = Instant.now();

  @Version private int version;
}
