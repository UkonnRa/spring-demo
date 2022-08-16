package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;

public abstract class WriteTaskHandler<
    S extends WriteTestSuite<S, E, C, Q, D>,
    E extends AbstractEntity<D>,
    C extends Command<C>,
    Q extends Query,
    D> {
  protected final WriteService<E, C, Q, D> service;

  protected WriteTaskHandler(WriteService<E, C, Q, D> service) {
    this.service = service;
  }

  protected abstract void doHandle(final S suite, final Task.Write.HandleCommand<S, C, D> task);

  protected abstract void doHandle(final S suite, final Task.Write.HandleCommands<S, C, D> task);

  public final void handleTask(final S suite, final Task.Write<S, TaskInput.Write, ?> task) {
    if (task instanceof Task.Write.HandleCommand handleCommand) {
      this.doHandle(suite, handleCommand);
    } else if (task instanceof Task.Write.HandleCommands handleCommands) {
      this.doHandle(suite, handleCommands);
    }
  }
}
