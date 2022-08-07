package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.RecordWriteTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = {CoreConfiguration.class, TestConfiguration.class})
public class RecordWriteTest extends RecordWriteTestSuite {
  @Autowired
  protected RecordWriteTest(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      RecordRepository repository,
      RecordService service,
      AccountRepository accountRepository,
      JournalRepository journalRepository) {
    super(dataGenerator, userRepository, repository, service, accountRepository, journalRepository);
  }
}
