package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.HierarchyReport;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountsModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.HierarchyReportModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.HierarchyReportsModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.HierarchyReportService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

  private void loadIncluded(
      final HalModelBuilder builder,
      final Collection<HierarchyReportModel> models,
      final Set<HierarchyReportArgs.Include> include) {
    if (include.contains(HierarchyReportArgs.Include.JOURNAL)) {
      final var journalIds =
          models.stream().map(HierarchyReportModel::getJournalId).collect(Collectors.toSet());
      final var journals =
          this.journalService.convert(this.journalService.findAllByIds(journalIds));
      final var journalModels =
          journals.stream().map(this.journalController::toEntityModel).toList();
      builder.embed(journalModels, JournalModel.class);
    }

    if (include.contains(HierarchyReportArgs.Include.ACCOUNTS)) {
      final var accountIds =
          models.stream()
              .flatMap(model -> model.getValues().keySet().stream())
              .collect(Collectors.toSet());
      final var accounts =
          this.accountService.convert(this.accountService.findAllByIds(accountIds));
      final var accountModels =
          accounts.stream().map(this.accountController::toEntityModel).toList();
      builder.embed(accountModels, AccountModel.class);
    }
  }

  @GetMapping
  public EntityModel<HierarchyReportsModel> findAll(
      @ParameterObject HierarchyReportArgs.FindAll args) {
    final var query =
        new HierarchyReport.Query(
            args.filter().id(),
            args.filter().journal(),
            args.filter().start(),
            args.filter().end());

    final var dtos = this.hierarchyReportService.findAll(query);
    final var models = dtos.stream().map(this::toEntityModel).toList();
    final var builder = HalModelBuilder.halModelOf(new HierarchyReportsModel(models));
    this.loadIncluded(builder, models, args.include());
    return (EntityModel<HierarchyReportsModel>) builder.build();
  }

  @GetMapping("/{id}")
  public @Nullable EntityModel<HierarchyReportModel> findById(
      @PathVariable(name = "id") String id, @ParameterObject HierarchyReportArgs.FindById args) {
    final var query = new HierarchyReport.Query(Set.of(id), null, null, null);

    final var dto = this.hierarchyReportService.findOne(query);
    final var entity = dto.map(this::toEntityModel);

    if (entity.isEmpty() || args.include().isEmpty()) {
      return entity.map(EntityModel::of).orElse(null);
    }

    final var builder = HalModelBuilder.halModelOf(entity);
    this.loadIncluded(builder, entity.stream().toList(), args.include());
    return (EntityModel<HierarchyReportModel>) builder.build();
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
  public ResponseEntity<AccountsModel> findRelatedAccountsById(
      @PathVariable(name = "id") String id) {
    final var query = new HierarchyReport.Query(Set.of(id), null, null, null);

    final var report = this.hierarchyReportService.findOne(query);
    if (report.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final var accountQuery = new Account.Query();
    accountQuery.setId(report.get().values().keySet());
    final var models =
        this.accountService.convert(this.accountService.findAll(accountQuery)).stream()
            .map(this.accountController::toEntityModel)
            .toList();

    return ResponseEntity.ok(
        new AccountsModel(models)
            .add(Link.of(String.format("/hierarchy-reports/%s/accounts", id))));
  }
}
