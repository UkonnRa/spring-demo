package com.ukonnra.whiterabbit.core.domain.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.UUID;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record AccountQuery(
    @JsonProperty(ExternalQuery.FullText.TYPE) @Nullable ExternalQuery.FullText fullText,
    @Nullable IdQuery id,
    @Nullable UUID journal,
    @Nullable TextQuery name,
    @Nullable String description,
    @Nullable AccountType type,
    @Nullable AccountStrategy strategy,
    @Nullable String unit,
    @Nullable Boolean includeArchived)
    implements Query {}
