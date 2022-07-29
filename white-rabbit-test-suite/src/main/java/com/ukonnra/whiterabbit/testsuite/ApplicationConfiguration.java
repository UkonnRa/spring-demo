package com.ukonnra.whiterabbit.testsuite;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

@Configuration
@Import(CoreConfiguration.class)
public class ApplicationConfiguration {
  private final DataGenerator dataGenerator;

  public ApplicationConfiguration(DataGenerator dataGenerator) {
    this.dataGenerator = dataGenerator;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void onApplicationStarted() {
    dataGenerator.prepareData();
  }
}
