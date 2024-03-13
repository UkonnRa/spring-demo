import path from "path";

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    path.resolve(__dirname, "./packages/*/index.html"),
    path.resolve(__dirname, "./packages/*/src/**/*.{vue,js,ts,jsx,tsx}"),
  ],
  prefix: "tw-",
  theme: {
    extend: {},
  },
  plugins: [],
};
