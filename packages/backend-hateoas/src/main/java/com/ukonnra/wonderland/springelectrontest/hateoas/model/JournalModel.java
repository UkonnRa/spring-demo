package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.Relation;

@Getter
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "journals")
public class JournalModel extends AbstractModel<JournalDto, JournalModel> {
  private final UUID id;
  private final Instant createdDate;
  private final int version;
  private final String name;
  private final String description;
  private final String unit;
  private final Set<String> tags;

  public JournalModel(JournalDto dto, @Nullable Link... links) {
    super(dto);
    if (links == null || links.length == 0) {
      this.add(Link.of("/journals/" + dto.id()));
    } else {
      this.add(links);
    }

    this.id = dto.id();
    this.createdDate = dto.createdDate();
    this.version = dto.version();
    this.name = dto.name();
    this.description = dto.description();
    this.unit = dto.unit();
    this.tags = dto.tags();
  }
}
