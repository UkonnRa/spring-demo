package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.Query;
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

  protected final WriteTaskHandler<S, E, C, Q, D> taskHandler;
  protected final DataGenerator dataGenerator;

  protected WriteTestSuite(
      WriteTaskHandler<S, E, C, Q, D> taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository) {
    super(userRepository);
    this.taskHandler = taskHandler;
    this.dataGenerator = dataGenerator;
  }

  @Transactional
  @ParameterizedTest
  @MethodSource("generateTasks")
  public void runTasks(final Task.Write<S, TaskInput.Write, ?> task) {
    this.dataGenerator.prepareData();
    this.taskHandler.handleTask((S) this, task);
    this.dataGenerator.clear();
  }
}
