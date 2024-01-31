package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.HierarchyReport;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.HierarchyReportModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.HierarchyReportService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/hierarchy-reports", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "HierarchyReports", description = "Hierarchy Reports related API")
@Slf4j
@Transactional
public class HierarchyReportController {
  private final JournalService journalService;
  private final JournalController journalController;
  private final AccountService accountService;
  private final AccountController accountController;
  private final HierarchyReportService hierarchyReportService;

  public HierarchyReportController(
      JournalService journalService,
      JournalController journalController,
      AccountService accountService,
      AccountController accountController,
      HierarchyReportService hierarchyReportService) {
    this.journalService = journalService;
    this.journalController = journalController;
    this.accountService = accountService;
    this.accountController = accountController;
    this.hierarchyReportService = hierarchyReportService;
  }

  HierarchyReportModel toEntityModel(HierarchyReport dto) {
    return new HierarchyReportModel(dto);
  }

  @GetMapping
  public CollectionModel<HierarchyReportModel> findAll(
      @RequestParam(name = "filter[id]", required = false)
          @Parameter(description = "Filter Hierarchy Reports by IDs")
          Set<String> id,
      @RequestParam(name = "filter[journal]", required = false)
          @Parameter(description = "Filter Hierarchy Reports by Journal IDs")
          Set<UUID> journal,
      @RequestParam(name = "filter[start]", required = false)
          @Parameter(description = "Filter Hierarchy Reports after the date, inclusive")
          @Nullable
          LocalDate start,
      @RequestParam(name = "filter[end]", required = false)
          @Parameter(description = "Filter Hierarchy Reports before the date, inclusive")
          @Nullable
          LocalDate end) {

    final var query = new HierarchyReport.Query(id, journal, start, end);

    final var dtos = this.hierarchyReportService.findAll(query);
    return CollectionModel.of(dtos.stream().map(this::toEntityModel).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<HierarchyReportModel> findById(@PathVariable(name = "id") String id) {
    final var query = new HierarchyReport.Query(Set.of(id), null, null, null);

    final var dto = this.hierarchyReportService.findOne(query);
    return ResponseEntity.of(dto.map(this::toEntityModel));
  }

  @GetMapping("/{id}/journal")
  public ResponseEntity<JournalModel> findRelatedJournalById(@PathVariable(name = "id") String id) {
    final var query = new HierarchyReport.Query(Set.of(id), null, null, null);

    return ResponseEntity.of(
        this.hierarchyReportService
            .findOne(query)
            .flatMap(
                report -> {
                  final var journalQuery = new Journal.Query();
                  journalQuery.setId(Set.of(report.journalId()));
                  return this.journalService.findOne(journalQuery);
                })
            .flatMap(this.journalService::convert)
            .map(
                dto ->
                    this.journalController.toEntityModel(
                        dto, Link.of(String.format("/hierarchy-reports/%s/journal", id)))));
  }

  @GetMapping("/{id}/accounts")
  public CollectionModel<AccountModel> findRelatedAccountsById(
      @PathVariable(name = "id") String id) {
    final var query = new HierarchyReport.Query(Set.of(id), null, null, null);

    return CollectionModel.of(
            this.hierarchyReportService.findOne(query).stream()
                .flatMap(
                    report -> {
                      final var accountQuery = new Account.Query();
                      accountQuery.setId(report.values().keySet());
                      return this.accountService
                          .convert(this.accountService.findAll(accountQuery))
                          .stream();
                    })
                .map(this.accountController::toEntityModel)
                .toList())
        .add(Link.of(String.format("/hierarchy-reports/%s/accounts", id)));
  }
}
