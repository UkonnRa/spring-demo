package com.ukonnra.wonderland.springelectrontest.desktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.service.AccountService;
import com.ukonnra.wonderland.springelectrontest.service.EntryService;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
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
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.transaction.annotation.Transactional;

@Command(group = "Journal Commands", command = "journals")
@Slf4j
public class JournalController {
  private final JournalService journalService;
  private final AccountService accountService;
  private final EntryService entryService;
  private final ObjectMapper objectMapper;

  public JournalController(
      JournalService journalService,
      AccountService accountService,
      EntryService entryService,
      ObjectMapper objectMapper) {
    this.journalService = journalService;
    this.accountService = accountService;
    this.entryService = entryService;
    this.objectMapper = objectMapper;
  }

  @Command(command = "find-all")
  @Transactional
  public String findAll(
      @Option(shortNames = 'i') Set<UUID> id,
      @Option(shortNames = 'n') Set<String> name,
      @Option(shortNames = 'u') Set<String> unit,
      @Option(shortNames = 'f') String fullText)
      throws JsonProcessingException {
    final var query = new Journal.Query();
    query.setId(id);
    query.setName(name);
    query.setUnit(unit);
    query.setFullText(fullText);
    final var results = this.journalService.findAll(query);
    return this.objectMapper.writeValueAsString(this.journalService.convert(results));
  }

  @Command(command = "init", description = "Init Data")
  @Transactional
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
