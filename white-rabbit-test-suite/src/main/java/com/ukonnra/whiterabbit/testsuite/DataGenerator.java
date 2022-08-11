package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.account.AccountStrategy;
import com.ukonnra.whiterabbit.core.domain.account.AccountType;
import com.ukonnra.whiterabbit.core.domain.group.GroupEntity;
import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordItemValue;
import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordType;
import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class DataGenerator {
  private static final String PROVIDER_AUTHING = "Authing";
  private static final Faker FAKER = new Faker();
  private static final Random RANDOM = new Random();

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final JournalRepository journalRepository;
  private final AccountRepository accountRepository;
  private final RecordRepository recordRepository;

  public DataGenerator(
      UserRepository userRepository,
      GroupRepository groupRepository,
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      RecordRepository recordRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
    this.recordRepository = recordRepository;
  }

  private <T> List<T> generateUnique(Supplier<T> supplier, int size, Set<T> store) {
    final var generator = FAKER.collection(supplier).len(size * 2);
    final var result = new HashSet<T>();
    while (result.size() < size) {
      final var generated =
          generator.generate().stream().filter(item -> !store.contains(item)).toList();
      if (generated.isEmpty()) {
        break;
      }
      result.addAll(generated);
      store.addAll(generated);
    }
    return result.stream().limit(size).toList();
  }

  private <T> List<T> generateUnique(Supplier<T> supplier, int size) {
    return this.generateUnique(supplier, size, new HashSet<>());
  }

  public List<UserEntity> generateUsers(
      final Set<String> usernameStore,
      final RoleValue role,
      final List<String> providers,
      int size) {
    return this.generateUnique(() -> FAKER.name().username(), size, usernameStore).stream()
        .map(
            name -> {
              Collections.shuffle(providers);
              final var authIds =
                  providers.stream()
                      .limit(3)
                      .map(provider -> new AuthIdValue(provider, UUID.randomUUID().toString()))
                      .collect(Collectors.toSet());
              return new UserEntity(name, role, authIds);
            })
        .toList();
  }

  public List<GroupEntity> generateGroups(final List<UserEntity> users, int size) {
    return this.generateUnique(() -> FAKER.expression("GROUP-#{examplify 'GRP-123'}"), size)
        .stream()
        .map(
            name -> {
              Collections.shuffle(users);
              final var adminSize = RANDOM.nextInt(1, GroupEntity.ITEM_MAX_LENGTH / 2);
              final var memberSize = RANDOM.nextInt(5, GroupEntity.ITEM_MAX_LENGTH);
              final var admins = new HashSet<>(users.subList(0, adminSize));
              final var members = new HashSet<>(users.subList(adminSize, adminSize + memberSize));
              return new GroupEntity(name, FAKER.lorem().paragraph(), admins, members);
            })
        .toList();
  }

  public Map.Entry<Set<AccessItemValue>, Set<AccessItemValue>> generateAccessItems(
      final Collection<UserEntity> users, final Collection<GroupEntity> groups) {
    final var adminSize = RANDOM.nextInt(1, JournalEntity.ACCESS_MAX_LENGTH);
    final var memberSize = RANDOM.nextInt(5, JournalEntity.ACCESS_MAX_LENGTH);

    final var entities =
        new ArrayList<>(
            Stream.concat(
                    AccessItemValue.ofUsers(users).stream(),
                    AccessItemValue.ofGroups(groups).stream())
                .toList());
    Collections.shuffle(entities);
    final var admins = entities.subList(0, adminSize);
    final var members = entities.subList(adminSize, adminSize + memberSize);
    return Map.entry(new HashSet<>(admins), new HashSet<>(members));
  }

  public List<JournalEntity> generateJournals(
      final Collection<UserEntity> users, final Collection<GroupEntity> groups, int size) {
    return this.generateUnique(
            () -> FAKER.expression("#{examplify 'DEP-123'} #{date.birthday '1','10','YYYY-MM-dd'}"),
            size)
        .stream()
        .map(
            name -> {
              final var adminsMembers = this.generateAccessItems(users, groups);
              return new JournalEntity(
                  name,
                  FAKER.lorem().paragraph(),
                  new HashSet<>(
                      FAKER
                          .collection(() -> FAKER.color().name())
                          .len(AbstractEntity.TAG_MAX_LENGTH)
                          .generate()),
                  FAKER.money().currencyCode(),
                  RANDOM.nextFloat() < 0.2,
                  adminsMembers.getKey(),
                  adminsMembers.getValue());
            })
        .toList();
  }

  public List<AccountEntity> generateAccounts(
      final Collection<JournalEntity> journals, int sizePerJournal) {
    return journals.stream()
        .flatMap(
            journal ->
                this.generateUnique(() -> FAKER.book().title(), sizePerJournal).stream()
                    .map(
                        name ->
                            new AccountEntity(
                                journal,
                                name,
                                FAKER.lorem().paragraph(),
                                AccountType.values()[
                                    RANDOM.nextInt(0, AccountType.values().length)],
                                AccountStrategy.values()[
                                    RANDOM.nextInt(0, AccountStrategy.values().length)],
                                FAKER.money().currencyCode(),
                                RANDOM.nextDouble() < 0.2)))
        .toList();
  }

  private BigDecimal nextPositive() {
    return new BigDecimal(String.format("%.2f", RANDOM.nextDouble(0.0, 100.0)));
  }

  public Set<RecordItemValue> generateRecordItems(
      final JournalEntity journal, final Collection<AccountEntity> accounts) {
    final var relatedAccounts =
        new ArrayList<>(
            accounts.stream().filter(account -> account.getJournal().equals(journal)).toList());
    Collections.shuffle(relatedAccounts);
    final var recordItems =
        relatedAccounts
            .subList(
                0,
                RANDOM.nextInt(
                    RecordEntity.ITEM_MIN_LENGTH,
                    Math.min(relatedAccounts.size(), RecordEntity.ITEM_MAX_LENGTH)))
            .stream()
            .map(
                account ->
                    new RecordItemValue(
                        account,
                        nextPositive(),
                        account.getUnit().equals(journal.getUnit()) ? null : nextPositive()))
            .toList();
    // Make the record valid
    if (RANDOM.nextDouble() >= 0.2) {
      Optional.ofNullable(recordItems.get(0))
          .ifPresent(
              item -> {
                item.setPrice(null);
                item.setAmount(null);
              });
    }
    return new HashSet<>(recordItems);
  }

  public List<RecordEntity> generateRecords(
      final Collection<JournalEntity> journals,
      final Collection<AccountEntity> accounts,
      final int sizePerJournal) {
    return journals.stream()
        .flatMap(
            journal ->
                this.generateUnique(() -> FAKER.job().title(), sizePerJournal).stream()
                    .map(
                        name ->
                            new RecordEntity(
                                journal,
                                name,
                                FAKER.lorem().paragraph(),
                                RecordType.RECORD,
                                FAKER.date().birthday(1, 2).toLocalDateTime().toLocalDate(),
                                new HashSet<>(
                                    FAKER
                                        .collection(() -> FAKER.color().name())
                                        .len(AbstractEntity.TAG_MAX_LENGTH)
                                        .generate()),
                                this.generateRecordItems(journal, accounts))))
        .toList();
  }

  @Transactional
  public void prepareData() {
    log.info("Start preparing data");
    final var providers = new ArrayList<>(this.generateUnique(() -> FAKER.commerce().vendor(), 5));
    final var usernameStore = new HashSet<String>();

    final var owners =
        this.generateUsers(usernameStore, RoleValue.OWNER, providers, RANDOM.nextInt(3, 5));
    final var admins =
        this.generateUsers(usernameStore, RoleValue.ADMIN, providers, RANDOM.nextInt(5, 8));
    final var normals =
        this.generateUsers(usernameStore, RoleValue.USER, providers, RANDOM.nextInt(13, 21));
    final var specificUsers =
        List.of(
            // TODO: the cleanup function after tests is not working
            new UserEntity(
                UUID.randomUUID().toString(),
                RoleValue.USER,
                Set.of(new AuthIdValue(PROVIDER_AUTHING, "62b9aa44030fd558ca2a13aa"))),
            new UserEntity(
                UUID.randomUUID().toString(),
                RoleValue.ADMIN,
                Set.of(new AuthIdValue(PROVIDER_AUTHING, "62b88497083e77d5faca3c29"))),
            new UserEntity(
                UUID.randomUUID().toString(),
                RoleValue.OWNER,
                Set.of(new AuthIdValue(PROVIDER_AUTHING, "62b7e12becdff87b52da3296"))),
            new UserEntity(
                UUID.randomUUID().toString(),
                RoleValue.USER,
                Set.of(new AuthIdValue(PROVIDER_AUTHING, "62b8642831d7c43e242820f3"))));
    final var users =
        this.userRepository.saveAll(
            Stream.of(owners, admins, normals, specificUsers).flatMap(Collection::stream).toList());

    final var groups = this.groupRepository.saveAll(this.generateGroups(users, 16));
    final var journals = this.journalRepository.saveAll(this.generateJournals(users, groups, 16));
    final var accounts = this.accountRepository.saveAll(this.generateAccounts(journals, 16));
    this.recordRepository.saveAll(this.generateRecords(journals, accounts, 16));
    log.info("End preparing data");
  }

  @Transactional
  public void clear() {
    log.info("Start clearing data");
    this.recordRepository.deleteAll();
    this.accountRepository.deleteAll();
    this.journalRepository.deleteAll();
    this.accountRepository.deleteAll();
    this.userRepository.deleteAll();
    log.info("End clearing data");
  }
}
