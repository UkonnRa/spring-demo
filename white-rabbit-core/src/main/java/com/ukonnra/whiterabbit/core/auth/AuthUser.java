package com.ukonnra.whiterabbit.core.auth;

import com.ukonnra.whiterabbit.core.domain.user.AuthIdValue;
import com.ukonnra.whiterabbit.core.domain.user.UserEntity;
import java.util.Set;
import org.springframework.lang.Nullable;

public record AuthUser(@Nullable UserEntity user, AuthIdValue authId, Set<String> scopes) {}
