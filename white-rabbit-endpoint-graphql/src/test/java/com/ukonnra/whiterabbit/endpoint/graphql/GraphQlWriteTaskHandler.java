package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.AbstractEntity;
import com.ukonnra.whiterabbit.core.Command;
import com.ukonnra.whiterabbit.core.WriteService;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.testsuite.WriteTaskHandler;
import com.ukonnra.whiterabbit.testsuite.WriteTestSuite;
import com.ukonnra.whiterabbit.testsuite.task.CheckerInput;
import com.ukonnra.whiterabbit.testsuite.task.Task;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.GraphQlTester;

public class GraphQlWriteTaskHandler<
        S extends WriteTestSuite<S, E, C, Q, D>,
        E extends AbstractEntity<D>,
        C extends Command<C>,
        Q extends Query,
        D>
    extends WriteTaskHandler<S, E, C, Q, D> {
  private final GraphQlTester tester;
  private final ObjectMapper objectMapper;
  private final Map<String, CommandHandler<C, D>> commandHandlers;
  private final CommandsParam<D> commandsParam;

  GraphQlWriteTaskHandler(
      WriteService<E, C, Q, D> service,
      GraphQlTester tester,
      ObjectMapper objectMapper,
      Map<String, CommandHandler<C, D>> commandHandlers,
      CommandsParam<D> commandsParam) {
    super(service);
    this.tester = tester;
    this.objectMapper = objectMapper;
    this.commandHandlers = commandHandlers;
    this.commandsParam = commandsParam;
  }

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommand<S, C, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    final var command = input.command();
    final var handler = this.commandHandlers.get(command.type());
    final var response = handler.parseCommand(command).execute();
    response.errors().verify();
    task.checker().accept(new CheckerInput<>(input, handler.parseResponse(response)));
  }

  @Override
  protected void doHandle(final S suite, final Task.Write.HandleCommands<S, C, D> task) {
    final var input = task.input().apply(suite);
    suite.setAuthentication(input.authUser());
    final var serializedCommands =
        input.commands().stream()
            .map(
                c -> this.objectMapper.convertValue(c, new TypeReference<Map<String, Object>>() {}))
            .toList();
    final var response =
        this.tester
            .documentName(this.commandsParam.graphQlName())
            .variable("commands", serializedCommands)
            .execute();
    response.errors().verify();
    final var result =
        response
            .path(this.commandsParam.graphQlName())
            .entity(this.commandsParam.typeReference())
            .get();
    task.checker().accept(new CheckerInput<>(input, result));
  }

  public interface CommandHandler<C extends Command<C>, D> {
    GraphQlTester.Request<?> parseCommand(final C command);

    Optional<D> parseResponse(final GraphQlTester.Response response);
  }

  public record CommandsParam<D>(
      String graphQlName, ParameterizedTypeReference<List<Optional<D>>> typeReference) {}
}
