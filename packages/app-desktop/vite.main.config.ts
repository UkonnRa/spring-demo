import { defineConfig } from "vite";
import path from "path";
import { fileURLToPath } from "url";

// https://vitejs.dev/config
export default defineConfig({
  resolve: {
    alias: {
      "@": path.resolve(path.dirname(fileURLToPath(import.meta.url)), "src"),
      "@core": path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../frontend-core/src"),
    },
  },
});
