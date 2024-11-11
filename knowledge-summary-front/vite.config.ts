import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import {
  NaiveUiResolver,
  AntDesignVueResolver,
} from "unplugin-vue-components/resolvers";
import { resolve } from "path";

// https://vitejs.dev/config/
export default defineConfig({
  base: "./",
  resolve: {
    alias: {
      "~/": `${resolve(__dirname, "src")}/`,
    },
  },
  plugins: [
    vue(),
    Components({
      dirs: ["src/components", "src/views"],
      extensions: ["vue"],
      dts: "presets/components.d.ts",
      include: [/\.vue$/, /\.vue\?vue/],
      exclude: [
        /[\\/]node_modules[\\/]/,
        /[\\/]\.git[\\/]/,
        /[\\/]\.nuxt[\\/]/,
      ],
      resolvers: [
        NaiveUiResolver(),
        AntDesignVueResolver({
          importStyle: false,
        }),
      ],
    }),
    AutoImport({
      imports: ["vue", "vue-router", "@vueuse/core"],
      dts: "presets/auto-import.d.ts",
    }),
  ],
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "~/init.scss";',
      },
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080/mvc",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
      "/flux": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/onlineFile": {
        target: "https://www.guetzjb.cn",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/onlineFile/, ""),
      },
      "/ossFile": {
        target: "https://guetzjb.cn",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ossFile/, ""),
      },
    },
  },
});
