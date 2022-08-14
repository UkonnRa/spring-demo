package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.ReadService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;

public abstract class ReadTaskHandler<S extends ReadTestSuite<S, E, Q, D>, E, Q extends Query, D> {
  protected final ReadService<E, Q, D> service;

  protected ReadTaskHandler(ReadService<E, Q, D> service) {
    this.service = service;
  }

  protected abstract void doHandle(final S suite, final Task.Read.FindOne<S, Q, D> task);

  protected abstract void doHandle(final S suite, final Task.Read.FindPage<S, Q, D> task);

  public final void handleTask(final S suite, final Task.Read<S, TaskInput.Read, ?> task) {
    if (task instanceof Task.Read.FindOne findOne) {
      this.doHandle(suite, findOne);
    } else if (task instanceof Task.Read.FindPage findPage) {
      this.doHandle(suite, findPage);
    }
  }
}
