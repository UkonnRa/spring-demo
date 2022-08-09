package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.account.QAccountEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.QJournalEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordCommand;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordItemValue;
import com.ukonnra.whiterabbit.core.domain.record.RecordQuery;
import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordType;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

@Slf4j
public abstract class RecordWriteTestSuite
    extends WriteTestSuite<
        RecordWriteTestSuite, RecordEntity, RecordCommand, RecordQuery, RecordEntity.Dto> {
  private final RecordRepository repository;
  private final AccountRepository accountRepository;

  private final JournalRepository journalRepository;

  private static Set<RecordItemValue.Dto> generateItems(
      final RecordWriteTestSuite suite, final JournalEntity journal) {
    final var builder = QAccountEntity.accountEntity;
    final var accounts =
        StreamSupport.stream(
                suite
                    .accountRepository
                    .findAll(builder.journal.eq(journal).and(builder.archived.isFalse()))
                    .spliterator(),
                true)
            .toList();
    return RecordItemValue.Dto.of(suite.dataGenerator.generateRecordItems(journal, accounts));
  }

  static Stream<Task.Write<RecordWriteTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Write.HandleCommand<RecordWriteTestSuite, RecordEntity, RecordCommand.Create>(
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

              final var items = generateItems(suite, journal);

              return new TaskInput.Write.HandleCommand<>(
                  new TaskInput.AuthUser(user, null),
                  new RecordCommand.Create(
                      null,
                      journal.getId(),
                      "new record name",
                      FAKER.lorem().paragraph(),
                      RecordType.RECORD,
                      FAKER.date().birthday().toLocalDateTime().toLocalDate(),
                      new HashSet<>(
                          FAKER.collection(() -> FAKER.color().name()).len(3, 5).generate()),
                      items));
            },
            (input) -> {
              final var result = input.result().orElseThrow();
              final var command = input.input().command();
              Assertions.assertEquals(command.name(), result.getName());
              Assertions.assertEquals(command.description(), result.getDescription());
              Assertions.assertEquals(command.recordType(), result.getType());
              Assertions.assertEquals(command.date(), result.getDate());
              Assertions.assertEquals(command.tags(), result.getTags());
              Assertions.assertEquals(command.items(), RecordItemValue.Dto.of(result.getItems()));
            }),
        new Task.Write.HandleCommands<RecordWriteTestSuite, RecordCommand, RecordEntity.Dto>(
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
                      new RecordCommand.Create(
                          lid,
                          journal.getId(),
                          "new record name",
                          FAKER.lorem().paragraph(),
                          RecordType.RECORD,
                          FAKER.date().birthday().toLocalDateTime().toLocalDate(),
                          new HashSet<>(
                              FAKER.collection(() -> FAKER.color().name()).len(3, 5).generate()),
                          generateItems(suite, journal)),
                      new RecordCommand.Update(
                          lid,
                          null,
                          FAKER.lorem().paragraph(),
                          RecordType.RECORD,
                          FAKER.date().birthday().toLocalDateTime().toLocalDate(),
                          new HashSet<>(
                              FAKER.collection(() -> FAKER.color().name()).len(3, 5).generate()),
                          generateItems(suite, journal)),
                      new RecordCommand.Delete(lid)));
            },
            (input) -> {
              final var commands = input.input().commands();
              final var results = input.result();
              Assertions.assertEquals(commands.size(), results.size());

              final var result0 = results.get(0).orElseThrow();
              if (commands.get(0) instanceof RecordCommand.Create create) {
                Assertions.assertEquals(create.name(), result0.name());
                Assertions.assertEquals(create.description(), result0.description());
                Assertions.assertEquals(create.recordType(), result0.type());
                Assertions.assertEquals(create.date(), result0.date());
                Assertions.assertEquals(create.tags(), result0.tags());
                Assertions.assertEquals(create.items(), result0.items());
              } else {
                Assertions.fail("commands[0] should be RecordCommand.Create");
              }

              final var result1 = results.get(1).orElseThrow();
              if (commands.get(1) instanceof RecordCommand.Update update) {
                Assertions.assertEquals(result0.name(), result1.name());
                Assertions.assertEquals(update.description(), result1.description());
                Assertions.assertEquals(update.recordType(), result1.type());
                Assertions.assertEquals(update.date(), result1.date());
                Assertions.assertEquals(update.tags(), result1.tags());
                Assertions.assertEquals(update.items(), result1.items());
              } else {
                Assertions.fail("commands[1] should be RecordCommand.Update");
              }

              final var result2 = results.get(2);
              if (commands.get(2) instanceof RecordCommand.Delete) {
                Assertions.assertTrue(result2.isEmpty());
              } else {
                Assertions.fail("commands[2] should be RecordCommand.Delete");
              }
            }));
  }

  protected RecordWriteTestSuite(
      WriteTaskHandler<
              RecordWriteTestSuite, RecordEntity, RecordCommand, RecordQuery, RecordEntity.Dto>
          taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      RecordRepository repository,
      AccountRepository accountRepository,
      JournalRepository journalRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.repository = repository;
    this.accountRepository = accountRepository;
    this.journalRepository = journalRepository;
  }
}
