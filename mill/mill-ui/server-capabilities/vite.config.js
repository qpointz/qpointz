import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: '/overview/',
  plugins: [react()],
  server: {
    proxy: {
      '/.well-known': 'http://localhost:8080',
    }
  }
})
