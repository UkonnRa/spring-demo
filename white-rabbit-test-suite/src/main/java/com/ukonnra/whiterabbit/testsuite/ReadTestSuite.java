package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.ReadService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.transaction.annotation.Transactional;

public abstract class ReadTestSuite<S extends ReadTestSuite<S, E, Q>, E, Q extends Query>
    extends TestSuite {
  private final DataGenerator dataGenerator;
  protected final ReadService<E, Q> service;

  protected ReadTestSuite(
      DataGenerator dataGenerator, UserRepository userRepository, ReadService<E, Q> service) {
    super(userRepository);
    this.dataGenerator = dataGenerator;
    this.service = service;
  }

  private void handleTask(final Task.Read.FindOne<S, E, Q> task) {
    final var input = task.input().apply((S) this);
    final var authUser = this.getAuthUser(input.authUser());
    final var entity = this.service.findOne(authUser, input.query());
    task.checker().accept(new CheckerInput<>(input, entity));
  }

  private void handleTask(final Task.Read.FindPage<S, E, Q> task) {
    final var input = task.input().apply((S) this);
    final var authUser = this.getAuthUser(input.authUser());
    final var page =
        this.service.findPage(authUser, input.pagination(), input.sort(), input.query());
    task.checker().accept(new CheckerInput<>(input, page));

    if (task.expectNextPage() != null) {
      if (task.expectNextPage()) {
        Assertions.assertTrue(page.info().hasNextPage());

        final var nextPage =
            this.service.findPage(
                authUser,
                new Pagination(page.info().endCursor(), null, input.pagination().size(), 0),
                input.sort(),
                input.query());

        Assertions.assertTrue(nextPage.info().hasPreviousPage());
        Assertions.assertFalse(nextPage.items().isEmpty());
        final var nextPagePrevious =
            this.service.findPage(
                authUser,
                new Pagination(null, nextPage.info().startCursor(), input.pagination().size(), 0),
                input.sort(),
                input.query());
        Assertions.assertEquals(page, nextPagePrevious);
      } else {
        Assertions.assertFalse(page.info().hasNextPage());
      }
    }
  }

  @Transactional
  @ParameterizedTest
  @MethodSource("generateTasks")
  public void runTasks(final Task.Read<S, TaskInput.Read, ?> task) {
    this.dataGenerator.prepareData();
    if (task instanceof Task.Read.FindOne findOne) {
      this.handleTask(findOne);
    } else if (task instanceof Task.Read.FindPage findPage) {
      this.handleTask(findPage);
    }
  }
}
