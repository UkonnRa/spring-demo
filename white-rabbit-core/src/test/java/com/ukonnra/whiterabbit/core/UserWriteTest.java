package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import com.ukonnra.whiterabbit.testsuite.UserWriteTestSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TestSuiteApplicationConfiguration.class)
public class UserWriteTest extends UserWriteTestSuite {
  @Autowired
  protected UserWriteTest(
      DataGenerator dataGenerator, UserRepository userRepository, UserService service) {
    super(dataGenerator, userRepository, service);
  }
}
