package com.ukonnra.wonderland.springelectrontest;

import com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity;
import com.ukonnra.wonderland.springelectrontest.entity.User;
import com.ukonnra.wonderland.springelectrontest.repository.UserRepository;
import java.util.List;
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
  private final UserRepository userRepository;

  public CoreConfiguration(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  @EventListener(ApplicationReadyEvent.class)
  void onApplicationReady() {
    final var users = List.of(new User("Name 1", 1), new User("Name 2", 2), new User("Name 3", 3));

    final var results = this.userRepository.saveAllAndFlush(users);
    for (final var result : results) {
      log.info("After Saving: {}", result);
    }
  }
}
