package com.ukonnra.whiterabbit.core.domain.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ukonnra.whiterabbit.core.query.ExternalQuery;
import com.ukonnra.whiterabbit.core.query.IdQuery;
import com.ukonnra.whiterabbit.core.query.Query;
import com.ukonnra.whiterabbit.core.query.RangeQuery;
import com.ukonnra.whiterabbit.core.query.TextQuery;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record RecordQuery(
    @JsonProperty(ExternalQuery.FullText.TYPE) @Nullable ExternalQuery.FullText fullText,
    @Nullable IdQuery id,
    @Nullable UUID journal,
    @Nullable TextQuery name,
    @Nullable String description,
    @Nullable RecordType type,
    @Nullable RangeQuery<LocalDate> date,
    @Nullable TextQuery tag)
    implements Query {}
