package com.ukonnra.wonderland.springelectrontest.entity;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = JournalCommand.Create.class, name = JournalCommand.Create.TYPE),
  @JsonSubTypes.Type(value = JournalCommand.Update.class, name = JournalCommand.Update.TYPE),
  @JsonSubTypes.Type(value = JournalCommand.Delete.class, name = JournalCommand.Delete.TYPE),
  @JsonSubTypes.Type(value = JournalCommand.Batch.class, name = JournalCommand.Batch.TYPE),
})
public sealed interface JournalCommand {
  record Create(@Nullable UUID id, String name, String description, String unit, Set<String> tags)
      implements JournalCommand {
    public static final String TYPE = "journals:create";
  }

  record Update(
      UUID id,
      @JsonSetter(nulls = Nulls.AS_EMPTY) String name,
      @Nullable String description,
      @JsonSetter(nulls = Nulls.AS_EMPTY) String unit,
      @Nullable Set<String> tags)
      implements JournalCommand {
    public static final String TYPE = "journals:update";
  }

  record Delete(@JsonSetter(nulls = Nulls.AS_EMPTY) Set<UUID> id) implements JournalCommand {
    public static final String TYPE = "journals:delete";
  }

  record Batch(
      @JsonSetter(nulls = Nulls.AS_EMPTY) Set<Create> create,
      @JsonSetter(nulls = Nulls.AS_EMPTY) Set<Update> update,
      @JsonSetter(nulls = Nulls.AS_EMPTY) Set<UUID> delete)
      implements JournalCommand {
    public static final String TYPE = "journals:batch";
  }
}
