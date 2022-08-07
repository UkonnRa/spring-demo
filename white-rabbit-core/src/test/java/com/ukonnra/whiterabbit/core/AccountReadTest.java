package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.account.AccountRepository;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.AccountReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TestSuiteApplicationConfiguration.class)
public class AccountReadTest extends AccountReadTestSuite {
  @Autowired
  protected AccountReadTest(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      AccountRepository repository,
      AccountService service,
      JournalRepository journalRepository) {
    super(userRepository, dataGenerator, repository, service, journalRepository);
  }
}
