/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          100: "#dbeafe", // blue-100
          300: "#93c5fd", // blue-300
          500: "#3b82f6", // blue-500
          700: "#1d4ed8", // blue-700
          900: "#1e3a8a", // blue-900
        },
        secondary: {
          100: "#ccfbf1", // teal-100
          300: "#5eead4", // teal-300
          500: "#14b8a6", // teal-500
          700: "#0f766e", // teal-700
          900: "#134e4a", // teal-900
        },
      },
    },
  },
  plugins: [],
} 