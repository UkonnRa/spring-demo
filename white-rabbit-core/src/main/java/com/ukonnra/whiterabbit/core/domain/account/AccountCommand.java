package com.ukonnra.whiterabbit.core.domain.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ukonnra.whiterabbit.core.Command;
import java.util.UUID;
import lombok.With;
import org.springframework.lang.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AccountCommand.Create.class, name = AccountCommand.TYPE_CREATE),
  @JsonSubTypes.Type(value = AccountCommand.Update.class, name = AccountCommand.TYPE_UPDATE),
  @JsonSubTypes.Type(value = AccountCommand.Delete.class, name = AccountCommand.TYPE_DELETE)
})
public sealed interface AccountCommand extends Command<AccountCommand>
    permits AccountCommand.Create, AccountCommand.Update, AccountCommand.Delete {
  String TYPE_CREATE = "AccountCommandCreate";
  String TYPE_UPDATE = "AccountCommandUpdate";
  String TYPE_DELETE = "AccountCommandDelete";

  record Create(
      @With @Nullable String targetId,
      UUID journal,
      String name,
      String description,
      AccountType accountType,
      AccountStrategy strategy,
      String unit)
      implements AccountCommand {
    @Override
    public String type() {
      return TYPE_CREATE;
    }
  }

  record Update(
      @With String targetId,
      @Nullable String name,
      @Nullable String description,
      @Nullable AccountType accountType,
      @Nullable AccountStrategy strategy,
      @Nullable String unit,
      @Nullable Boolean archived)
      implements AccountCommand {
    @Override
    public String type() {
      return TYPE_UPDATE;
    }
  }

  record Delete(@With String targetId) implements AccountCommand {

    @Override
    public String type() {
      return TYPE_DELETE;
    }
  }
}
