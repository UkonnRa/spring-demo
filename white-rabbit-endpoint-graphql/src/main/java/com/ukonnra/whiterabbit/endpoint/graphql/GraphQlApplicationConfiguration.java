package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.AuthUser;
import com.ukonnra.whiterabbit.core.CoreConfiguration;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.QUserEntity;
import com.ukonnra.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import graphql.scalars.ExtendedScalars;
import java.util.stream.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
                    .pathMatchers(HttpMethod.GET, "/graphiql")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(
                            this.jwtConverter.andThen(
                                authToken ->
                                    authToken
                                        .flatMap(
                                            token ->
                                                Mono.fromCallable(
                                                        () ->
                                                            this.userRepository.findOne(
                                                                QUserEntity.userEntity
                                                                    .authIds
                                                                    .any()
                                                                    .tokenValue
                                                                    .eq(token.getName())))
                                                    .subscribeOn(Schedulers.boundedElastic()))
                                        .flatMap(Mono::justOrEmpty)
                                        .flatMap(
                                            user ->
                                                authToken.map(
                                                    token -> {
                                                      final var authUser =
                                                          new AuthUser(user, token);
                                                      authUser
                                                          .getAuthorities()
                                                          .addAll(
                                                              Stream.of(
                                                                      UserService.READ_SCOPE,
                                                                      UserService.WRITE_SCOPE,
                                                                      GroupService.READ_SCOPE,
                                                                      GroupService.WRITE_SCOPE,
                                                                      JournalService.READ_SCOPE,
                                                                      JournalService.WRITE_SCOPE,
                                                                      AccountService.READ_SCOPE,
                                                                      AccountService.WRITE_SCOPE,
                                                                      RecordService.READ_SCOPE,
                                                                      RecordService.WRITE_SCOPE)
                                                                  .map(
                                                                      scope ->
                                                                          new SimpleGrantedAuthority(
                                                                              "SCOPE_" + scope))
                                                                  .toList());
                                                      return authUser;
                                                    }))))));
    return http.build();
  }
}
