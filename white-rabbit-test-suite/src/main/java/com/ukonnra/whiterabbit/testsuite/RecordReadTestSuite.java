package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.record.QRecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordQuery;
import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
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
public abstract class RecordReadTestSuite
    extends ReadTestSuite<RecordReadTestSuite, RecordEntity, RecordQuery> {
  private final RecordRepository repository;

  static Stream<Task.Read<RecordReadTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Read.FindOne<RecordReadTestSuite, RecordEntity, RecordQuery>(
            "Find by id",
            (suite) -> {
              final var journalSubBuilder = QRecordEntity.recordEntity.journal;

              final var record =
                  suite
                      .repository
                      .findAll(
                          journalSubBuilder
                              .admins
                              .any()
                              .itemType
                              .eq(AccessItemValue.Type.USER)
                              .and(journalSubBuilder.archived.isFalse()))
                      .iterator()
                      .next();
              final var user =
                  record.getJournal().getAdmins().stream()
                      .filter(item -> item.getItemType() == AccessItemValue.Type.USER)
                      .findFirst()
                      .flatMap(item -> suite.userRepository.findById(item.getId()))
                      .orElseThrow();
              return new TaskInput.Read.FindOne<>(
                  new TaskInput.AuthUser(user, null),
                  RecordQuery.builder()
                      .id(new IdQuery.Single(record.getId()))
                      .journal(record.getJournal().getId())
                      .build());
            },
            (input) -> {
              final var query = input.input().query();
              final var result = input.result().orElseThrow();
              Assertions.assertEquals(query.journal(), result.getJournal().getId());

              if (input.input().query().id() instanceof IdQuery.Single single) {
                Assertions.assertEquals(single.id(), result.getId());
              } else {
                Assertions.fail();
              }
            }),
        new Task.Read.FindPage<RecordReadTestSuite, RecordEntity, RecordQuery>(
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
                  RecordQuery.builder().build());
            },
            false,
            true,
            (input) ->
                Assertions.assertEquals(
                    input.input().pagination().size(), input.result().items().size())));
  }

  protected RecordReadTestSuite(
      ReadTaskHandler<RecordReadTestSuite, RecordEntity, RecordQuery> taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      RecordRepository recordRepository) {
    super(taskHandler, dataGenerator, userRepository);
    this.repository = recordRepository;
  }
}
