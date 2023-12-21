package com.ukonnra.wonderland.springelectrontest;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.entity.Journal;
import com.ukonnra.wonderland.springelectrontest.repository.JournalRepository;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackageClasses = AbstractEntity.class)
@EnableJpaRepositories(basePackageClasses = CoreConfiguration.class)
@EnableJpaAuditing
@EnableTransactionManagement
@Slf4j
public class CoreConfiguration {
  private final JournalRepository journalRepository;

  public CoreConfiguration(JournalRepository journalRepository) {
    this.journalRepository = journalRepository;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  @EventListener(ApplicationReadyEvent.class)
  void onApplicationReady() {
    final var journals =
        List.of(
            new Journal("Name 1", "Desc 1", "Unit 1", Set.of("Tag 1", "Tag 2")),
            new Journal("Name 2", "Desc 2", "Unit 2", Set.of("Tag 2", "Tag 4")),
            new Journal("Name 3", "Desc 3", "Unit 3", Set.of("Tag 1", "Tag 4")));

    final var results = this.journalRepository.saveAllAndFlush(journals);
    for (final var result : results) {
      log.info("After Saving: {}", result);
    }
  }
}
