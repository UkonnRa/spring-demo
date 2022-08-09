package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.group.GroupRepository;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.testsuite.DataGenerator;
import com.ukonnra.whiterabbit.testsuite.GroupWriteTestSuite;
import com.ukonnra.whiterabbit.testsuite.TestSuiteApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TestSuiteApplicationConfiguration.class)
public class GroupWriteTest extends GroupWriteTestSuite {
  @Autowired
  protected GroupWriteTest(
      DataGenerator dataGenerator,
      UserRepository userRepository,
      GroupRepository repository,
      GroupService service) {
    super(new CoreWriteTaskHandler<>(service), dataGenerator, userRepository, repository);
  }
}
