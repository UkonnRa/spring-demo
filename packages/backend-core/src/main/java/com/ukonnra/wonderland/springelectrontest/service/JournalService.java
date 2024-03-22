package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.entity.JournalCommand;
import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import com.ukonnra.wonderland.springelectrontest.error.EntityAlreadyExistsError;
import com.ukonnra.wonderland.springelectrontest.error.Errors;
import com.ukonnra.wonderland.springelectrontest.error.FieldOutOfRangeError;
import com.ukonnra.wonderland.springelectrontest.repository.JournalRepository;
import jakarta.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface JournalService
    extends WriteService<Journal, Journal.Query, JournalCommand, JournalRepository, JournalDto> {
  @Override
  default List<JournalDto> convert(Collection<Journal> entities) {
    return entities.stream().map(JournalDto::new).toList();
  }

  @Override
  @Transactional
  default List<Journal> handleCommand(JournalCommand command) {
    if (command instanceof JournalCommand.Create create) {
      return List.of(this.create(create));
    }

    return List.of();
  }

  private Journal create(final JournalCommand.Create command) {
    if (command.id() != null && this.getRepository().existsById(command.id())) {
      throw new EntityAlreadyExistsError(Journal.TYPE, "id", command.id().toString());
    }

    final var queryByName = new Journal.Query();
    queryByName.setName(Set.of(command.name()));
    if (this.getRepository().exists(queryByName)) {
      throw new EntityAlreadyExistsError(Journal.TYPE, "name", command.name());
    }

    final var journal =
        new Journal(command.name(), command.description(), command.unit(), command.tags());
    journal.setId(command.id());

    final var errors =
        Errors.of(
            this.getValidator().validate(journal).stream()
                .map(result -> FieldOutOfRangeError.of(Journal.TYPE, result))
                .filter(Objects::nonNull)
                .toList());
    if (errors != null) {
      throw errors;
    }

    return this.getRepository().saveAndFlush(journal);
  }

  @Service
  class Impl implements JournalService {
    private final JournalRepository repository;
    private final Validator validator;

    public Impl(JournalRepository repository, Validator validator) {
      this.repository = repository;
      this.validator = validator;
    }

    @Override
    public JournalRepository getRepository() {
      return this.repository;
    }

    @Override
    public Validator getValidator() {
      return this.validator;
    }
  }
}
