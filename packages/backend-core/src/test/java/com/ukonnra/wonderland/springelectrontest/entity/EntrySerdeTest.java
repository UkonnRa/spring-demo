package com.ukonnra.wonderland.springelectrontest.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ukonnra.wonderland.springelectrontest.configuration.JsonConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;

@JsonTest
@Slf4j
@ContextConfiguration(classes = JsonConfiguration.class)
class EntrySerdeTest {
  private final ObjectMapper objectMapper;

  @Autowired
  public EntrySerdeTest(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Test
  void testEntryStateSerde() throws JsonProcessingException {
    final var dtos =
        List.of(
            new EntryDto.Record(
                UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936ba"),
                Instant.ofEpochSecond(1703666880),
                1,
                "Entry 1",
                "Desc 1",
                LocalDate.of(2021, 1, 1),
                Set.of("Tag 1", "Tag 2"),
                Set.of(
                    new EntryDto.Item(
                        UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bc"),
                        BigDecimal.TEN,
                        BigDecimal.ONE),
                    new EntryDto.Item(
                        UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bd"),
                        BigDecimal.ONE,
                        BigDecimal.TWO)),
                new EntryState.Invalid(BigDecimal.TEN, BigDecimal.TWO)),
            new EntryDto.Check(
                UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bb"),
                Instant.ofEpochSecond(1703666882),
                2,
                "Entry 2",
                "Desc 2",
                LocalDate.of(2021, 2, 1),
                Set.of("Tag 2", "Tag 4"),
                Set.of(
                    new EntryDto.Item(
                        UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bc"),
                        BigDecimal.valueOf(456.432),
                        BigDecimal.valueOf(123)),
                    new EntryDto.Item(
                        UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bd"),
                        BigDecimal.TWO,
                        BigDecimal.TEN)),
                Map.of(
                    UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bc"),
                    new EntryState.Valid(BigDecimal.valueOf(456.432)),
                    UUID.fromString("2f611717-2716-4d3b-a01d-62dff21936bd"),
                    new EntryState.Invalid(BigDecimal.TWO, BigDecimal.TEN))));

    for (final var dto : dtos) {
      final var json = this.objectMapper.writeValueAsString(dto);
      log.info("Json: {}", json);

      final var converted = this.objectMapper.readValue(json, EntryDto.class);
      log.info("Converted: {}", converted);
      Assertions.assertEquals(dto, converted);
    }
  }
}
