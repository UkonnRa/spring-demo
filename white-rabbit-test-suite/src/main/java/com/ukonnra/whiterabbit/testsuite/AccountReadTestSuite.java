package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountQuery;
import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.account.QAccountEntity;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.QJournalEntity;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Sort;

@Slf4j
public abstract class AccountReadTestSuite
    extends ReadTestSuite<AccountReadTestSuite, AccountEntity, AccountQuery, AccountEntity.Dto> {
  private final AccountRepository repository;
  private final JournalRepository journalRepository;

  static Stream<Task.Read<AccountReadTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Read.FindOne<AccountReadTestSuite, AccountQuery, AccountEntity.Dto>(
            "Find by id",
            (suite) -> {
              final var accountBuilder = QAccountEntity.accountEntity;
              final var journalSubBuilder = QAccountEntity.accountEntity.journal;

              final var account =
                  suite
                      .repository
                      .findAll(
                          accountBuilder
                              .archived
                              .isFalse()
                              .and(
                                  journalSubBuilder
                                      .admins
                                      .any()
                                      .itemType
                                      .eq(AccessItemValue.Type.USER))
                              .and(journalSubBuilder.archived.isFalse()))
                      .iterator()
                      .next();
              final var user =
                  account.getJournal().getAdmins().stream()
                      .filter(item -> item.getItemType() == AccessItemValue.Type.USER)
                      .findFirst()
                      .flatMap(item -> suite.userRepository.findById(item.getId()))
                      .orElseThrow();
              return new TaskInput.Read.FindOne<>(
                  new TaskInput.AuthUser(user, null),
                  AccountQuery.builder()
                      .id(new IdQuery.Single(account.getId()))
                      .journal(account.getJournal().getId())
                      .build());
            },
            (input) -> {
              final var query = input.input().query();
              final var result = input.result().orElseThrow();
              Assertions.assertEquals(query.journal(), result.journal());

              if (input.input().query().id() instanceof IdQuery.Single single) {
                Assertions.assertEquals(single.id(), result.id());
              } else {
                Assertions.fail();
              }
            }),
        new Task.Read.FindPage<AccountReadTestSuite, AccountQuery, AccountEntity.Dto>(
            "Find by page",
            (suite) -> {
              final var journal =
                  suite
                      .journalRepository
                      .findAll(QJournalEntity.journalEntity.archived.isFalse())
                      .iterator()
                      .next();
              final var user =
                  suite
                      .userRepository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              return new TaskInput.Read.FindPage<>(
                  TaskInput.AuthUser.builder()
                      .authId(user.getAuthIds().stream().findFirst().orElseThrow())
                      .build(),
                  Pagination.DEFAULT,
                  Sort.by(Sort.Order.desc("name")),
                  AccountQuery.builder().includeArchived(true).journal(journal.getId()).build());
            },
            false,
            true,
            (input) ->
                Assertions.assertEquals(
                    input.input().pagination().size(), input.result().items().size())));
  }

  protected AccountReadTestSuite(
      ReadTaskHandler<AccountReadTestSuite, AccountEntity, AccountQuery, AccountEntity.Dto>
          taskHandler,
      UserRepository userRepository,
      DataGenerator dataGenerator,
      AccountRepository accountRepository,
      JournalRepository journalRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.repository = accountRepository;
    this.journalRepository = journalRepository;
  }
}
