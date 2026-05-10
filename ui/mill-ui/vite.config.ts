import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/app/',
  build: {
    outDir: '../../services/mill-ui-service/src/main/resources/static/app/v2',
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
      // Spring oauth2Login defaults live at servlet root `/oauth2/**`, not under SPA `/app/`.
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // Legacy/wrong SPA-prefixed hrefs still reach the backend if present.
      '/app/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // OAuth2 callback only — do not proxy `/app/login` itself or the Vite dev server cannot serve the SPA login route.
      '/app/login/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/login/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/app/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/.well-known': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/services': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
});
