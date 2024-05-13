package com.ukonnra.wonderland.springelectrontest.hateoas.model;

import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MAX_LONG_TEXT;
import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MAX_NAMELY;
import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MAX_TAGS;
import static com.ukonnra.wonderland.springelectrontest.entity.AbstractEntity.MIN_NAMELY;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import org.springframework.lang.NonNull;

public record JournalAttributes(
    @NonNull @Schema(minLength = MIN_NAMELY, maxLength = MAX_NAMELY, example = "New Journal")
        String name,
    @NonNull @Schema(maxLength = MAX_LONG_TEXT, defaultValue = "''") String description,
    @NonNull @Schema(minLength = MIN_NAMELY, maxLength = MAX_NAMELY, example = "USD") String unit,
    @NonNull
        @ArraySchema(
            maxItems = MAX_TAGS,
            uniqueItems = true,
            schema = @Schema(minLength = MIN_NAMELY, maxLength = MAX_NAMELY),
            arraySchema = @Schema(defaultValue = "[]"))
        Set<String> tags)
    implements Attributes {}
