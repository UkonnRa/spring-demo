package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.ReadTaskHandler;
import com.ukonnra.whiterabbit.testsuite.ReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import org.junit.jupiter.api.Assertions;

public class CoreReadTaskHandler<S extends ReadTestSuite<S, E, Q>, E, Q extends Query>
    extends ReadTaskHandler<S, E, Q> {
  CoreReadTaskHandler(ReadService<E, Q> service) {
    super(service);
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindOne<S, E, Q> task) {
    final var input = task.input().apply(suite);
    final var authUser = suite.getAuthUser(input.authUser());
    final var entity = this.service.findOne(authUser, input.query());
    task.checker().accept(new CheckerInput<>(input, entity));
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindPage<S, E, Q> task) {
    final var input = task.input().apply(suite);
    final var authUser = suite.getAuthUser(input.authUser());
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
}
