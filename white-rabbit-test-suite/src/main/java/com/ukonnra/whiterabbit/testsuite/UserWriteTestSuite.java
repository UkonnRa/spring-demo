package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserCommand;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserQuery;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

@Slf4j
public abstract class UserWriteTestSuite
    extends WriteTestSuite<UserWriteTestSuite, UserEntity, UserCommand, UserQuery, UserEntity.Dto> {
  private final UserRepository repository;

  static Stream<Task.Write<UserWriteTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Write.HandleCommand<UserWriteTestSuite, UserCommand.Create, UserEntity.Dto>(
            "Create",
            (suite) -> {
              final var user =
                  suite
                      .repository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              return new TaskInput.Write.HandleCommand<>(
                  new TaskInput.AuthUser(user, null),
                  new UserCommand.Create(
                      null,
                      "new.user.name",
                      RoleValue.ADMIN,
                      Set.of(new AuthIdValue("provider 1", UUID.randomUUID().toString()))));
            },
            (input) -> {
              final var result = input.result().orElseThrow();
              final var command = input.input().command();
              Assertions.assertEquals(command.name(), result.name());
              Assertions.assertEquals(command.role(), result.role());
              Assertions.assertEquals(command.authIds(), result.authIds());
            }),
        new Task.Write.HandleCommand<UserWriteTestSuite, UserCommand.Delete, UserEntity.Dto>(
            "Delete user by id by OWNER",
            (suite) -> {
              final var user =
                  suite
                      .repository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              final var deleted =
                  suite
                      .repository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.USER))
                      .iterator()
                      .next();
              return new TaskInput.Write.HandleCommand<>(
                  new TaskInput.AuthUser(user, null),
                  new UserCommand.Delete(deleted.getId().toString()));
            },
            (input) -> Assertions.assertTrue(input.result().isEmpty())),
        new Task.Write.HandleCommands<UserWriteTestSuite, UserCommand, UserEntity.Dto>(
            "Handle all commands",
            (suite) -> {
              final var user =
                  suite
                      .repository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              final var lid = "lid";
              return new TaskInput.Write.HandleCommands<>(
                  new TaskInput.AuthUser(user, null),
                  List.of(
                      new UserCommand.Create(
                          lid,
                          "name 1",
                          RoleValue.ADMIN,
                          Set.of(new AuthIdValue("provider 2", "value 1"))),
                      new UserCommand.Update(
                          lid,
                          "name updated 1",
                          null,
                          Set.of(new AuthIdValue("provider 2", "value new 1"))),
                      new UserCommand.Delete(lid)));
            },
            (input) -> {
              final var commands = input.input().commands();
              final var results = input.result();
              Assertions.assertEquals(commands.size(), results.size());

              final var result0 = results.get(0).orElseThrow();
              if (commands.get(0) instanceof UserCommand.Create create) {
                Assertions.assertEquals(create.name(), result0.name());
                Assertions.assertEquals(create.role(), result0.role());
                Assertions.assertEquals(create.authIds(), result0.authIds());
              } else {
                Assertions.fail("commands[0] should be UserCommand.Create");
              }

              final var result1 = results.get(1).orElseThrow();
              if (commands.get(1) instanceof UserCommand.Update update) {
                Assertions.assertEquals(update.name(), result1.name());
                Assertions.assertEquals(result0.role(), result1.role());
                Assertions.assertEquals(update.authIds(), result1.authIds());
              } else {
                Assertions.fail("commands[1] should be UserCommand.Update");
              }

              final var result2 = results.get(2);
              if (commands.get(2) instanceof UserCommand.Delete) {
                Assertions.assertTrue(result2.isEmpty());
              } else {
                Assertions.fail("commands[2] should be UserCommand.Delete");
              }
            }));
  }

  protected UserWriteTestSuite(
      WriteTaskHandler<UserWriteTestSuite, UserEntity, UserCommand, UserQuery, UserEntity.Dto>
          taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.repository = userRepository;
  }
}
