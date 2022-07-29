package com.ukonnra.whiterabbit.core.domain.user;

import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.List;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record UserQuery(
    @Nullable IdQuery id,
    @Nullable TextQuery name,
    @Nullable RoleValue role,
    @Nullable List<String> authIdProviders)
    implements Query {}
