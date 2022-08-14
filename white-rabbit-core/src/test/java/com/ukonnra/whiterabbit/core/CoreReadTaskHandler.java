package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.query.Page;
import com.ukonnra.whiterabbit.core.query.Pagination;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.ReadTaskHandler;
import com.ukonnra.whiterabbit.testsuite.ReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import org.junit.jupiter.api.Assertions;

public class CoreReadTaskHandler<S extends ReadTestSuite<S, E, Q, D>, E, Q extends Query, D>
    extends ReadTaskHandler<S, E, Q, D> {
  CoreReadTaskHandler(ReadService<E, Q, D> service) {
    super(service);
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindOne<S, Q, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    final var entity = this.service.findOne(input.query());
    task.checker().accept(new CheckerInput<>(input, entity.map(this.service::toDto)));
  }

  @Override
  protected void doHandle(final S suite, final Task.Read.FindPage<S, Q, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    final var page = this.service.findPage(input.pagination(), input.sort(), input.query());
    final var items =
        page.items().stream()
            .map(item -> new Page.Item<>(item.cursor(), this.service.toDto(item.data())))
            .toList();
    task.checker().accept(new CheckerInput<>(input, new Page<>(page.info(), items)));

    if (task.expectNextPage() != null) {
      if (task.expectNextPage()) {
        Assertions.assertTrue(page.info().hasNextPage());

        final var nextPage =
            this.service.findPage(
                new Pagination(page.info().endCursor(), null, input.pagination().size(), 0),
                input.sort(),
                input.query());

        Assertions.assertTrue(nextPage.info().hasPreviousPage());
        Assertions.assertFalse(nextPage.items().isEmpty());
        final var nextPagePrevious =
            this.service.findPage(
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
