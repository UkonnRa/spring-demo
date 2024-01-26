package com.ukonnra.wonderland.springelectrontest.hateoas;

import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MAX_NAMELY;
import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MAX_TAGS;
import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MIN_NAMELY;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JournalModel extends AbstractModel<Journal, JournalModel> {
  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  private final String name;

  @Size(max = AbstractEntity.MAX_LONG_TEXT)
  private final String description;

  @Size(min = MIN_NAMELY, max = MAX_NAMELY)
  private final String unit;

  @Size(max = MAX_TAGS)
  private final Set<@Size(min = MIN_NAMELY, max = MAX_NAMELY) String> tags;

  protected JournalModel(Journal entity) {
    super(entity, Journal.TYPE);
    this.name = entity.getName();
    this.description = entity.getDescription();
    this.unit = entity.getUnit();
    this.tags = entity.getTags();
  }
}
