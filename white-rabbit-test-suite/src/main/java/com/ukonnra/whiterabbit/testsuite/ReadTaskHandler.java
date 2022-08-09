package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.ReadService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;

public abstract class ReadTaskHandler<S extends ReadTestSuite<S, E, Q>, E, Q extends Query> {
  protected final ReadService<E, Q> service;

  protected ReadTaskHandler(ReadService<E, Q> service) {
    this.service = service;
  }

  protected abstract void doHandle(final S suite, final Task.Read.FindOne<S, E, Q> task);

  protected abstract void doHandle(final S suite, final Task.Read.FindPage<S, E, Q> task);

  public final void handleTask(final S suite, final Task.Read<S, TaskInput.Read, ?> task) {
    if (task instanceof Task.Read.FindOne findOne) {
      this.doHandle(suite, findOne);
    } else if (task instanceof Task.Read.FindPage findPage) {
      this.doHandle(suite, findPage);
    }
  }
}
