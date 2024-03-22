import type {
  EntryCommand,
  JournalApi,
  JournalCommand,
  JournalQuery,
  JournalSort,
} from "@core/services";
import { Journal } from "@core/services";
import { AbstractWriteApi, type HttpMethod } from "./api";

class JournalApiImpl extends AbstractWriteApi<Journal, JournalQuery, JournalCommand, JournalSort> {
  protected override get modelType(): string {
    return "/journals";
  }

  protected override convert(input: Record<string, unknown>): Journal {
    return new Journal(
      input.id as string,
      input.createdDate as string,
      input.version as number,
      input.name as string,
      input.description as string,
      input.unit as string,
      input.tags as string[],
    );
  }

  protected parseCommand(
    command: JournalCommand,
  ): [string | null, HttpMethod, Record<string, unknown>] {
    return [null, "GET", {}];
  }
}

export const journalApi: JournalApi = new JournalApiImpl();
