package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@org.springframework.boot.test.context.TestConfiguration
@Import(CoreConfiguration.class)
public class TestConfiguration {
  @Bean
  DataGenerator dataGenerator(
      UserRepository userRepository,
      GroupRepository groupRepository,
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      RecordRepository recordRepository) {
    return new DataGenerator(
        userRepository, groupRepository, journalRepository, accountRepository, recordRepository);
  }
}
