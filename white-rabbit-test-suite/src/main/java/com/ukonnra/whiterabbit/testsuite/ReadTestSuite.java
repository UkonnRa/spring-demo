package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import com.ukonnra.whiterabbit.testsuite.task.TaskInput;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class ReadTestSuite<S extends ReadTestSuite<S, E, Q, D>, E, Q extends Query, D>
    extends TestSuite {
  private final ReadTaskHandler<S, E, Q, D> taskHandler;
  private final DataGenerator dataGenerator;

  protected ReadTestSuite(
      ReadTaskHandler<S, E, Q, D> taskHandler,
      DataGenerator dataGenerator,
      UserRepository userRepository) {
    super(userRepository);
    this.taskHandler = taskHandler;
    this.dataGenerator = dataGenerator;
  }

  @ParameterizedTest
  @MethodSource("generateTasks")
  public void runTasks(final Task.Read<S, TaskInput.Read, ?> task) {
    this.dataGenerator.prepareData();
    this.taskHandler.handleTask((S) this, task);
    this.dataGenerator.clear();
  }
}
