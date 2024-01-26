package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.EntryService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/journals",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Journals", description = "Journal related API")
@Slf4j
public class JournalController {
  private final JournalService journalService;
  private final AccountService accountService;
  private final EntryService entryService;

  public JournalController(
      JournalService journalService, AccountService accountService, EntryService entryService) {
    this.journalService = journalService;
    this.accountService = accountService;
    this.entryService = entryService;
  }

  @GetMapping
  public CollectionModel<JournalModel> findAll() {
    final var entities = this.journalService.findAll(new Journal.Query());
    return CollectionModel.of(entities.stream().map(JournalModel::new).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<JournalModel>> findById(@PathVariable(name = "id") UUID id) {
    final var query = new Journal.Query();
    query.setId(Set.of(id));
    return ResponseEntity.of(
        this.journalService.findOne(query).map(JournalModel::new).map(EntityModel::of));
  }

  @PostMapping("/init")
  public void init() {
    var journals =
        List.of(
            new Journal("Name 1", "Desc 1", "Unit 1", Set.of("Tag 1", "Tag 2")),
            new Journal("Name 2", "Desc 2", "Unit 2", Set.of("Tag 2", "Tag 4")),
            new Journal("Name 3", "Desc 3", "Unit 3", Set.of("Tag 1", "Tag 4")));

    journals = this.journalService.getRepository().saveAllAndFlush(journals);

    final var journalQuery = new Journal.Query();
    journalQuery.setTag(Set.of("Tag 4", "Tag 2"));
    journalQuery.setFullText(" 2");
    for (final var journal : this.journalService.getRepository().findAll(journalQuery)) {
      log.info("Journal: {}", journal.getName());
    }

    var accounts =
        journals.stream()
            .flatMap(
                journal ->
                    Arrays.stream(Account.Type.values())
                        .map(
                            type -> {
                              final var name = String.format("%s - %s", journal.getName(), type);
                              return new Account(
                                  journal,
                                  name,
                                  String.format("Desc - %s", name),
                                  "Unit 1",
                                  type,
                                  Set.of("Tag 1", "Tag 2", type.name()));
                            }))
            .toList();
    accounts = this.accountService.getRepository().saveAllAndFlush(accounts);

    final var accountQuery = new Account.Query();
    accountQuery.setTag(Set.of("Tag 4", "Tag 2"));
    accountQuery.setUnit(Set.of("Unit 1"));
    accountQuery.setFullText(Account.Type.ASSET.name());

    for (final var account : this.accountService.getRepository().findAll(accountQuery)) {
      log.info("Account: {}", account);
    }

    final var accountByJournal =
        accounts.stream().collect(Collectors.groupingBy(Account::getJournal));
    var entries =
        accountByJournal.entrySet().stream()
            .flatMap(
                pair -> {
                  final var journal = pair.getKey();
                  final var list = new ArrayList<>(pair.getValue());

                  return Stream.of(
                      new Entry(
                          journal,
                          String.format("%s - 1", journal.getName()),
                          String.format("Desc - %s - 1", journal.getName()),
                          Entry.Type.RECORD,
                          LocalDate.of(2023, 1, 1),
                          Set.of("Tag 1", "Tag 2"),
                          Set.of(
                              new Entry.Item(list.get(0), BigDecimal.ONE, BigDecimal.TEN),
                              new Entry.Item(
                                  list.get(1), BigDecimal.TWO, BigDecimal.valueOf(3.5)))),
                      new Entry(
                          journal,
                          String.format("%s - 2", journal.getName()),
                          String.format("Desc - %s - 2", journal.getName()),
                          Entry.Type.RECORD,
                          LocalDate.of(2023, 2, 1),
                          Set.of("Tag 2", "Tag 4"),
                          Set.of(
                              new Entry.Item(list.get(1), BigDecimal.TEN, BigDecimal.TWO),
                              new Entry.Item(
                                  list.get(2), BigDecimal.ONE, BigDecimal.valueOf(7.1)))));
                })
            .toList();
    entries = this.entryService.getRepository().saveAllAndFlush(entries);
    for (final var entry : entries) {
      log.info("Entry: {}", entry);
    }
  }
}
