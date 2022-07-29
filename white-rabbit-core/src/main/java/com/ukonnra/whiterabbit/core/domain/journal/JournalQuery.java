package com.ukonnra.whiterabbit.core.domain.journal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.util.UUID;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record JournalQuery(
    @JsonProperty(ExternalQuery.FullText.TYPE) @Nullable ExternalQuery.FullText fullText,
    @JsonProperty(ExternalQuery.ContainingUser.TYPE) @Nullable UUID containingUser,
    @Nullable IdQuery id,
    @Nullable TextQuery name,
    @Nullable String description,
    @Nullable TextQuery tags,
    @Nullable String unit,
    @Nullable Boolean includeArchived,
    @Nullable AccessItemValue admin,
    @Nullable AccessItemValue member)
    implements Query {}
