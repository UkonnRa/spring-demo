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
  protected void doHandle(final S suite, final Task.Write.HandleCommand<S, C, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    final var entity = this.service.handle(input.command());
    task.checker().accept(new CheckerInput<>(input, entity.map(AbstractEntity::toDto)));
  }

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommands<S, C, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    final var entities = this.service.handleAll(input.commands());
    task.checker().accept(new CheckerInput<>(input, entities));
  }
}
