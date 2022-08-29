package com.ukonnra.whiterabbit.endpoint.graphql;

import com.ukonnra.whiterabbit.core.CoreConfiguration;
import com.ukonnra.whiterabbit.core.domain.account.AccountService;
import com.ukonnra.whiterabbit.core.domain.group.GroupService;
import com.ukonnra.whiterabbit.core.domain.journal.AccessItemValue;
import com.ukonnra.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.whiterabbit.core.domain.record.RecordService;
import com.ukonnra.whiterabbit.core.domain.user.UserService;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.GroupController;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.JournalController;
import com.ukonnra.whiterabbit.endpoint.graphql.controller.UserController;
import graphql.scalars.ExtendedScalars;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
@ComponentScan
@Import(CoreConfiguration.class)
@Slf4j
public class GraphQlApplicationConfiguration {
  private static final Set<GrantedAuthority> ALL_SCOPES =
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
          .map(scope -> new SimpleGrantedAuthority(String.format("SCOPE_%s", scope)))
          .collect(Collectors.toSet());
  private final OAuth2ResourceServerProperties resourceServerProperties;
  private final GraphQlProperties graphQlProperties;

  public GraphQlApplicationConfiguration(
      OAuth2ResourceServerProperties resourceServerProperties,
      GraphQlProperties graphQlProperties) {
    this.resourceServerProperties = resourceServerProperties;
    this.graphQlProperties = graphQlProperties;
  }

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
    final var jwtConfig = this.resourceServerProperties.getJwt();
    final var isJwtConfigEmpty =
        jwtConfig.getAudiences().isEmpty()
            && !StringUtils.hasText(jwtConfig.getIssuerUri())
            && !StringUtils.hasText(jwtConfig.getJwkSetUri())
            && jwtConfig.getPublicKeyLocation() == null;

    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .authorizeRequests(
            config -> {
              if (this.graphQlProperties.getGraphiql().isEnabled()) {
                config.antMatchers("/graphiql", "/graphql").permitAll();
              }
              config.anyRequest().authenticated();
            });

    if (isJwtConfigEmpty) {
      http.oauth2ResourceServer(
          config ->
              config.opaqueToken(
                  opaque ->
                      opaque.introspector(
                          token ->
                              new DefaultOAuth2AuthenticatedPrincipal(
                                  Map.of("sub", token), ALL_SCOPES))));
    } else {
      http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    }

    return http.build();
  }
}
