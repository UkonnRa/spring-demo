package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.GroupController;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.JournalController;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.UserController;
import graphql.scalars.ExtendedScalars;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ComponentScan
@Import(CoreConfiguration.class)
@Slf4j
public class GraphQlApplicationConfiguration {
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wb ->
        wb.scalar(ExtendedScalars.Date)
            .scalar(ExtendedScalars.Json)
            .scalar(ExtendedScalars.NonNegativeInt)
            .scalar(ExtendedScalars.GraphQLBigDecimal)
            .type(
                JournalController.TYPE_ACCESS_ITEM,
                builder ->
                    builder.typeResolver(
                        env -> {
                          if (env.getObject() instanceof AccessItemValue item) {
                            final var type =
                                switch (item.getItemType()) {
                                  case USER -> UserController.TYPE;
                                  case GROUP -> GroupController.TYPE;
                                };
                            return env.getSchema().getObjectType(type);
                          }
                          return null;
                        }));
  }

  @Bean
  SecurityFilterChain applicationFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .authorizeRequests(
            config ->
                config
                    .antMatchers(HttpMethod.GET, "/graphiql", "/graphql")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    return http.build();
  }
}
