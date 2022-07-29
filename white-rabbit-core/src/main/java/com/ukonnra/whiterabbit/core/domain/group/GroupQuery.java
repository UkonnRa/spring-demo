package com.ukonnra.whiterabbit.core.domain.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record GroupQuery(
    @JsonProperty(ExternalQuery.FullText.TYPE) @Nullable ExternalQuery.FullText fullText,
    @JsonProperty(ExternalQuery.ContainingUser.TYPE) @Nullable UUID containingUser,
    @Nullable IdQuery id,
    @Nullable TextQuery name,
    @Nullable String description,
    @Nullable Set<UUID> admins,
    @Nullable Set<UUID> members)
    implements Query {}
