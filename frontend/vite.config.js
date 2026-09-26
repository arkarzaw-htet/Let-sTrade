import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // sockjs-client references Node's `global` — polyfill it for the browser
  define: {
    global: 'globalThis',
  },
})
