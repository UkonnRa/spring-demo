import type { Command, Model, Query, WriteApi } from "@core/services";

export const JOURNAL_TYPE = "journals";

export const JOURNAL_API_KEY = Symbol("JOURNAL_API_KEY");

export const JOURNAL_ICON = "book";

export class Journal implements Model<typeof JOURNAL_TYPE> {
  id: string;
  createdDate: string;
  version: number;
  name: string;
  description: string;
  unit: string;
  tags: string[];

  constructor(
    id: string,
    createdDate: string,
    version: number,
    name: string,
    description: string,
    unit: string,
    tags: string[],
  ) {
    this.id = id;
    this.createdDate = createdDate;
    this.version = version;
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.tags = tags;
  }

  get modelType(): typeof JOURNAL_TYPE {
    return JOURNAL_TYPE;
  }
}

export type JournalSort = "name" | "unit" | "-name" | "-unit";

export interface JournalQuery extends Query {
  readonly id?: string[];
  readonly name?: string[];
  readonly unit?: string[];
  readonly tag?: string[];
  readonly fullText?: string;
}

export interface JournalCommandCreate extends Command<`${typeof JOURNAL_TYPE}:create`> {
  readonly id?: string;
  readonly name: string;
  readonly description: string;
  readonly unit: string;
  readonly tags: string[];
}

export interface JournalCommandUpdate extends Command<`${typeof JOURNAL_TYPE}:update`> {
  readonly id: string;
  readonly name?: string;
  readonly description?: string;
  readonly unit?: string;
  readonly tags?: string[];
}

export interface JournalCommandDelete extends Command<`${typeof JOURNAL_TYPE}:delete`> {
  readonly id: string[];
}

export interface JournalCommandBatch extends Command<`${typeof JOURNAL_TYPE}:batch`> {
  readonly create?: Omit<JournalCommandCreate, "commandType">[];
  readonly update?: Omit<JournalCommandUpdate, "commandType">[];
  readonly delete?: string[];
}

export type JournalCommand =
  | JournalCommandCreate
  | JournalCommandUpdate
  | JournalCommandDelete
  | JournalCommandBatch;

export type JournalApi = WriteApi<Journal, JournalQuery, JournalCommand, JournalSort>;
