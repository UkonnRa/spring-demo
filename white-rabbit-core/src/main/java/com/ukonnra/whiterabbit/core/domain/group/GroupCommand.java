package com.ukonnra.whiterabbit.core.domain.group;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ukonnra.whiterabbit.core.Command;
import java.util.Set;
import java.util.UUID;
import lombok.With;
import org.springframework.lang.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = GroupCommand.Create.class, name = GroupCommand.TYPE_CREATE),
  @JsonSubTypes.Type(value = GroupCommand.Update.class, name = GroupCommand.TYPE_UPDATE),
  @JsonSubTypes.Type(value = GroupCommand.Delete.class, name = GroupCommand.TYPE_DELETE)
})
public sealed interface GroupCommand extends Command<GroupCommand>
    permits GroupCommand.Create, GroupCommand.Update, GroupCommand.Delete {
  String TYPE_CREATE = "GroupCommandCreate";
  String TYPE_UPDATE = "GroupCommandUpdate";
  String TYPE_DELETE = "GroupCommandDelete";

  record Create(
      @With @Nullable String targetId,
      String name,
      String description,
      Set<UUID> admins,
      Set<UUID> members)
      implements GroupCommand {
    @Override
    public String type() {
      return TYPE_CREATE;
    }
  }

  record Update(
      @With String targetId,
      @Nullable String name,
      @Nullable String description,
      @Nullable Set<UUID> admins,
      @Nullable Set<UUID> members)
      implements GroupCommand {
    @Override
    public String type() {
      return TYPE_UPDATE;
    }
  }

  record Delete(@With String targetId) implements GroupCommand {

    @Override
    public String type() {
      return TYPE_DELETE;
    }
  }
}
