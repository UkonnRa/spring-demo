package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

@Configuration
@Import(CoreConfiguration.class)
@ComponentScan
public class TestSuiteApplicationConfiguration {
  private final DataGenerator dataGenerator;

  public TestSuiteApplicationConfiguration(DataGenerator dataGenerator) {
    this.dataGenerator = dataGenerator;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void onApplicationStarted() {
    dataGenerator.prepareData();
  }
}
