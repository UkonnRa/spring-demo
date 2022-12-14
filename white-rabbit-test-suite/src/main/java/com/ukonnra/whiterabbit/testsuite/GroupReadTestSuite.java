package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.group.GroupEntity;
import com.ukonnra.whiterabbit.core.domain.group.GroupQuery;
import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.group.QGroupEntity;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Sort;

@Slf4j
public abstract class GroupReadTestSuite
    extends ReadTestSuite<GroupReadTestSuite, GroupEntity, GroupQuery, GroupEntity.Dto> {
  private final GroupRepository repository;

  static Stream<Task.Read<GroupReadTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Read.FindOne<GroupReadTestSuite, GroupQuery, GroupEntity.Dto>(
            "Find by id",
            (suite) -> {
              final var group =
                  suite
                      .repository
                      .findAll(QGroupEntity.groupEntity.admins.any().role.ne(RoleValue.USER))
                      .iterator()
                      .next();
              final var user =
                  group.getAdmins().stream()
                      .filter(u -> u.getRole().compareTo(RoleValue.USER) > 0)
                      .findFirst()
                      .orElseThrow();

              return new TaskInput.Read.FindOne<>(
                  new TaskInput.AuthUser(user, null),
                  GroupQuery.builder()
                      .id(new IdQuery.Single(group.getId()))
                      .admins(Set.of(user.getId()))
                      .build());
            },
            (input) -> {
              final var query = input.input().query();
              Assertions.assertTrue(
                  input.result().orElseThrow().admins().stream()
                      .anyMatch(user -> query.admins().contains(user)));

              if (input.input().query().id() instanceof IdQuery.Single single) {
                Assertions.assertEquals(single.id(), input.result().orElseThrow().id());
              } else {
                Assertions.fail();
              }
            }),
        new Task.Read.FindPage<GroupReadTestSuite, GroupQuery, GroupEntity.Dto>(
            "Find by page",
            (suite) -> {
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
                  GroupQuery.builder().build());
            },
            false,
            true,
            (input) ->
                Assertions.assertEquals(
                    input.input().pagination().size(), input.result().items().size())));
  }

  protected GroupReadTestSuite(
      ReadTaskHandler<GroupReadTestSuite, GroupEntity, GroupQuery, GroupEntity.Dto> taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      GroupRepository groupRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.repository = groupRepository;
  }
}
