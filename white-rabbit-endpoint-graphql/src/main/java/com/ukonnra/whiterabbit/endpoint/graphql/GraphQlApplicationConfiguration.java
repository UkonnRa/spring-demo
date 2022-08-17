package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.GroupController;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.JournalController;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.UserController;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
@ComponentScan
@Import(CoreConfiguration.class)
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
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(ServerHttpSecurity.CorsSpec::disable)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.GET, "/graphiql", "/graphql")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);
    return http.build();
  }
}
