/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          100: '#dbeafe',
          300: '#93c5fd',
          500: '#3b82f6',
          700: '#1d4ed8',
          900: '#1e3a8a',
        },
        secondary: {
          100: '#ccfbf1',
          300: '#5eead4',
          500: '#14b8a6',
          700: '#0f766e',
          900: '#134e4a',
        },
      },
    },
  },
  plugins: [],
} 