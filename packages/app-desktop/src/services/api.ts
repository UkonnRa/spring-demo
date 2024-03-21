import type { Command, FindAllArgs, Query, ReadApi, Model, WriteApi } from "@core/services";
import { Notify } from "quasar";
import { get, isNumber, isObject, isObjectLike, isString } from "lodash";

type HttpMethod = "GET" | "POST" | "DELETE" | "PATCH";

type ProblemDetail = {
  readonly status: number;
  readonly title: string;
  readonly detail?: string;
  readonly properties?: Record<string, unknown>;
};

export abstract class AbstractReadApi<M extends Model, Q extends Query, S extends string = string>
  implements ReadApi<M, Q, S>
{
  protected abstract get modelType(): string;

  protected abstract convert(input: Record<string, unknown>): M | undefined;

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  protected async loadIncluded(_models: M[]): Promise<Map<string, Model>> {
    return new Map();
  }

  private parseQuery(input: FindAllArgs<Q, S>): Record<string, string> {
    const entries: Record<string, string> = {};

    if (input.query) {
      for (const [key, value] of Object.entries(input.query)) {
        if (Array.isArray(value) && value.length > 0) {
          entries[`filter[${key}]`] = value.join(",");
        } else if (value) {
          entries[`filter[${key}]`] = `${value}`;
        }
      }
    }

    return entries;
  }

  private responseAsProblemDetail(body: Record<string, unknown>): ProblemDetail | undefined {
    if (isNumber(body.status) && isString(body.title) && body.status > 0 && body.title) {
      return {
        status: body.status,
        title: body.title,
        detail: `${body.detail}` ?? undefined,
        properties: isObject(body.properties)
          ? (body.properties as Record<string, unknown>)
          : undefined,
      };
    }
  }

  private async request(
    path: string,
    searchParams?: Record<string, string>,
    method: HttpMethod = "GET",
    body?: unknown,
  ): Promise<Record<string, unknown> | undefined> {
    const port = await window.api.getPort();
    let url = path;
    if (searchParams && Object.keys(searchParams).length > 0) {
      url = `${path}?${new URLSearchParams(searchParams)}`;
    }
    const urlInst = new URL(url, `http://localhost:${port}`);
    const response = await fetch(urlInst, {
      method,
      headers: {
        "Content-Type": "application/json",
      },
      body: body ? JSON.stringify(body) : undefined,
    });

    let responseBody: Record<string, unknown> | undefined;
    try {
      responseBody = await response.json();
    } catch (e) {
      console.error("Error when fetching: ", e);
    }

    if (response.ok) {
      return responseBody;
    }

    const problemDetail = isObject(responseBody)
      ? this.responseAsProblemDetail(responseBody)
      : undefined;
    if (problemDetail) {
      Notify.create({
        type: "negative",
        message: problemDetail.title,
        caption: problemDetail.detail,
      });
    }
  }

  async findAll(
    args: FindAllArgs<Q, S>,
    loadIncluded?: boolean,
  ): Promise<[M[], Map<string, Model>]> {
    const searchParams = this.parseQuery(args);
    const body = await this.request(this.modelType, searchParams);

    const models: M[] = [];
    const included = new Map<string, Model>();
    if (Array.isArray(body?.values)) {
      models.push(
        ...body.values.map((value) => this.convert(value)).filter((model): model is M => !!model),
      );
    }

    return [models, included];
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
