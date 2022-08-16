package com.ukonnra.whiterabbit.testsuite.task;

import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.query.Page;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.TestSuite;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.lang.Nullable;

public sealed interface Task<S extends TestSuite, I extends TaskInput, R>
    permits Task.Read, Task.Write {

  String name();

  Function<S, I> input();

  @Nullable
  Consumer<CheckerInput<I, R>> checker();

  sealed interface Read<S extends TestSuite, I extends TaskInput.Read, R> extends Task<S, I, R>
      permits Read.FindOne, Read.FindPage {
    record FindOne<S extends TestSuite, Q extends Query, D>(
        String name,
        Function<S, TaskInput.Read.FindOne<Q>> input,
        Consumer<CheckerInput<TaskInput.Read.FindOne<Q>, Optional<D>>> checker)
        implements Read<S, TaskInput.Read.FindOne<Q>, Optional<D>> {}

    record FindPage<S extends TestSuite, Q extends Query, D>(
        String name,
        Function<S, TaskInput.Read.FindPage<Q>> input,
        @Nullable Boolean expectPreviousPage,
        @Nullable Boolean expectNextPage,
        Consumer<CheckerInput<TaskInput.Read.FindPage<Q>, Page<D>>> checker)
        implements Read<S, TaskInput.Read.FindPage<Q>, Page<D>> {}
  }

  sealed interface Write<S extends TestSuite, I extends TaskInput.Write, R> extends Task<S, I, R>
      permits Write.HandleCommand, Write.HandleCommands {
    record HandleCommand<S extends TestSuite, C extends Command, D>(
        String name,
        Function<S, TaskInput.Write.HandleCommand<C>> input,
        Consumer<CheckerInput<TaskInput.Write.HandleCommand<C>, Optional<D>>> checker)
        implements Write<S, TaskInput.Write.HandleCommand<C>, Optional<D>> {}

    record HandleCommands<S extends TestSuite, C extends Command, D>(
        String name,
        Function<S, TaskInput.Write.HandleCommands<C>> input,
        Consumer<CheckerInput<TaskInput.Write.HandleCommands<C>, List<Optional<D>>>> checker)
        implements Write<S, TaskInput.Write.HandleCommands<C>, List<Optional<D>>> {}
  }
}
