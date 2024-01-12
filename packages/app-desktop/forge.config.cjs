const path = require("path");
const packageInfo = require("./package.json");

/**
 * @type {import('@electron-forge/shared-types').ForgeConfig}
 */
module.exports = {
  packagerConfig: {
    asar: true,
    name: packageInfo.name,
    extraResource: [path.resolve(__dirname, "../endpoint-desktop/build/native/nativeCompile")],
  },
  rebuildConfig: {},
  makers: [
    {
      name: "@electron-forge/maker-squirrel",
    },
    {
      name: "@electron-forge/maker-zip",
      platforms: ["darwin"],
    },
    {
      name: "@electron-forge/maker-deb",
      config: {},
    },
    {
      name: "@electron-forge/maker-rpm",
      config: {},
    },
  ],
  plugins: [
    {
      name: "@electron-forge/plugin-vite",
      config: {
        build: [
          {
            entry: "src/main.ts",
            config: "vite.main.config.ts",
          },
          {
            entry: "src/preload.ts",
            config: "vite.preload.config.ts",
          },
        ],
        renderer: [
          {
            name: "main_window",
            config: "vite.config.ts",
          },
        ],
      },
    },
  ],
};
