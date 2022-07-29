package com.ukonnra.whiterabbit.core.domain.journal;

import com.ukonnra.whiterabbit.core.Command;
import java.util.Set;
import lombok.With;
import org.springframework.lang.Nullable;

public sealed interface JournalCommand extends Command<JournalCommand>
    permits JournalCommand.Create, JournalCommand.Delete, JournalCommand.Update {
  String TYPE_CREATE = "JournalCommandCreate";
  String TYPE_UPDATE = "JournalCommandUpdate";
  String TYPE_DELETE = "JournalCommandDelete";

  record Create(
      @With @Nullable String targetId,
      String name,
      String description,
      Set<String> tags,
      String unit,
      Set<AccessItemValue> admins,
      Set<AccessItemValue> members)
      implements JournalCommand {
    @Override
    public String type() {
      return TYPE_CREATE;
    }
  }

  record Update(
      @With String targetId,
      @Nullable String name,
      @Nullable String description,
      @Nullable Set<String> tags,
      @Nullable String unit,
      @Nullable Boolean archived,
      @Nullable Set<AccessItemValue> admins,
      @Nullable Set<AccessItemValue> members)
      implements JournalCommand {
    @Override
    public String type() {
      return TYPE_UPDATE;
    }
  }

  record Delete(@With String targetId) implements JournalCommand {
    @Override
    public String type() {
      return TYPE_DELETE;
    }
  }
}
