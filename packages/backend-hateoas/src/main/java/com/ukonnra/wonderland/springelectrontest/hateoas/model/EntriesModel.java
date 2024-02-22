package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = true)
public class EntriesModel extends RepresentationModel<EntriesModel> {
  private final List<EntryModel> values;

  public EntriesModel(List<EntryModel> values) {
    this.values = values;
  }
}
