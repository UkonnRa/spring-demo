package com.ukonnra.whiterabbit.endpoint.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.whiterabbit.core.domain.group.GroupEntity;
import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.endpoint.graphql.model.GraphQlPage;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.GroupReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
public class GroupReadTest extends GroupReadTestSuite {
  private static Set<String> toDto(final List<Map<String, Object>> ids) {
    return ids.stream().map(id -> id.get("id").toString()).collect(Collectors.toSet());
  }

  private static GroupEntity.Dto toDto(
      final ObjectMapper mapper, final Map<String, Object> rawObject) {
    rawObject.put("admins", toDto((List<Map<String, Object>>) rawObject.get("admins")));
    rawObject.put("members", toDto((List<Map<String, Object>>) rawObject.get("members")));
    return mapper.convertValue(rawObject, GroupEntity.Dto.class);
  }

  private static GraphQlPage<GroupEntity.Dto> toDto(
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
  protected GroupReadTest(
      HttpGraphQlTester tester,
      ObjectMapper objectMapper,
      DataGenerator dataGenerator,
      UserRepository userRepository,
      GroupRepository groupRepository,
      GroupService service) {
    super(
        new GraphQlReadTaskHandler<>(
            tester,
            service,
            objectMapper,
            new GraphQlReadTaskHandler.Params<>(
                Map.of(
                    GraphQlReadTaskHandler.TaskType.FIND_ONE,
                    "findGroup",
                    GraphQlReadTaskHandler.TaskType.FIND_ALL,
                    "findGroups"),
                response ->
                    response
                        .path("group")
                        .entity(new ParameterizedTypeReference<Optional<Map<String, Object>>>() {})
                        .get()
                        .map(e -> toDto(objectMapper, e)),
                response ->
                    toDto(
                        objectMapper,
                        response
                            .path("groups")
                            .entity(
                                new ParameterizedTypeReference<
                                    GraphQlPage<Map<String, Object>>>() {})
                            .get()))),
        dataGenerator,
        userRepository,
        groupRepository);
  }
}
