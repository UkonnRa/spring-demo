package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JournalsModel extends RepresentationModel<JournalsModel> {
  private final List<JournalModel> values;

  public JournalsModel(List<JournalModel> values) {
    this.values = values;
  }
}
