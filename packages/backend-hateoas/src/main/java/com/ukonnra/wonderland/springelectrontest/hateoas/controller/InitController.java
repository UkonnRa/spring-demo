package com.ukonnra.wonderland.springelectrontest.hateoas.controller;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.EntryItem;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.repository.EntryItemRepository;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.EntryService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/init", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Init", description = "Init API")
@Slf4j
@Transactional
public class InitController {
  private final JournalService journalService;
  private final AccountService accountService;
  private final EntryService entryService;
  private final EntryItemRepository entryItemRepository;

  public InitController(
      JournalService journalService,
      AccountService accountService,
      EntryService entryService,
      EntryItemRepository entryItemRepository) {
    this.journalService = journalService;
    this.accountService = accountService;
    this.entryService = entryService;
    this.entryItemRepository = entryItemRepository;
  }

  @PostMapping
  public void init() {
    final var prefix = UUID.randomUUID().toString();
    var journals =
        List.of(
            new Journal(
                String.format("Name 1 - %s", prefix), "Desc 1", "Unit 1", Set.of("Tag 1", "Tag 2")),
            new Journal(
                String.format("Name 2 - %s", prefix), "Desc 2", "Unit 2", Set.of("Tag 2", "Tag 4")),
            new Journal(
                String.format("Name 3 - %s", prefix),
                "Desc 3",
                "Unit 3",
                Set.of("Tag 1", "Tag 4")));

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
                  return Stream.of(
                      new Entry(
                          journal,
                          String.format("%s - 1", journal.getName()),
                          String.format("Desc - %s - 1", journal.getName()),
                          Entry.Type.RECORD,
                          LocalDate.of(2023, 1, 1),
                          Set.of("Tag 1", "Tag 2")),
                      new Entry(
                          journal,
                          String.format("%s - 2", journal.getName()),
                          String.format("Desc - %s - 2", journal.getName()),
                          Entry.Type.RECORD,
                          LocalDate.of(2023, 2, 1),
                          Set.of("Tag 2", "Tag 4")));
                })
            .toList();
    entries = this.entryService.getRepository().saveAllAndFlush(entries);

    for (final var entry : entries) {
      log.info("Entry: {}", entry);
    }

    var entryItems =
        entries.stream()
            .flatMap(
                entry -> {
                  var accountItems = accountByJournal.get(entry.getJournal());
                  if (accountItems == null || accountItems.size() < 3) {
                    return Stream.empty();
                  }

                  if (entry.getName().endsWith("1")) {
                    return Stream.of(
                        new EntryItem(entry, accountItems.get(0), BigDecimal.ONE, BigDecimal.TEN),
                        new EntryItem(
                            entry, accountItems.get(1), BigDecimal.TWO, BigDecimal.valueOf(3.5)));
                  } else {
                    return Stream.of(
                        new EntryItem(entry, accountItems.get(1), BigDecimal.TEN, BigDecimal.TWO),
                        new EntryItem(
                            entry, accountItems.get(2), BigDecimal.TWO, BigDecimal.valueOf(7.1)));
                  }
                })
            .collect(Collectors.toSet());

    this.entryItemRepository.saveAllAndFlush(entryItems);
  }
}
