/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  plugins: [],
  theme: {
    extend: {
      colors: {
        "primary-color": "var(--primary-color)",
        "ghost-primary-color": "var(--ghost-primary-color)",
        "user-admin-color": "var(--user-admin-color)",
      },
    },
  },
}