package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@Import(CoreConfiguration.class)
public class ApplicationConfiguration {
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wb -> wb.scalar(ExtendedScalars.DateTime).scalar(ExtendedScalars.GraphQLLong);
  }
}
