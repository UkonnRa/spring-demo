package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.transaction.annotation.Transactional;

public abstract class WriteTestSuite<
        S extends WriteTestSuite<S, E, C, Q, D>,
        E extends AbstractEntity<D>,
        C extends Command<C>,
        Q extends Query,
        D>
    extends TestSuite {

  protected final DataGenerator dataGenerator;
  protected final WriteService<E, C, Q, D> service;

  protected WriteTestSuite(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      WriteService<E, C, Q, D> service) {
    super(userRepository);
    this.dataGenerator = dataGenerator;
    this.service = service;
  }

  private void handleTask(final Task.Write.HandleCommand<S, E, C> task) {
    final var input = task.input().apply((S) this);
    final var authUser = this.getAuthUser(input.authUser());
    final var entity = this.service.handle(authUser, input.command());
    task.checker().accept(new CheckerInput<>(input, entity));
  }

  private void handleTask(final Task.Write.HandleCommands<S, C, D> task) {
    final var input = task.input().apply((S) this);
    final var authUser = this.getAuthUser(input.authUser());
    final var entities = this.service.handleAll(authUser, input.commands());
    task.checker().accept(new CheckerInput<>(input, entities));
  }

  @Transactional
  @ParameterizedTest
  @MethodSource("generateTasks")
  public void runTasks(final Task.Write<S, TaskInput.Write, ?> task) {
    this.dataGenerator.prepareData();
    if (task instanceof Task.Write.HandleCommand handleCommand) {
      this.handleTask(handleCommand);
    } else if (task instanceof Task.Write.HandleCommands handleCommands) {
      this.handleTask(handleCommands);
    }
  }
}
