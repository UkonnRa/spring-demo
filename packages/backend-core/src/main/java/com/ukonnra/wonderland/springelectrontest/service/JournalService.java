package com.ukonnra.wonderland.springelectrontest.service;

import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.entity.JournalDto;
import com.ukonnra.wonderland.springelectrontest.repository.JournalRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JournalService
    implements WriteService<Journal, Journal.Query, Object, JournalRepository, JournalDto> {
  private final JournalRepository repository;

  public JournalService(JournalRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Journal> handleCommand(Object command) {
    return null;
  }

  @Override
  public List<JournalDto> convert(Collection<Journal> entities) {
    return entities.stream().map(JournalDto::new).toList();
  }

  @Override
  public JournalRepository getRepository() {
    return this.repository;
  }
}
