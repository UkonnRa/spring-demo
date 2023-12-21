package com.ukonnra.wonderland.springelectrontest.desktop;

import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.repository.JournalRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JournalController {
  private final JournalRepository journalRepository;

  public JournalController(JournalRepository journalRepository) {
    this.journalRepository = journalRepository;
  }

  @GetMapping
  public List<Journal> getJournals() {
    return this.journalRepository.findAll();
  }
}
