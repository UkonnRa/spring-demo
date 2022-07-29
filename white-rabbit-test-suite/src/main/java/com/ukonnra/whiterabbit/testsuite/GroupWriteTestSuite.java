package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.domain.group.GroupCommand;
import com.ukonnra.whiterabbit.core.domain.group.GroupEntity;
import com.ukonnra.whiterabbit.core.domain.group.GroupQuery;
import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Slf4j
public abstract class GroupWriteTestSuite
    extends WriteTestSuite<
        GroupWriteTestSuite, GroupEntity, GroupCommand, GroupQuery, GroupEntity.Dto> {
  private final GroupRepository repository;

  private static Map.Entry<Set<UUID>, Set<UUID>> prepareAdminsMembers(
      final GroupWriteTestSuite suite) {
    final var users =
        new ArrayList<>(
            suite
                .userRepository
                .findAll(PageRequest.of(0, 10, Sort.unsorted()))
                .map(AbstractEntity::getId)
                .toList());
    Collections.shuffle(users);
    return Map.entry(
        new HashSet<>(users.subList(0, 4)), new HashSet<>(users.subList(4, users.size())));
  }

  static Stream<Task.Write<GroupWriteTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Write.HandleCommand<GroupWriteTestSuite, GroupEntity, GroupCommand.Create>(
            "Create",
            (suite) -> {
              final var user =
                  suite
                      .userRepository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              final var adminsMembers = prepareAdminsMembers(suite);
              adminsMembers.getKey().add(user.getId());

              return new TaskInput.Write.HandleCommand<>(
                  new TaskInput.AuthUser(user, null),
                  new GroupCommand.Create(
                      null,
                      "new group name",
                      FAKER.lorem().sentence(10),
                      adminsMembers.getKey(),
                      adminsMembers.getValue()));
            },
            (input) -> {
              final var result = input.result().orElseThrow();
              final var command = input.input().command();
              Assertions.assertEquals(command.name(), result.getName());
              Assertions.assertEquals(command.description(), result.getDescription());
              Assertions.assertEquals(
                  command.admins(),
                  result.getAdmins().stream()
                      .map(AbstractEntity::getId)
                      .collect(Collectors.toSet()));
              Assertions.assertEquals(
                  command.members(),
                  result.getMembers().stream()
                      .map(AbstractEntity::getId)
                      .collect(Collectors.toSet()));
            }),
        new Task.Write.HandleCommands<GroupWriteTestSuite, GroupCommand, GroupEntity.Dto>(
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
              adminsMembers.getKey().add(user.getId());

              final var adminsMembers2 = prepareAdminsMembers(suite);
              adminsMembers2.getKey().remove(user.getId());
              adminsMembers2.getValue().add(user.getId());

              return new TaskInput.Write.HandleCommands<>(
                  new TaskInput.AuthUser(user, null),
                  List.of(
                      new GroupCommand.Create(
                          lid,
                          FAKER.expression(
                              "#{examplify 'DEP-123'} #{date.birthday '1','10','YYYY-MM-dd'}"),
                          FAKER.lorem().sentence(10),
                          adminsMembers.getKey(),
                          adminsMembers.getValue()),
                      new GroupCommand.Update(
                          lid,
                          null,
                          FAKER.lorem().sentence(10),
                          adminsMembers2.getKey(),
                          adminsMembers2.getValue()),
                      new GroupCommand.Delete(lid)));
            },
            (input) -> {
              final var commands = input.input().commands();
              final var results = input.result();
              Assertions.assertEquals(commands.size(), results.size());

              final var result0 = results.get(0).orElseThrow();
              if (commands.get(0) instanceof GroupCommand.Create create) {
                Assertions.assertEquals(create.name(), result0.name());
                Assertions.assertEquals(create.description(), result0.description());
                Assertions.assertEquals(create.admins(), result0.admins());
                Assertions.assertEquals(create.members(), result0.members());
              } else {
                Assertions.fail("commands[0] should be GroupCommand.Create");
              }

              final var result1 = results.get(1).orElseThrow();
              if (commands.get(1) instanceof GroupCommand.Update update) {
                Assertions.assertEquals(result0.name(), result1.name());
                Assertions.assertEquals(update.description(), result1.description());
                Assertions.assertEquals(update.admins(), result1.admins());
                Assertions.assertEquals(update.members(), result1.members());
              } else {
                Assertions.fail("commands[1] should be GroupCommand.Update");
              }

              final var result2 = results.get(2);
              if (commands.get(2) instanceof GroupCommand.Delete) {
                Assertions.assertTrue(result2.isEmpty());
              } else {
                Assertions.fail("commands[2] should be GroupCommand.Delete");
              }
            }));
  }

  protected GroupWriteTestSuite(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      GroupRepository repository,
      GroupService service) {
    super(dataGenerator, userRepository, service);
    this.repository = repository;
  }
}
