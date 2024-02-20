package com.ukonnra.wonderland.springelectrontest;

import com.ukonnra.wonderland.springelectrontest.configuration.JpaConfiguration;
import com.ukonnra.wonderland.springelectrontest.configuration.JsonConfiguration;
import com.ukonnra.wonderland.springelectrontest.service.ReadService;
import jakarta.annotation.Nullable;
import jakarta.validation.Validator;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Import({JpaConfiguration.class, JsonConfiguration.class})
@ComponentScan(basePackageClasses = ReadService.class)
@ImportRuntimeHints(CoreConfiguration.RuntimeHints.class)
public class CoreConfiguration {
  public static class RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(
        org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
      hints
          .reflection()
          .registerTypeIfPresent(classLoader, "org.springframework.data.domain.Unpaged");
    }
  }

  @Bean
  Validator validator() {
    return new LocalValidatorFactoryBean();
  }
}
