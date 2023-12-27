package com.ukonnra.wonderland.springelectrontest;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.repository.AccountRepository;
import com.ukonnra.wonderland.springelectrontest.repository.EntryRepository;
import com.ukonnra.wonderland.springelectrontest.repository.JournalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EntityScan(basePackageClasses = AbstractEntity.class)
@EnableJpaRepositories(basePackageClasses = CoreConfiguration.class)
@EnableJpaAuditing
@EnableTransactionManagement
@Slf4j
public class CoreConfiguration {
  private final JournalRepository journalRepository;
  private final AccountRepository accountRepository;
  private final EntryRepository entryRepository;

  public CoreConfiguration(
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      EntryRepository entryRepository) {
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
    this.entryRepository = entryRepository;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void onApplicationReady() {
    var journals =
        List.of(
            new Journal("Name 1", "Desc 1", "Unit 1", Set.of("Tag 1", "Tag 2")),
            new Journal("Name 2", "Desc 2", "Unit 2", Set.of("Tag 2", "Tag 4")),
            new Journal("Name 3", "Desc 3", "Unit 3", Set.of("Tag 1", "Tag 4")));

    journals = this.journalRepository.saveAllAndFlush(journals);

    final var journalQuery = new Journal.Query();
    journalQuery.setTag(Set.of("Tag 4", "Tag 2"));
    journalQuery.setFullText(" 2");
    for (final var journal : this.journalRepository.findAll(journalQuery)) {
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
    accounts = this.accountRepository.saveAllAndFlush(accounts);

    final var accountQuery = new Account.Query();
    accountQuery.setTag(Set.of("Tag 4", "Tag 2"));
    accountQuery.setUnit(Set.of("Unit 1"));
    accountQuery.setFullText(Account.Type.ASSET.name());

    for (final var account : this.accountRepository.findAll(accountQuery)) {
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
    entries = this.entryRepository.saveAllAndFlush(entries);
    for (final var entry : entries) {
      log.info("Entry: {}", entry);
    }
  }
}
