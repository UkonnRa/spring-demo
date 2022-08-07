package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.JournalReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TestSuiteApplicationConfiguration.class)
public class JournalReadTest extends JournalReadTestSuite {
  @Autowired
  protected JournalReadTest(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      JournalRepository repository,
      JournalService service) {
    super(dataGenerator, userRepository, repository, service);
  }
}
