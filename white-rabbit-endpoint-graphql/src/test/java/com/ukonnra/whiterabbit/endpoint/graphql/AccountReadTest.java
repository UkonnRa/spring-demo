package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.account.AccountEntity;
import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.AccountReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
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
public class AccountReadTest extends AccountReadTestSuite {
  private static AccountEntity.Dto toDto(
      final ObjectMapper mapper, final Map<String, Object> rawObject) {
    rawObject.put("journal", ((Map<String, Object>) rawObject.get("journal")).get("id"));
    return mapper.convertValue(rawObject, AccountEntity.Dto.class);
  }

  private static GraphQlPage<AccountEntity.Dto> toDto(
      final ObjectMapper mapper, final GraphQlPage<Map<String, Object>> rawPage) {
    rawPage
        .edges()
        .forEach(
            edge -> {
              final var rawObject = edge.node();
              rawObject.put("journal", ((Map<String, Object>) rawObject.get("journal")).get("id"));
            });
    return mapper.convertValue(rawPage, new TypeReference<>() {});
  }

  @Autowired
  protected AccountReadTest(
      HttpGraphQlTester tester,
      ObjectMapper objectMapper,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      AccountRepository accountRepository,
      AccountService service,
      JournalRepository journalRepository) {
    super(
        new GraphQlReadTaskHandler<>(
            tester,
            service,
            objectMapper,
            new GraphQlReadTaskHandler.Params<>(
                Map.of(
                    GraphQlReadTaskHandler.TaskType.FIND_ONE,
                    "findAccount",
                    GraphQlReadTaskHandler.TaskType.FIND_ALL,
                    "findAccounts"),
                response ->
                    response
                        .path("account")
                        .entity(new ParameterizedTypeReference<Optional<Map<String, Object>>>() {})
                        .get()
                        .map(e -> toDto(objectMapper, e)),
                response ->
                    toDto(
                        objectMapper,
                        response
                            .path("accounts")
                            .entity(
                                new ParameterizedTypeReference<
                                    GraphQlPage<Map<String, Object>>>() {})
                            .get()))),
        userRepository,
        dataGenerator,
        accountRepository,
        journalRepository);
  }
}
