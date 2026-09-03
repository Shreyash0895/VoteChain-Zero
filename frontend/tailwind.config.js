/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#10141A',
        surface: '#1A2029',
        rule: '#2B323D',
        paper: '#EDEAE2',
        'paper-dim': '#9AA0AC',
        brass: '#C9A227',
        'brass-dim': '#8A701F',
        teal: '#3EC9B0',
        signal: '#C1443D',
      },
      fontFamily: {
        serif: ['"Source Serif 4"', 'serif'],
        sans: ['"IBM Plex Sans"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
}