package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.WriteTaskHandler;
import com.ukonnra.whiterabbit.testsuite.WriteTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;

public class CoreWriteTaskHandler<
        S extends WriteTestSuite<S, E, C, Q, D>,
        E extends AbstractEntity<D>,
        C extends Command<C>,
        Q extends Query,
        D>
    extends WriteTaskHandler<S, E, C, Q, D> {
  CoreWriteTaskHandler(WriteService<E, C, Q, D> service) {
    super(service);
  }

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommand<S, E, C> task) {
    final var input = task.input().apply(suite);
    final var authUser = suite.getAuthUser(input.authUser());
    final var entity = this.service.handle(authUser, input.command());
    task.checker().accept(new CheckerInput<>(input, entity));
  }

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommands<S, C, D> task) {
    final var input = task.input().apply(suite);
    final var authUser = suite.getAuthUser(input.authUser());
    final var entities = this.service.handleAll(authUser, input.commands());
    task.checker().accept(new CheckerInput<>(input, entities));
  }
}
