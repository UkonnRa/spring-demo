package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.EntryItem;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountsModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.EntriesModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.EntryModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.EntryService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
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
@RequestMapping(value = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Entries", description = "Entry related API")
@Slf4j
@Transactional
public class EntryController {
  private final JournalService journalService;
  private final JournalController journalController;
  private final AccountService accountService;
  private final AccountController accountController;
  private final EntryService entryService;

  public EntryController(
      JournalService journalService,
      JournalController journalController,
      AccountService accountService,
      AccountController accountController,
      EntryService entryService) {
    this.journalService = journalService;
    this.journalController = journalController;
    this.accountService = accountService;
    this.accountController = accountController;
    this.entryService = entryService;
  }

  @Nullable
  EntryModel toEntityModel(EntryDto dto, Link... links) {
    if (dto instanceof EntryDto.Record item) {
      return new EntryModel.Record(item, links);
    } else if (dto instanceof EntryDto.Check check) {
      return new EntryModel.Check(check, links);
    }
    return null;
  }

  private void loadIncluded(
      final HalModelBuilder builder,
      final Collection<EntryModel> models,
      final Set<EntryArgs.Include> include) {
    if (include.contains(EntryArgs.Include.JOURNAL)) {
      final var journalIds =
          models.stream().map(EntryModel::getJournalId).collect(Collectors.toSet());
      final var journals =
          this.journalService.convert(this.journalService.findAllByIds(journalIds));
      final var journalModels =
          journals.stream().map(this.journalController::toEntityModel).toList();
      builder.embed(journalModels, JournalModel.class);
    }

    if (include.contains(EntryArgs.Include.ACCOUNTS)) {
      final var accountIds =
          models.stream()
              .flatMap(model -> model.getAccountIds().stream())
              .collect(Collectors.toSet());
      final var accounts =
          this.accountService.convert(this.accountService.findAllByIds(accountIds));
      final var accountModels =
          accounts.stream().map(this.accountController::toEntityModel).toList();
      builder.embed(accountModels, AccountModel.class);
    }
  }

  @GetMapping
  public EntityModel<EntriesModel> findAll(@ParameterObject EntryArgs.FindAll args) {
    final var query = new Entry.Query();
    query.setId(args.filter().id());
    query.setJournal(args.filter().journal());
    query.setAccount(args.filter().account());
    query.setName(args.filter().name());
    query.setType(args.filter().type());
    query.setStart(args.filter().start());
    query.setEnd(args.filter().end());
    query.setTag(args.filter().tag());
    query.setFullText(args.filter().fullText());

    final var dtos = this.entryService.convert(this.entryService.findAll(query));
    final var models = dtos.stream().map(this::toEntityModel).toList();
    final var builder = HalModelBuilder.halModelOf(new EntriesModel(models));
    this.loadIncluded(builder, models, args.include());
    return (EntityModel<EntriesModel>) builder.build();
  }

  @GetMapping("/{id}")
  public @Nullable EntityModel<EntryModel> findById(
      @PathVariable(name = "id") UUID id, @ParameterObject EntryArgs.FindById args) {
    final var query = new Entry.Query();
    query.setId(Set.of(id));

    final var dto = this.entryService.convert(this.entryService.findOne(query));
    final var entity = dto.map(this::toEntityModel);

    if (entity.isEmpty() || args.include().isEmpty()) {
      return entity.map(EntityModel::of).orElse(null);
    }

    final var builder = HalModelBuilder.halModelOf(entity);
    this.loadIncluded(builder, entity.stream().toList(), args.include());
    return (EntityModel<EntryModel>) builder.build();
  }

  @GetMapping("/{id}/accounts")
  public ResponseEntity<AccountsModel> findRelatedAccounts(@PathVariable(name = "id") UUID id) {
    final var query = new Entry.Query();
    query.setId(Set.of(id));

    final var entry = this.entryService.findOne(query);
    if (entry.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    final var accounts = entry.get().getItems().stream().map(EntryItem::getAccount).toList();
    final var dtos = this.accountService.convert(accounts);

    final var models = dtos.stream().map(this.accountController::toEntityModel).toList();
    return ResponseEntity.ok(
        new AccountsModel(models).add(Link.of(String.format("/entries/%s/accounts", id))));
  }

  @GetMapping("/{id}/journal")
  public ResponseEntity<JournalModel> findRelatedJournal(@PathVariable(name = "id") UUID id) {
    final var query = new Entry.Query();
    query.setId(Set.of(id));

    final var dto =
        this.entryService
            .findOne(query)
            .flatMap(account -> this.journalService.convert(account.getJournal()));

    return ResponseEntity.of(
        dto.map(
            e ->
                this.journalController.toEntityModel(
                    e, Link.of(String.format("/entries/%s/journal", id)))));
  }
}
