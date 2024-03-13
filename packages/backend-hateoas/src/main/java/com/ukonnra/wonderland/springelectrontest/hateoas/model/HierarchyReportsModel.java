package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@EqualsAndHashCode(callSuper = true)
public class HierarchyReportsModel extends RepresentationModel<HierarchyReportsModel> {
  private final List<HierarchyReportModel> values;

  public HierarchyReportsModel(List<HierarchyReportModel> values) {
    this.values = values;
  }
}
