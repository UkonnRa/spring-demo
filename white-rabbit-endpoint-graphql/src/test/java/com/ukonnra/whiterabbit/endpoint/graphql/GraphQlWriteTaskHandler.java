package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.WriteTaskHandler;
import com.ukonnra.whiterabbit.testsuite.WriteTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.Task;

public class GraphQlWriteTaskHandler<
        S extends WriteTestSuite<S, E, C, Q, D>,
        E extends AbstractEntity<D>,
        C extends Command<C>,
        Q extends Query,
        D>
    extends WriteTaskHandler<S, E, C, Q, D> {
  GraphQlWriteTaskHandler(WriteService<E, C, Q, D> service) {
    super(service);
  }

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommand<S, E, C> task) {}

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommands<S, C, D> task) {}
}
