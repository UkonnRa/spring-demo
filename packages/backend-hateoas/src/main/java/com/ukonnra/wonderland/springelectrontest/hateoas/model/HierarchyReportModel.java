package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import com.ukonnra.wonderland.springelectrontest.entity.HierarchyReport;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.Relation;

@Getter
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "hierarchy-reports")
public class HierarchyReportModel extends AbstractModel<HierarchyReport, HierarchyReportModel> {
  private final String id;
  private final UUID journalId;
  private final String prefix;
  private final String unit;
  private final Map<UUID, BigDecimal> values;

  public HierarchyReportModel(HierarchyReport dto, @Nullable Link... links) {
    super(dto);
    if (links == null || links.length == 0) {
      this.add(Link.of("/hierarchy-reports/" + dto.id()));
    } else {
      this.add(links);
    }

    this.id = dto.id();
    this.journalId = dto.journalId();
    this.prefix = dto.prefix();
    this.unit = dto.unit();
    this.values = dto.values();
  }
}
