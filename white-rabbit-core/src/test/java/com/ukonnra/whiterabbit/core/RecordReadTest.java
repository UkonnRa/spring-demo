package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.record.RecordRepository;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.RecordReadTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TestSuiteApplicationConfiguration.class)
public class RecordReadTest extends RecordReadTestSuite {
  @Autowired
  protected RecordReadTest(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      RecordRepository repository,
      RecordService service) {
    super(new CoreReadTaskHandler<>(service), dataGenerator, userRepository, repository);
  }
}
