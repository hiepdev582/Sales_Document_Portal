/// <reference types="nuxt" />
// https://nuxt.com/docs/api/configuration/nuxt-config
import { defineNuxtConfig } from "nuxt/config";

export default defineNuxtConfig({
  devtools: { enabled: false },
  css: ["~/assets/css/main.css"],
  app: {
    head: {
      title: "Sales Document Portal - Secure Resource Management System",
      meta: [
        {
          name: "description",
          content:
            "Secure Sales Document Management Portal with Advanced BOLA/BFLA & Cryptography",
        },
        { name: "viewport", content: "width=device-width, initial-scale=1" },
      ],
      link: [
        { rel: "preconnect", href: "https://fonts.googleapis.com" },
        {
          rel: "preconnect",
          href: "https://fonts.gstatic.com",
          crossorigin: "",
        },
        {
          rel: "stylesheet",
          href: "https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap",
        },
      ],
    },
  },
  routeRules: {
    '/api/**': {
      proxy: 'http://localhost:8080/api/**'
    }
  }
});
