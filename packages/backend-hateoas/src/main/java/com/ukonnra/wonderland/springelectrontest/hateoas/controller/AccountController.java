package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.AccountDto;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.AccountModel;
import com.ukonnra.wonderland.springelectrontest.hateoas.model.JournalModel;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

  @GetMapping
  public CollectionModel<AccountModel> findAll(
      @RequestParam(name = "filter[id]", required = false)
          @Parameter(description = "Filter Accounts by IDs")
          Set<UUID> id,
      @RequestParam(name = "filter[journal]", required = false)
          @Parameter(description = "Filter Accounts by Journal IDs")
          Set<UUID> journal,
      @RequestParam(name = "filter[name]", required = false)
          @Parameter(description = "Filter Accounts by names with exactly matching")
          Set<String> name,
      @RequestParam(name = "filter[unit]", required = false)
          @Parameter(description = "Filter Accounts by units")
          Set<String> unit,
      @RequestParam(name = "filter[type]", required = false)
          @Parameter(description = "Filter Accounts by Account type")
          Set<Account.Type> type,
      @RequestParam(name = "filter[tag]", required = false)
          @Parameter(description = "Filter Accounts containing any of the given tags")
          Set<String> tag,
      @RequestParam(name = "filter[fullText]", required = false)
          @Parameter(
              description =
                  "Filter Accounts by full-text searching on Field 'name', 'description', 'tags'")
          String fullText) {

    final var query = new Account.Query();
    query.setId(id);
    query.setJournal(journal);
    query.setName(name);
    query.setUnit(unit);
    query.setType(type);
    query.setTag(tag);
    query.setFullText(fullText);

    final var dtos = this.accountService.convert(this.accountService.findAll(query));
    return CollectionModel.of(dtos.stream().map(this::toEntityModel).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountModel> findById(@PathVariable(name = "id") UUID id) {
    final var query = new Account.Query();
    query.setId(Set.of(id));

    final var dto = this.accountService.convert(this.accountService.findOne(query));
    return ResponseEntity.of(dto.map(this::toEntityModel));
  }

  @GetMapping("/{id}/journal")
  public ResponseEntity<JournalModel> findRelatedJournalById(@PathVariable(name = "id") UUID id) {
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
