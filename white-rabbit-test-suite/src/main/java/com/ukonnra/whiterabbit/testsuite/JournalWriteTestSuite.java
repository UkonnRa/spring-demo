package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.journal.JournalCommand;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalQuery;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

@Slf4j
public abstract class JournalWriteTestSuite
    extends WriteTestSuite<
        JournalWriteTestSuite, JournalEntity, JournalCommand, JournalQuery, JournalEntity.Dto> {

  private final GroupRepository groupRepository;

  private static Map.Entry<Set<AccessItemValue>, Set<AccessItemValue>> prepareAdminsMembers(
      final JournalWriteTestSuite suite) {
    final var users = suite.userRepository.findAll();
    final var groups = suite.groupRepository.findAll();
    return suite.dataGenerator.generateAccessItems(users, groups);
  }

  static Stream<Task.Write<JournalWriteTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Write.HandleCommand<JournalWriteTestSuite, JournalEntity, JournalCommand.Create>(
            "Create",
            (suite) -> {
              final var user =
                  suite
                      .userRepository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              final var adminsMembers = prepareAdminsMembers(suite);
              adminsMembers.getKey().add(new AccessItemValue(user));

              return new TaskInput.Write.HandleCommand<>(
                  new TaskInput.AuthUser(user, null),
                  new JournalCommand.Create(
                      null,
                      "new journal name",
                      FAKER.lorem().paragraph(),
                      Set.of("tag 1", "tag 2", "tag 3"),
                      FAKER.money().currencyCode(),
                      adminsMembers.getKey(),
                      adminsMembers.getValue()));
            },
            (input) -> {
              final var result = input.result().orElseThrow();
              final var command = input.input().command();
              Assertions.assertEquals(command.name(), result.getName());
              Assertions.assertEquals(command.description(), result.getDescription());
              Assertions.assertTrue(command.admins().containsAll(result.getAdmins()));
              Assertions.assertTrue(command.members().containsAll(result.getMembers()));
            }),
        new Task.Write.HandleCommands<JournalWriteTestSuite, JournalCommand, JournalEntity.Dto>(
            "Handle all commands",
            (suite) -> {
              final var user =
                  suite
                      .userRepository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              final var lid = "lid";
              final var adminsMembers = prepareAdminsMembers(suite);
              adminsMembers.getKey().add(new AccessItemValue(user));

              final var adminsMembers2 = prepareAdminsMembers(suite);
              adminsMembers2.getKey().remove(new AccessItemValue(user));
              adminsMembers.getValue().add(new AccessItemValue(user));

              return new TaskInput.Write.HandleCommands<>(
                  new TaskInput.AuthUser(user, null),
                  List.of(
                      new JournalCommand.Create(
                          lid,
                          "new journal name",
                          FAKER.lorem().paragraph(),
                          Set.of("tag 1", "tag 2", "tag 3"),
                          FAKER.money().currencyCode(),
                          adminsMembers.getKey(),
                          adminsMembers.getValue()),
                      new JournalCommand.Update(
                          lid,
                          null,
                          FAKER.lorem().sentence(10),
                          null,
                          FAKER.money().currencyCode(),
                          true,
                          adminsMembers2.getKey(),
                          adminsMembers2.getValue()),
                      new JournalCommand.Delete(lid)));
            },
            (input) -> {
              final var commands = input.input().commands();
              final var results = input.result();
              Assertions.assertEquals(commands.size(), results.size());

              final var result0 = results.get(0).orElseThrow();
              if (commands.get(0) instanceof JournalCommand.Create create) {
                Assertions.assertEquals(create.name(), result0.name());
                Assertions.assertEquals(create.description(), result0.description());
                Assertions.assertEquals(create.tags(), result0.tags());
                Assertions.assertEquals(create.unit(), result0.unit());
                Assertions.assertFalse(result0.archived());
                Assertions.assertTrue(create.admins().containsAll(result0.admins()));
                Assertions.assertTrue(create.members().containsAll(result0.members()));
              } else {
                Assertions.fail("commands[0] should be JournalCommand.Create");
              }

              final var result1 = results.get(1).orElseThrow();
              if (commands.get(1) instanceof JournalCommand.Update update) {
                Assertions.assertEquals(result0.name(), result1.name());
                Assertions.assertEquals(update.description(), result1.description());
                Assertions.assertEquals(result0.tags(), result1.tags());
                Assertions.assertEquals(update.unit(), result1.unit());
                Assertions.assertTrue(result1.archived());
                Assertions.assertTrue(
                    update.admins() != null && update.admins().containsAll(result1.admins()));
                Assertions.assertTrue(
                    update.members() != null && update.members().containsAll(result1.members()));
              } else {
                Assertions.fail("commands[1] should be JournalCommand.Update");
              }

              final var result2 = results.get(2);
              if (commands.get(2) instanceof JournalCommand.Delete) {
                Assertions.assertTrue(result2.isEmpty());
              } else {
                Assertions.fail("commands[2] should be JournalCommand.Delete");
              }
            }));
  }

  protected JournalWriteTestSuite(
      WriteTaskHandler<
              JournalWriteTestSuite, JournalEntity, JournalCommand, JournalQuery, JournalEntity.Dto>
          taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      GroupRepository groupRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.groupRepository = groupRepository;
  }
}
