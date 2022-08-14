package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.account.AccountCommand;
import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountQuery;
import com.ukonnra.whiterabbit.core.domain.account.AccountStrategy;
import com.ukonnra.whiterabbit.core.domain.account.AccountType;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.QJournalEntity;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

@Slf4j
public abstract class AccountWriteTestSuite
    extends WriteTestSuite<
        AccountWriteTestSuite, AccountEntity, AccountCommand, AccountQuery, AccountEntity.Dto> {
  private final JournalRepository journalRepository;

  static Stream<Task.Write<AccountWriteTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Write.HandleCommand<AccountWriteTestSuite, AccountEntity, AccountCommand.Create>(
            "Create",
            (suite) -> {
              final var user =
                  suite
                      .userRepository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();

              final var journal =
                  suite
                      .journalRepository
                      .findAll(QJournalEntity.journalEntity.archived.isFalse())
                      .iterator()
                      .next();

              return new TaskInput.Write.HandleCommand<>(
                  new TaskInput.AuthUser(user, null),
                  new AccountCommand.Create(
                      null,
                      journal.getId(),
                      "new journal name",
                      FAKER.lorem().paragraph(),
                      AccountType.ASSET,
                      AccountStrategy.FIFO,
                      FAKER.money().currencyCode()));
            },
            (input) -> {
              final var result = input.result().orElseThrow();
              final var command = input.input().command();
              Assertions.assertEquals(command.name(), result.getName());
              Assertions.assertEquals(command.description(), result.getDescription());
              Assertions.assertEquals(command.accountType(), result.getType());
              Assertions.assertEquals(command.strategy(), result.getStrategy());
              Assertions.assertEquals(command.unit(), result.getUnit());
            }),
        new Task.Write.HandleCommands<AccountWriteTestSuite, AccountCommand, AccountEntity.Dto>(
            "Handle all commands",
            (suite) -> {
              final var user =
                  suite
                      .userRepository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();

              final var journal =
                  suite
                      .journalRepository
                      .findAll(QJournalEntity.journalEntity.archived.isFalse())
                      .iterator()
                      .next();

              final var lid = "lid";

              return new TaskInput.Write.HandleCommands<>(
                  new TaskInput.AuthUser(user, null),
                  List.of(
                      new AccountCommand.Create(
                          lid,
                          journal.getId(),
                          "new journal name",
                          FAKER.lorem().paragraph(),
                          AccountType.ASSET,
                          AccountStrategy.FIFO,
                          FAKER.money().currencyCode()),
                      new AccountCommand.Update(
                          lid,
                          null,
                          FAKER.lorem().sentence(10),
                          null,
                          AccountStrategy.AVERAGE,
                          null,
                          null),
                      new AccountCommand.Delete(lid)));
            },
            (input) -> {
              final var commands = input.input().commands();
              final var results = input.result();
              Assertions.assertEquals(commands.size(), results.size());

              final var result0 = results.get(0).orElseThrow();
              if (commands.get(0) instanceof AccountCommand.Create create) {
                Assertions.assertEquals(create.name(), result0.name());
                Assertions.assertEquals(create.description(), result0.description());
                Assertions.assertEquals(create.accountType(), result0.type());
                Assertions.assertEquals(create.strategy(), result0.strategy());
                Assertions.assertEquals(create.unit(), result0.unit());
                Assertions.assertFalse(result0.archived());
              } else {
                Assertions.fail("commands[0] should be AccountCommand.Create");
              }

              final var result1 = results.get(1).orElseThrow();
              if (commands.get(1) instanceof AccountCommand.Update update) {
                Assertions.assertEquals(result0.name(), result1.name());
                Assertions.assertEquals(update.description(), result1.description());
                Assertions.assertEquals(result0.type(), result1.type());
                Assertions.assertEquals(update.strategy(), result1.strategy());
                Assertions.assertEquals(result0.unit(), result1.unit());
                Assertions.assertFalse(result1.archived());
              } else {
                Assertions.fail("commands[1] should be AccountCommand.Update");
              }

              final var result2 = results.get(2);
              if (commands.get(2) instanceof AccountCommand.Delete) {
                Assertions.assertTrue(result2.isEmpty());
              } else {
                Assertions.fail("commands[2] should be AccountCommand.Delete");
              }
            }));
  }

  protected AccountWriteTestSuite(
      WriteTaskHandler<
              AccountWriteTestSuite, AccountEntity, AccountCommand, AccountQuery, AccountEntity.Dto>
          taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      JournalRepository journalRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.journalRepository = journalRepository;
  }
}
