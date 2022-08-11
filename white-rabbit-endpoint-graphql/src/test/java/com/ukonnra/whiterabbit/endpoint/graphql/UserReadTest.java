package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import com.ukonnra.whiterabbit.testsuite.UserReadTestSuite;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
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
            Map.of(
                GraphQlReadTaskHandler.TaskType.FIND_ONE, "findUser",
                GraphQlReadTaskHandler.TaskType.FIND_ALL, "findUsers")),
        dataGenerator,
        userRepository);
  }
}
