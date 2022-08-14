package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import com.ukonnra.whiterabbit.testsuite.UserReadTestSuite;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
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
public class UserReadTest extends UserReadTestSuite {
  @Autowired
  protected UserReadTest(
      HttpGraphQlTester tester,
      ObjectMapper objectMapper,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      UserService service) {
    super(
        new GraphQlReadTaskHandler<>(
            tester,
            service,
            objectMapper,
            new GraphQlParams<>(
                Map.of(
                    GraphQlParams.TaskType.FIND_ONE,
                    "findUser",
                    GraphQlParams.TaskType.FIND_ALL,
                    "findUsers"),
                response ->
                    response
                        .path("user")
                        .entity(new ParameterizedTypeReference<Optional<UserEntity.Dto>>() {})
                        .get(),
                response ->
                    response
                        .path("users")
                        .entity(new ParameterizedTypeReference<GraphQlPage<UserEntity.Dto>>() {})
                        .get())),
        dataGenerator,
        userRepository);
  }
}
