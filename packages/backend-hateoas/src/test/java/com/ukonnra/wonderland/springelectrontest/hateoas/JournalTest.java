package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.wonderland.springelectrontest.entity.JournalCommand;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Slf4j
@WebMvcTest
@AutoConfigureJson
@AutoConfigureDataJpa
@ContextConfiguration(classes = {HateoasConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JournalTest {
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;

  @Autowired
  public JournalTest(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  @Test
  void testServer() throws Exception {
    {
      final var request =
          MockMvcRequestBuilders.get("/journals").contentType(MediaType.APPLICATION_JSON);
      final var pathValues = MockMvcResultMatchers.jsonPath("$.values");
      mockMvc
          .perform(request)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
          .andExpectAll(pathValues.isArray(), pathValues.isEmpty());
    }

    {
      final var request =
          MockMvcRequestBuilders.post("/init").contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(request)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().bytes(new byte[0]));
    }

    {
      final var request =
          MockMvcRequestBuilders.get("/journals").contentType(MediaType.APPLICATION_JSON);
      final var pathValues = MockMvcResultMatchers.jsonPath("$.values");
      mockMvc
          .perform(request)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
          .andExpectAll(pathValues.isNotEmpty());
    }
  }

  @Test
  void testServer1() throws Exception {
    {
      final var request =
          MockMvcRequestBuilders.get("/journals").contentType(MediaType.APPLICATION_JSON);
      final var pathValues = MockMvcResultMatchers.jsonPath("$.values");
      mockMvc
          .perform(request)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
          .andExpectAll(pathValues.isArray(), pathValues.isEmpty());
    }

    {
      final var request =
          MockMvcRequestBuilders.post("/init").contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(request)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().bytes(new byte[0]));
    }

    final var request =
        MockMvcRequestBuilders.get("/journals").contentType(MediaType.APPLICATION_JSON);
    final var pathValues = MockMvcResultMatchers.jsonPath("$.values");
    final var results =
        mockMvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpectAll(pathValues.isNotEmpty())
            .andReturn();

    final var jsonRoot = this.objectMapper.readTree(results.getResponse().getContentAsString());
    final var firstNode = jsonRoot.at("/values/0");
    Assertions.assertTrue(firstNode.isObject());
    Assertions.assertFalse(firstNode.isEmpty());

    final var request2 =
        MockMvcRequestBuilders.get("/journals/{id}", firstNode.at("/id").asText())
            .contentType(MediaType.APPLICATION_JSON);

    mockMvc
        .perform(request2)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpectAll(
            MockMvcResultMatchers.jsonPath("$.id").value(firstNode.at("/id").asText()),
            MockMvcResultMatchers.jsonPath("$.name").value(firstNode.at("/name").asText()),
            MockMvcResultMatchers.jsonPath("$.description")
                .value(firstNode.at("/description").asText()),
            MockMvcResultMatchers.jsonPath("$.unit").value(firstNode.at("/unit").asText()),
            MockMvcResultMatchers.jsonPath(
                "$.tags",
                Matchers.containsInAnyOrder(
                    this.objectMapper.convertValue(firstNode.at("/tags"), String[].class))));
  }

  @Test
  void testError() throws Exception {

    final var longName =
        IntStream.range(0, 50).mapToObj(i -> "Name " + i).collect(Collectors.joining(";"));
    final var longTags =
        IntStream.range(0, 20).mapToObj(i -> "Tag " + i).collect(Collectors.toSet());
    final var command = new JournalCommand.Create(null, longName, "Desc 1", "Unit 1", longTags);

    final var request =
        MockMvcRequestBuilders.post("/journals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command));

    final var expectedErrors =
        this.objectMapper.readValue(
            """
{
    "type": "urn:wonderland:white-rabbit:errors:errors",
    "title": "Errors Found",
    "status": 400,
    "detail": "Multiple errors found",
    "instance": "/journals",
    "errors": [
        {
            "type": "urn:wonderland:white-rabbit:errors:fields-out-of-range",
            "title": "FieldOutOfRangeError",
            "status": 400,
            "detail": "journals[tags] should between 0 and 15",
            "entityType": "journals",
            "max": 15,
            "min": 0,
            "field": "tags"
        },
        {
            "type": "urn:wonderland:white-rabbit:errors:fields-out-of-range",
            "title": "FieldOutOfRangeError",
            "status": 400,
            "detail": "journals[name] should between 2 and 127",
            "entityType": "journals",
            "max": 127,
            "min": 2,
            "field": "name"
        }
    ]
}
""",
            Map.class);

    final var results =
        mockMvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
            .andReturn();
    final var returnedBody =
        this.objectMapper.readValue(results.getResponse().getContentAsString(), Map.class);
    Assertions.assertEquals(expectedErrors, returnedBody);
  }
}
