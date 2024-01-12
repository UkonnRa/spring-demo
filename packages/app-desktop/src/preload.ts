import { contextBridge, ipcRenderer, type IpcRendererEvent } from "electron";

contextBridge.exposeInMainWorld("api", {
  getPort: async (): Promise<number> => {
    return await ipcRenderer.invoke("get-port");
  },
  onPortUpdated: (callback: (event: IpcRendererEvent, port?: number) => void) =>
    ipcRenderer.on("update:port", callback),
});
