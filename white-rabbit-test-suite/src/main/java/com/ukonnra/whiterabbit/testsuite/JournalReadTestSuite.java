package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalQuery;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.journal.QJournalEntity;
import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
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
public abstract class JournalReadTestSuite
    extends ReadTestSuite<JournalReadTestSuite, JournalEntity, JournalQuery> {
  private final JournalRepository repository;

  static Stream<Task.Read<JournalReadTestSuite, ?, ?>> generateTasks() {
    return Stream.of(
        new Task.Read.FindOne<JournalReadTestSuite, JournalEntity, JournalQuery>(
            "Find by id",
            (suite) -> {
              final var journalBuilder = QJournalEntity.journalEntity;
              final var journal =
                  suite
                      .repository
                      .findAll(
                          journalBuilder
                              .archived
                              .isFalse()
                              .and(
                                  journalBuilder
                                      .admins
                                      .any()
                                      .itemType
                                      .eq(AccessItemValue.Type.USER)))
                      .iterator()
                      .next();
              final var user =
                  journal.getAdmins().stream()
                      .filter(item -> item.getItemType() == AccessItemValue.Type.USER)
                      .findFirst()
                      .flatMap(item -> suite.userRepository.findById(item.getId()))
                      .orElseThrow();
              return new TaskInput.Read.FindOne<>(
                  new TaskInput.AuthUser(user, null),
                  JournalQuery.builder()
                      .id(new IdQuery.Single(journal.getId()))
                      .admin(new AccessItemValue(user))
                      .build());
            },
            (input) -> {
              final var query = input.input().query();
              Assertions.assertTrue(
                  input.result().orElseThrow().getAdmins().contains(query.admin()));

              if (input.input().query().id() instanceof IdQuery.Single single) {
                Assertions.assertEquals(single.id(), input.result().orElseThrow().getId());
              } else {
                Assertions.fail();
              }
            }),
        new Task.Read.FindPage<JournalReadTestSuite, JournalEntity, JournalQuery>(
            "Find by page",
            (suite) ->
                new TaskInput.Read.FindPage<>(
                    TaskInput.AuthUser.builder()
                        .authId(new AuthIdValue("provider 1", "value 1"))
                        .build(),
                    Pagination.DEFAULT,
                    Sort.by(Sort.Order.desc("name")),
                    JournalQuery.builder().includeArchived(true).build()),
            false,
            true,
            (input) ->
                Assertions.assertEquals(
                    input.input().pagination().size(), input.result().items().size())));
  }

  protected JournalReadTestSuite(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      JournalRepository journalRepository,
      JournalService service) {
    super(dataGenerator, userRepository, service);
    this.repository = journalRepository;
  }
}
