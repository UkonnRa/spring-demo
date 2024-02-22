package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.AccountDto;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountsModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
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
@RequestMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Accounts", description = "Account related API")
@Slf4j
@Transactional
public class AccountController {
  private final JournalService journalService;
  private final JournalController journalController;
  private final AccountService accountService;

  public AccountController(
      JournalService journalService,
      JournalController journalController,
      AccountService accountService) {
    this.journalService = journalService;
    this.journalController = journalController;
    this.accountService = accountService;
  }

  AccountModel toEntityModel(AccountDto dto, Link... links) {
    return new AccountModel(dto, links);
  }

  private void loadIncluded(
      final HalModelBuilder builder,
      final Collection<AccountModel> models,
      final Set<AccountArgs.Include> include) {
    if (include.contains(AccountArgs.Include.JOURNAL)) {
      final var journalIds =
          models.stream().map(AccountModel::getJournalId).collect(Collectors.toSet());
      final var journals =
          this.journalService.convert(this.journalService.findAllByIds(journalIds));
      final var journalModels =
          journals.stream().map(this.journalController::toEntityModel).toList();
      builder.embed(journalModels, JournalModel.class);
    }
  }

  @GetMapping
  public EntityModel<AccountsModel> findAll(@ParameterObject AccountArgs.FindAll args) {
    final var query = new Account.Query();
    query.setId(args.filter().id());
    query.setJournal(args.filter().journal());
    query.setName(args.filter().name());
    query.setUnit(args.filter().unit());
    query.setType(args.filter().type());
    query.setTag(args.filter().tag());
    query.setFullText(args.filter().fullText());

    final var dtos = this.accountService.convert(this.accountService.findAll(query));
    final var models = dtos.stream().map(this::toEntityModel).toList();
    final var builder = HalModelBuilder.halModelOf(new AccountsModel(models));
    this.loadIncluded(builder, models, args.include());
    return (EntityModel<AccountsModel>) builder.build();
  }

  @GetMapping("/{id}")
  public @Nullable EntityModel<AccountModel> findById(
      @PathVariable(name = "id") UUID id, @ParameterObject AccountArgs.FindById args) {
    final var query = new Account.Query();
    query.setId(Set.of(id));

    final var dto = this.accountService.convert(this.accountService.findOne(query));
    final var entity = dto.map(this::toEntityModel);

    if (entity.isEmpty() || args.include().isEmpty()) {
      return entity.map(EntityModel::of).orElse(null);
    }

    final var builder = HalModelBuilder.halModelOf(entity);
    this.loadIncluded(builder, entity.stream().toList(), args.include());
    return (EntityModel<AccountModel>) builder.build();
  }

  @GetMapping("/{id}/journal")
  public ResponseEntity<JournalModel> findRelatedJournal(@PathVariable(name = "id") UUID id) {
    final var query = new Account.Query();
    query.setId(Set.of(id));

    return ResponseEntity.of(
        this.accountService
            .findOne(query)
            .flatMap(account -> this.journalService.convert(account.getJournal()))
            .map(
                dto ->
                    this.journalController.toEntityModel(
                        dto, Link.of(String.format("/accounts/%s/journal", id)))));
  }
}
