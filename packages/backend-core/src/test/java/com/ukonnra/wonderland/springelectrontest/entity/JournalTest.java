package com.ukonnra.wonderland.springelectrontest.entity;

import com.ukonnra.wonderland.springelectrontest.CoreConfiguration;
import com.ukonnra.wonderland.springelectrontest.error.EntityAlreadyExistsError;
import com.ukonnra.wonderland.springelectrontest.error.Errors;
import com.ukonnra.wonderland.springelectrontest.error.FieldOutOfRangeError;
import com.ukonnra.wonderland.springelectrontest.service.JournalService;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureJson
@Slf4j
@ContextConfiguration(classes = CoreConfiguration.class)
public class JournalTest {
  private final JournalService journalService;

  @Autowired
  public JournalTest(JournalService journalService) {
    this.journalService = journalService;
  }

  @Test
  void testSuccess() {
    final var journalCommand =
        new JournalCommand.Create(null, "New Journal", "Desc", "UNIT", Set.of("TAG 1", "TAG 2"));

    final var journals = this.journalService.handleCommand(journalCommand);
    Assertions.assertEquals(1, journals.size());
    final var journal = journals.getFirst();
    Assertions.assertEquals(journalCommand.name(), journal.getName());
    Assertions.assertEquals(journalCommand.description(), journal.getDescription());
    Assertions.assertEquals(journalCommand.unit(), journal.getUnit());
    Assertions.assertEquals(journalCommand.tags(), journal.getTags());
  }

  @Test
  void testDuplicated() {
    final var journalCommand =
        new JournalCommand.Create(null, "New Journal", "Desc", "UNIT", Set.of("TAG 1", "TAG 2"));

    this.journalService.handleCommand(journalCommand);
    final var error =
        Assertions.assertThrows(
            EntityAlreadyExistsError.class,
            () -> this.journalService.handleCommand(journalCommand));
    Assertions.assertEquals(
        Map.of("type", Journal.TYPE, "values", Map.of("name", journalCommand.name())),
        error.getProperties());
  }

  @Test
  void testOutOfRange() {
    final var journalCommand = new JournalCommand.Create(null, "", "Desc", "", Set.of());

    final var errors =
        Assertions.assertThrows(
            Errors.class, () -> this.journalService.handleCommand(journalCommand));
    for (final var error : errors.errors) {
      Assertions.assertEquals(error.getTitle(), FieldOutOfRangeError.class.getSimpleName());
    }
  }
}
