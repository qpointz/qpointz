import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/app/',
  build: {
    outDir: '../mill-grinder-service/src/main/resources/static/app/v2',
    emptyOutDir: true,
    sourcemap: false,
    target: 'esnext',
  },
  server: {
    port: 5173,
    open: true,
    hmr: true,
    watch: {
      usePolling: true,
      interval: 300
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
});
