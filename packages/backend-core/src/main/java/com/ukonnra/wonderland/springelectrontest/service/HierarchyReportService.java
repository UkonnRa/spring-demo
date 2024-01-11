package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.HierarchyReport;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HierarchyReportService implements ReadService<HierarchyReport, HierarchyReport.Query> {
  private final AccountService accountService;
  private final EntryService entryService;

  public HierarchyReportService(AccountService accountService, EntryService entryService) {
    this.accountService = accountService;
    this.entryService = entryService;
  }

  @Override
  @Transactional
  public List<HierarchyReport> findAll(HierarchyReport.Query query) {
    if (query.journal().isEmpty()) {
      return List.of();
    }

    final var accountQuery = new Account.Query();
    accountQuery.setJournal(query.journal());
    final var accounts = this.accountService.findAll(accountQuery);

    final var entryQuery = new Entry.Query();
    entryQuery.setJournal(query.journal());
    entryQuery.setStart(query.start());
    entryQuery.setEnd(query.end());
    final var entries = this.entryService.findAll(entryQuery);

    return this.doAggregateByAccount(accounts, entries).entrySet().stream()
        .collect(
            Collectors.groupingBy(
                e -> new PrefixIndex(e.getKey()),
                Collectors.mapping(
                    e -> Map.entry(e.getKey().accountId(), e.getValue()), Collectors.toSet())))
        .entrySet()
        .stream()
        .map(
            e -> {
              final var index = e.getKey();
              final var values =
                  e.getValue().stream()
                      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
              return new HierarchyReport(index.journalId, index.prefix, index.unit, values);
            })
        .toList();
  }

  private Map<AccountIndex, BigDecimal> doAggregateByAccount(
      List<Account> accounts, List<Entry> entries) {
    final var accountMap =
        accounts.stream().collect(Collectors.toMap(Account::getId, Function.identity()));
    final var resultsByAccount = new HashMap<AccountIndex, BigDecimal>();

    for (final var entry : entries) {
      for (final var item : entry.getItems()) {
        final var account = accountMap.get(item.getAccount().getId());
        if (account == null
            || !Objects.equals(entry.getJournal().getId(), account.getJournal().getId())) {
          continue;
        }

        final var offset = item.getAmount().multiply(item.getPrice());
        final var journalId = account.getJournal().getId();
        for (final var prefix : account.getNamePrefixes()) {
          resultsByAccount.compute(
              new AccountIndex(journalId, prefix, account.getUnit(), account.getId()),
              (index, a) -> {
                final var aVal = a == null ? BigDecimal.ZERO : a;
                return aVal.add(offset);
              });
        }
      }
    }

    return resultsByAccount;
  }

  private record AccountIndex(UUID journalId, String prefix, String unit, UUID accountId) {}

  private record PrefixIndex(UUID journalId, String prefix, String unit) {
    public PrefixIndex(final AccountIndex index) {
      this(index.journalId, index.prefix, index.unit);
    }
  }
}
