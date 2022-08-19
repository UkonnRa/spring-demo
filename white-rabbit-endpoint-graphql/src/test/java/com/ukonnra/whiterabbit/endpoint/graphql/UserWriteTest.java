package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.user.UserCommand;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import com.ukonnra.whiterabbit.testsuite.UserWriteTestSuite;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureHttpGraphQlTester
@ContextConfiguration(
    classes = {TestSuiteApplicationConfiguration.class, GraphQlApplicationConfiguration.class})
@Transactional
public class UserWriteTest extends UserWriteTestSuite {
  private static final String GRAPHQL_CREATE = "createUser";
  private static final String GRAPHQL_UPDATE = "updateUser";
  private static final String GRAPHQL_DELETE = "deleteUser";
  private static final String GRAPHQL_HANDLE_ALL = "handleUserCommands";

  private static Optional<UserEntity.Dto> doParseResponse(
      final GraphQlTester.Response response, final String path) {
    return response
        .path(path)
        .entity(new ParameterizedTypeReference<Optional<UserEntity.Dto>>() {})
        .get();
  }

  @Autowired
  protected UserWriteTest(
      HttpGraphQlTester tester,
      ObjectMapper objectMapper,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      UserService service) {
    super(
        new GraphQlWriteTaskHandler<>(
            service,
            tester,
            objectMapper,
            Map.of(
                UserCommand.TYPE_CREATE,
                new GraphQlWriteTaskHandler.CommandHandler<UserCommand, UserEntity.Dto>() {
                  @Override
                  public GraphQlTester.Request<?> parseCommand(UserCommand command) {
                    final var create = (UserCommand.Create) command;
                    return tester
                        .documentName(GRAPHQL_CREATE)
                        .variable("targetId", create.targetId())
                        .variable("name", create.name())
                        .variable("role", create.role())
                        .variable("authIds", create.authIds());
                  }

                  @Override
                  public Optional<UserEntity.Dto> parseResponse(GraphQlTester.Response response) {
                    return doParseResponse(response, GRAPHQL_CREATE);
                  }
                },
                UserCommand.TYPE_UPDATE,
                new GraphQlWriteTaskHandler.CommandHandler<UserCommand, UserEntity.Dto>() {
                  @Override
                  public GraphQlTester.Request<?> parseCommand(UserCommand command) {
                    final var update = (UserCommand.Update) command;
                    return tester
                        .documentName(GRAPHQL_UPDATE)
                        .variable("targetId", update.targetId())
                        .variable("name", update.name())
                        .variable("role", update.role())
                        .variable("authIds", update.authIds());
                  }

                  @Override
                  public Optional<UserEntity.Dto> parseResponse(GraphQlTester.Response response) {
                    return doParseResponse(response, GRAPHQL_UPDATE);
                  }
                },
                UserCommand.TYPE_DELETE,
                new GraphQlWriteTaskHandler.CommandHandler<UserCommand, UserEntity.Dto>() {
                  @Override
                  public GraphQlTester.Request<?> parseCommand(UserCommand command) {
                    final var delete = (UserCommand.Delete) command;
                    return tester
                        .documentName(GRAPHQL_DELETE)
                        .variable("targetId", delete.targetId());
                  }

                  @Override
                  public Optional<UserEntity.Dto> parseResponse(GraphQlTester.Response response) {
                    response.path(GRAPHQL_DELETE).pathDoesNotExist();
                    return Optional.empty();
                  }
                }),
            new GraphQlWriteTaskHandler.CommandsParam<>(
                GRAPHQL_HANDLE_ALL, new ParameterizedTypeReference<>() {})),
        dataGenerator,
        userRepository);
  }
}
