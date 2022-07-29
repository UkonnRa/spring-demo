package com.ukonnra.whiterabbit.core.domain.user;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ukonnra.whiterabbit.core.Command;
import java.util.Set;
import lombok.With;
import org.springframework.lang.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = UserCommand.Create.class, name = UserCommand.TYPE_CREATE),
  @JsonSubTypes.Type(value = UserCommand.Update.class, name = UserCommand.TYPE_UPDATE),
  @JsonSubTypes.Type(value = UserCommand.Delete.class, name = UserCommand.TYPE_DELETE)
})
public sealed interface UserCommand extends Command<UserCommand>
    permits UserCommand.Create, UserCommand.Delete, UserCommand.Update {
  String TYPE_CREATE = "UserCommandCreate";
  String TYPE_UPDATE = "UserCommandUpdate";
  String TYPE_DELETE = "UserCommandDelete";

  record Create(
      @With @Nullable String targetId,
      String name,
      @Nullable RoleValue role,
      @Nullable Set<AuthIdValue> authIds)
      implements UserCommand {

    @Override
    public String type() {
      return TYPE_CREATE;
    }
  }

  record Update(
      @With String targetId,
      @Nullable String name,
      @Nullable RoleValue role,
      @Nullable Set<AuthIdValue> authIds)
      implements UserCommand {
    @Override
    public String type() {
      return "UserCommandUpdate";
    }
  }

  record Delete(@With String targetId) implements UserCommand {
    @Override
    public String type() {
      return "UserCommandDelete";
    }
  }
}
