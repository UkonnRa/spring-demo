package com.ukonnra.wonderland.springelectrontest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.wonderland.springelectrontest.entity.Account;
import com.ukonnra.wonderland.springelectrontest.entity.Entry;
import com.ukonnra.wonderland.springelectrontest.entity.HierarchyReport;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface HierarchyReportService
    extends ReadService<HierarchyReport, HierarchyReport.Query> {
  AccountService accountService();

  EntryService entryService();

  ObjectMapper objectMapper();

  Logger log();

  private @Nullable PrefixIndex decodeId(final String id) {
    try {
      final var jsonString = Base64.getUrlDecoder().decode(id.getBytes(StandardCharsets.UTF_8));
      return this.objectMapper().readValue(jsonString, PrefixIndex.class);
    } catch (IllegalArgumentException | IOException e) {
      this.log().warn("Error when decode Report Id: {}", e.getMessage(), e);
      return null;
    }
  }

  private @Nullable String encodeId(final PrefixIndex id) {
    try {
      final var jsonString = this.objectMapper().writeValueAsString(id);
      return Base64.getUrlEncoder().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));
    } catch (JsonProcessingException e) {
      this.log().warn("Error when encode Report Id: {}", e.getMessage(), e);
      return null;
    }
  }

  @Override
  @Transactional
  default List<HierarchyReport> findAll(HierarchyReport.Query query) {
    this.log().info("== Start Finding All Hierarchy Reports");

    final var reportIds =
        query.id().stream()
            .map(this::decodeId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    final var journalIds =
        Stream.concat(query.journal().stream(), reportIds.stream().map(id -> id.journalId))
            .collect(Collectors.toSet());

    final var accountQuery = new Account.Query();
    accountQuery.setJournal(journalIds);
    final var accounts = this.accountService().findAll(accountQuery);
    this.log().info("  Accounts: {}", accounts);

    final var entryQuery = new Entry.Query();
    entryQuery.setJournal(journalIds);
    entryQuery.setStart(query.start());
    entryQuery.setEnd(query.end());
    final var entries = this.entryService().findAll(entryQuery);
    this.log().info("  Entries: {}", accounts);

    final var results =
        this.doAggregateByAccount(accounts, entries).entrySet().stream()
            .filter(
                e -> {
                  if (query.id().isEmpty()) {
                    return true;
                  }
                  return reportIds.contains(new PrefixIndex(e.getKey()));
                })
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
                  final var id = this.encodeId(index);
                  if (id == null || id.isEmpty()) {
                    return null;
                  }

                  final var values =
                      e.getValue().stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                  return new HierarchyReport(id, index.journalId, index.prefix, index.unit, values);
                })
            .filter(Objects::nonNull)
            .toList();

    this.log().info("  Reports: {}", results);

    return results;
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

  record AccountIndex(UUID journalId, String prefix, String unit, UUID accountId) {}

  record PrefixIndex(UUID journalId, String prefix, String unit) {
    public PrefixIndex(final AccountIndex index) {
      this(index.journalId, index.prefix, index.unit);
    }

    public PrefixIndex(final HierarchyReport report) {
      this(report.journalId(), report.prefix(), report.unit());
    }
  }

  @Service
  @Slf4j
  class Impl implements HierarchyReportService {
    private final AccountService accountService;
    private final EntryService entryService;
    private final ObjectMapper objectMapper;

    public Impl(
        AccountService accountService, EntryService entryService, ObjectMapper objectMapper) {
      this.accountService = accountService;
      this.entryService = entryService;
      this.objectMapper = objectMapper;
    }

    public AccountService accountService() {
      return this.accountService;
    }

    public EntryService entryService() {
      return this.entryService;
    }

    public ObjectMapper objectMapper() {
      return this.objectMapper;
    }

    public Logger log() {
      return log;
    }
  }
}
