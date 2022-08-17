package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.record.RecordEntity;
import com.ukonnra.whiterabbit.core.domain.record.RecordItemValue;
import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.RecordReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
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
@AutoConfigureWebTestClient(timeout = "36000")
public class RecordReadTest extends RecordReadTestSuite {
  private static RecordItemValue.Dto toDto(final Map<String, Object> rawItem) {
    return new RecordItemValue.Dto(
        UUID.fromString(((Map<String, Object>) rawItem.get("account")).get("id").toString()),
        Optional.ofNullable(rawItem.get("amount"))
            .map(d -> BigDecimal.valueOf((double) d))
            .orElse(null),
        Optional.ofNullable(rawItem.get("price"))
            .map(d -> BigDecimal.valueOf((double) d))
            .orElse(null));
  }

  private static RecordEntity.Dto toDto(
      final ObjectMapper mapper, final Map<String, Object> rawObject) {
    rawObject.put("journal", ((Map<String, Object>) rawObject.get("journal")).get("id"));

    final var items =
        ((List<Map<String, Object>>) rawObject.get("items"))
            .stream().map(RecordReadTest::toDto).collect(Collectors.toSet());
    rawObject.put("items", items);
    return mapper.convertValue(rawObject, RecordEntity.Dto.class);
  }

  private static GraphQlPage<RecordEntity.Dto> toDto(
      final ObjectMapper mapper, final GraphQlPage<Map<String, Object>> rawPage) {
    rawPage
        .edges()
        .forEach(
            edge -> {
              final var rawObject = edge.node();
              rawObject.put("journal", ((Map<String, Object>) rawObject.get("journal")).get("id"));

              final var items =
                  ((List<Map<String, Object>>) rawObject.get("items"))
                      .stream().map(RecordReadTest::toDto).collect(Collectors.toSet());
              rawObject.put("items", items);
            });
    return mapper.convertValue(rawPage, new TypeReference<>() {});
  }

  @Autowired
  protected RecordReadTest(
      HttpGraphQlTester tester,
      ObjectMapper objectMapper,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      RecordRepository recordRepository,
      RecordService service) {
    super(
        new GraphQlReadTaskHandler<>(
            tester,
            service,
            objectMapper,
            new GraphQlReadTaskHandler.Params<>(
                Map.of(
                    GraphQlReadTaskHandler.TaskType.FIND_ONE,
                    "findRecord",
                    GraphQlReadTaskHandler.TaskType.FIND_ALL,
                    "findRecords"),
                response ->
                    response
                        .path("record")
                        .entity(new ParameterizedTypeReference<Optional<Map<String, Object>>>() {})
                        .get()
                        .map(e -> toDto(objectMapper, e)),
                response ->
                    toDto(
                        objectMapper,
                        response
                            .path("records")
                            .entity(
                                new ParameterizedTypeReference<
                                    GraphQlPage<Map<String, Object>>>() {})
                            .get()))),
        dataGenerator,
        userRepository,
        recordRepository);
  }
}
