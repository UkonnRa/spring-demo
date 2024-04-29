package com.ukonnra.wonderland.springelectrontest.entity;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AccountCommand.Create.class, name = AccountCommand.Create.TYPE),
  @JsonSubTypes.Type(value = AccountCommand.Update.class, name = AccountCommand.Update.TYPE),
  @JsonSubTypes.Type(value = AccountCommand.Delete.class, name = AccountCommand.Delete.TYPE),
  @JsonSubTypes.Type(value = AccountCommand.Batch.class, name = AccountCommand.Batch.TYPE),
})
public interface AccountCommand extends AbstractCommand {
  record Create(
      @Nullable UUID id,
      UUID journalId,
      String name,
      String description,
      String unit,
      Account.Type accountType,
      Set<String> tags)
      implements AccountCommand {
    public static final String TYPE = "accounts:create";

    @Override
    public String type() {
      return TYPE;
    }
  }

  record Update(
      UUID id,
      @JsonSetter(nulls = Nulls.AS_EMPTY) String name,
      @Nullable String description,
      @JsonSetter(nulls = Nulls.AS_EMPTY) String unit,
      @Nullable Account.Type accountType,
      @Nullable Set<String> tags)
      implements AccountCommand {
    public static final String TYPE = "accounts:update";

    @Override
    public String type() {
      return TYPE;
    }
  }

  record Delete(@JsonSetter(nulls = Nulls.AS_EMPTY) Set<UUID> id) implements AccountCommand {
    public static final String TYPE = "accounts:delete";

    @Override
    public String type() {
      return TYPE;
    }
  }

  record Batch(
      @JsonSetter(nulls = Nulls.AS_EMPTY) Set<Create> create,
      @JsonSetter(nulls = Nulls.AS_EMPTY) Set<Update> update,
      @JsonSetter(nulls = Nulls.AS_EMPTY) Set<UUID> delete)
      implements AccountCommand {
    public static final String TYPE = "accounts:batch";

    @Override
    public String type() {
      return TYPE;
    }
  }
}
