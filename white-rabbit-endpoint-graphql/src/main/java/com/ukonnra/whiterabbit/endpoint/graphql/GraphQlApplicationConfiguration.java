package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
@ComponentScan
@Import(CoreConfiguration.class)
public class GraphQlApplicationConfiguration {
  private final ReactiveJwtAuthenticationConverter jwtConverter =
      new ReactiveJwtAuthenticationConverter();
  private final UserRepository userRepository;

  public GraphQlApplicationConfiguration(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wb ->
        wb.scalar(ExtendedScalars.Date)
            .scalar(ExtendedScalars.Json)
            .scalar(ExtendedScalars.NonNegativeInt)
            .scalar(ExtendedScalars.GraphQLBigDecimal);
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
