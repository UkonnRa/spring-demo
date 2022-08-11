package com.ukonnra.whiterabbit.core;

import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class AuthUser extends AbstractAuthenticationToken {
  @Nullable private transient UserEntity detail;
  @Nullable private transient UUID principal;

  public AuthUser(@Nullable UserEntity detail, Authentication token) {
    super(
        Stream.of(
                token.getAuthorities(),
                detail == null
                    ? List.<GrantedAuthority>of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + detail.getRole())))
            .flatMap(Collection::stream)
            .toList());
    this.detail = detail;
    this.principal = detail == null ? null : detail.getId();
    this.setAuthenticated(token.isAuthenticated());
  }

  public AuthUser(@Nullable UserEntity detail, Set<String> scopes) {
    super(
        Stream.concat(
                scopes.stream().map(scope -> "SCOPE_" + scope),
                detail == null ? Stream.<String>empty() : Stream.of("ROLE_" + detail.getRole()))
            .map(SimpleGrantedAuthority::new)
            .toList());
    this.detail = detail;
    this.principal = detail == null ? null : detail.getId();
    this.setAuthenticated(true);
  }

  @Override
  public String getCredentials() {
    return "";
  }

  public Set<String> scopes() {
    return this.getAuthorities().stream()
        .map(authority -> authority.getAuthority().replace("SCOPE_", ""))
        .collect(Collectors.toSet());
  }

  public @Nullable UserEntity user() {
    return this.detail;
  }
}
