package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractModel<T extends AbstractEntity, M extends AbstractModel<T, M>>
    extends RepresentationModel<M> {
  private final UUID id;

  private final Instant createdDate;

  private final int version;

  protected AbstractModel(final T entity, final String type) {
    this.id = Optional.ofNullable(entity.getId()).orElseGet(UUID::randomUUID);
    this.createdDate = entity.getCreatedDate();
    this.version = entity.getVersion();
    this.add(Link.of(String.format("/%s/%s", type, this.id)));
  }
}
