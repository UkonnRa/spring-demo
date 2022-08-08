package com.ukonnra.whiterabbit.core.domain.user;

import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.List;
import lombok.Builder;
import lombok.With;
import org.springframework.lang.Nullable;

@Builder
public record UserQuery(
    @With @Nullable IdQuery id,
    @Nullable TextQuery name,
    @Nullable RoleValue role,
    @Nullable List<String> authIdProviders)
    implements Query {
  public UserQuery() {
    this(null, null, null, null);
  }
}
