package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.EntryDto;
import com.ukonnra.wonderland.springelectrontest.entity.EntryState;
import com.ukonnra.wonderland.springelectrontest.repository.EntryRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntryService
    implements WriteService<Entry, Entry.Query, Object, EntryRepository, EntryDto> {
  private final EntryRepository repository;
  private final JournalService journalService;
  private final AccountService accountService;

  public EntryService(
      EntryRepository repository, JournalService journalService, AccountService accountService) {
    this.repository = repository;
    this.journalService = journalService;
    this.accountService = accountService;
  }

  @Override
  public List<Entry> handleCommand(Object command) {
    return null;
  }

  private EntryState doGetRecordState(final Entry entry) {
    var left = BigDecimal.ZERO;
    var right = BigDecimal.ZERO;

    for (final var item : entry.getItems()) {
      final var value = item.getAmount().multiply(item.getPrice());
      if (item.getAccount().getType() == Account.Type.EXPENSE
          || item.getAccount().getType() == Account.Type.ASSET) {
        left = left.add(value);
      } else {
        right = right.add(value);
      }
    }

    if (left.equals(right)) {
      return new EntryState.Valid(left);
    } else {
      return new EntryState.Invalid(left, right);
    }
  }

  private Map<UUID, EntryState> doGetCheckState(
      final Entry entry, final Collection<Entry> entries) {
    final var actualByAccount =
        entries.stream()
            .filter(e -> e.getType() == Entry.Type.RECORD && !e.getDate().isAfter(entry.getDate()))
            .flatMap(e -> e.getItems().stream())
            .collect(
                Collectors.groupingBy(
                    item -> Objects.requireNonNull(item.getAccount().getId()),
                    Collectors.mapping(
                        item -> item.getPrice().multiply(item.getAmount()),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

    return entry.getItems().stream()
        .map(
            item -> {
              final var accountId = Objects.requireNonNull(item.getAccount().getId());
              final var expected = item.getAmount().multiply(item.getPrice());
              final var actual = actualByAccount.getOrDefault(accountId, BigDecimal.ZERO);
              if (expected.equals(actual)) {
                return Map.entry(accountId, new EntryState.Valid(expected));
              } else {
                return Map.entry(accountId, new EntryState.Invalid(expected, actual));
              }
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  @Transactional
  public List<EntryDto> convert(Collection<Entry> entities) {
    final var accountIds =
        entities.stream()
            .flatMap(e -> e.getItems().stream())
            .map(i -> i.getAccount().getId())
            .collect(Collectors.toSet());

    final var accountQuery = new Account.Query();
    accountQuery.setId(accountIds);
    final var accountsByJournal =
        this.accountService.findAll(accountQuery).stream()
            .collect(
                Collectors.groupingBy(
                    a -> Objects.requireNonNull(a.getJournal().getId()),
                    Collectors.mapping(
                        Function.identity(),
                        Collectors.toMap(Account::getId, Function.identity()))));

    final var recordQuery = new Entry.Query();
    recordQuery.setJournal(accountsByJournal.keySet());
    recordQuery.setType(Entry.Type.RECORD);
    final var recordsByJournal =
        this.findAll(recordQuery).stream()
            .collect(
                Collectors.groupingBy(
                    a -> Objects.requireNonNull(a.getJournal().getId()),
                    Collectors.mapping(Function.identity(), Collectors.toSet())));

    final var results = new ArrayList<EntryDto>();

    for (final var entity : entities) {
      if (entity.getType() == Entry.Type.RECORD) {
        final var state = this.doGetRecordState(entity);
        results.add(new EntryDto.Record(entity, state));
      } else {
        final var recordMap = recordsByJournal.getOrDefault(entity.getJournal().getId(), Set.of());

        final var state = this.doGetCheckState(entity, recordMap);
        results.add(new EntryDto.Check(entity, state));
      }
    }

    return results;
  }

  @Override
  public EntryRepository getRepository() {
    return this.repository;
  }
}
