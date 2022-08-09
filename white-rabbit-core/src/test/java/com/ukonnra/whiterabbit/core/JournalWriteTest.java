package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.JournalWriteTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TestSuiteApplicationConfiguration.class)
public class JournalWriteTest extends JournalWriteTestSuite {
  @Autowired
  protected JournalWriteTest(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      JournalRepository repository,
      JournalService service,
      GroupRepository groupRepository) {
    super(
        new CoreWriteTaskHandler<>(service),
        dataGenerator,
        userRepository,
        repository,
        groupRepository);
  }
}
