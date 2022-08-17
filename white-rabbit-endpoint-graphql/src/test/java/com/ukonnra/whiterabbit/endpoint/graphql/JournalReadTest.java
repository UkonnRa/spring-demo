package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.journal.JournalEntity;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.JournalReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
public class JournalReadTest extends JournalReadTestSuite {
  private static List<AccessItemValue> toDto(final List<Map<String, Object>> ids) {
    return ids.stream()
        .map(
            id -> {
              final var type =
                  switch (id.get("__typename").toString()) {
                    case "User" -> AccessItemValue.Type.USER;
                    case "Group" -> AccessItemValue.Type.GROUP;
                    default -> throw new AssertionError("Invalid type");
                  };
              return new AccessItemValue(UUID.fromString(id.get("id").toString()), type);
            })
        .toList();
  }

  private static JournalEntity.Dto toDto(
      final ObjectMapper mapper, final Map<String, Object> rawObject) {
    rawObject.put("admins", toDto((List<Map<String, Object>>) rawObject.get("admins")));
    rawObject.put("members", toDto((List<Map<String, Object>>) rawObject.get("members")));
    return mapper.convertValue(rawObject, JournalEntity.Dto.class);
  }

  private static GraphQlPage<JournalEntity.Dto> toDto(
      final ObjectMapper mapper, final GraphQlPage<Map<String, Object>> rawPage) {
    rawPage
        .edges()
        .forEach(
            edge -> {
              final var rawObject = edge.node();
              rawObject.put("admins", toDto((List<Map<String, Object>>) rawObject.get("admins")));
              rawObject.put("members", toDto((List<Map<String, Object>>) rawObject.get("members")));
            });
    return mapper.convertValue(rawPage, new TypeReference<>() {});
  }

  @Autowired
  protected JournalReadTest(
      HttpGraphQlTester tester,
      ObjectMapper objectMapper,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      JournalRepository journalRepository,
      JournalService service) {
    super(
        new GraphQlReadTaskHandler<>(
            tester,
            service,
            objectMapper,
            new GraphQlReadTaskHandler.Params<>(
                Map.of(
                    GraphQlReadTaskHandler.TaskType.FIND_ONE,
                    "findJournal",
                    GraphQlReadTaskHandler.TaskType.FIND_ALL,
                    "findJournals"),
                response ->
                    response
                        .path("journal")
                        .entity(new ParameterizedTypeReference<Optional<Map<String, Object>>>() {})
                        .get()
                        .map(e -> toDto(objectMapper, e)),
                response ->
                    toDto(
                        objectMapper,
                        response
                            .path("journals")
                            .entity(
                                new ParameterizedTypeReference<
                                    GraphQlPage<Map<String, Object>>>() {})
                            .get()))),
        dataGenerator,
        userRepository,
        journalRepository);
  }
}
