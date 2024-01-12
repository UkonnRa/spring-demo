import type { Command, FindAllArgs, Query, ReadApi, Model, WriteApi } from "@core/services";
import { Notify } from "quasar";

export abstract class AbstractReadApi<M extends Model, Q extends Query, S extends string = string>
  implements ReadApi<M, Q, S>
{
  protected abstract convert(input: Record<string, unknown>): M;

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  protected async loadIncluded(_models: M[]): Promise<Map<string, Model>> {
    return new Map();
  }

  protected abstract get findAllKey(): string;

  protected abstract get findByIdKey(): string;

  async findAll(
    { query, sort }: FindAllArgs<Q, S>,
    loadIncluded?: boolean,
  ): Promise<[M[], Map<string, Model>]> {
    return [[], new Map()];
    // let response: Record<string, unknown>[] = [];
    // try {
    //   response = (await window.api.invoke(this.findAllKey, {
    //     query,
    //     sort,
    //   })) as Record<string, unknown>[];
    // } catch (e) {
    //   console.error(e);
    //   throw e;
    // }
    //
    // const models = response.map((record) => this.convert(record));
    // return [models, loadIncluded ? await this.loadIncluded(models) : new Map()];
  }

  async findById(id: string, loadIncluded?: boolean): Promise<[M, Map<string, Model>] | null> {
    return null;
    // let response: Record<string, unknown> | null = null;
    // try {
    //   response = (await ipcRenderer.invoke(this.findByIdKey, {
    //     id,
    //   })) as Record<string, unknown> | null;
    // } catch (e) {
    //   console.error(e);
    //   throw e;
    // }
    //
    // if (response) {
    //   const model = this.convert(response);
    //   return [model, loadIncluded ? await this.loadIncluded([model]) : new Map()];
    // } else {
    //   return response;
    // }
  }
}

export abstract class AbstractWriteApi<
    M extends Model,
    Q extends Query,
    C extends Command,
    S extends string,
  >
  extends AbstractReadApi<M, Q, S>
  implements WriteApi<M, Q, C, S>
{
  protected abstract get handleCommandKey(): string;
  async handleCommand(command: C): Promise<M[]> {
    return [];

    // let response: Record<string, unknown>[] = [];

    // try {
    //   response = (await ipcRenderer.invoke(this.handleCommandKey, {
    //     command,
    //   })) as Record<string, unknown>[];
    // } catch (e) {
    //   Notify.create({
    //     color: "negative",
    //     message: e as string,
    //   });
    //   throw new Error(e as string);
    // }
    //
    // return response.map((record) => this.convert(record));
  }
}
