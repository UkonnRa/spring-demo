package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.RoleValue;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserQuery;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Sort;

@Slf4j
public abstract class UserReadTestSuite
    extends ReadTestSuite<UserReadTestSuite, UserEntity, UserQuery, UserEntity.Dto> {
  private final UserRepository repository;

  static Stream<Task.Read<UserReadTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Read.FindOne<UserReadTestSuite, UserQuery, UserEntity.Dto>(
            "Find by id",
            (suite) -> {
              final var user =
                  suite
                      .repository
                      .findAll(QUserEntity.userEntity.role.eq(RoleValue.OWNER))
                      .iterator()
                      .next();
              return new TaskInput.Read.FindOne<>(
                  new TaskInput.AuthUser(user, null),
                  UserQuery.builder().id(new IdQuery.Single(user.getId())).build());
            },
            (input) -> {
              if (input.input().query().id() instanceof IdQuery.Single single) {
                Assertions.assertEquals(single.id(), input.result().orElseThrow().id());
              } else {
                Assertions.fail();
              }
            }),
        new Task.Read.FindPage<UserReadTestSuite, UserQuery, UserEntity.Dto>(
            "Find by page",
            (suite) ->
                new TaskInput.Read.FindPage<>(
                    TaskInput.AuthUser.builder()
                        .authId(new AuthIdValue("provider 1", "value 1"))
                        .build(),
                    Pagination.DEFAULT,
                    Sort.by(Sort.Order.desc("name")),
                    UserQuery.builder().build()),
            false,
            true,
            (input) ->
                Assertions.assertEquals(
                    input.input().pagination().size(), input.result().items().size())),
        new Task.Read.FindPage<UserReadTestSuite, UserQuery, UserEntity.Dto>(
            "Find USERS and name by page",
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
                  new Pagination(null, null, 5, 0),
                  Sort.unsorted(),
                  UserQuery.builder()
                      .role(RoleValue.USER)
                      .name(new TextQuery.FullText("a"))
                      .build());
            },
            false,
            true,
            (input) -> {
              Assertions.assertEquals(
                  input.input().pagination().size(), input.result().items().size());
              Assertions.assertAll(
                  input.result().items().stream()
                      .map(
                          entity ->
                              () -> {
                                Assertions.assertEquals(
                                    input.input().query().role(), entity.data().role());
                                if (input.input().query().name()
                                    instanceof TextQuery.FullText fullText) {
                                  Assertions.assertTrue(
                                      entity.data().name().contains(fullText.value()));
                                }
                              }));
            }));
  }

  protected UserReadTestSuite(
      ReadTaskHandler<UserReadTestSuite, UserEntity, UserQuery, UserEntity.Dto> taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.repository = userRepository;
  }
}
