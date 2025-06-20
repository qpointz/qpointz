import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  base : '/sql-chat/',
  build : {
    outDir: '../../resources/ui/sql-chat',
    emptyOutDir: true
  },
  plugins: [react()],
  server: {
    proxy: {
      '/data-bot': 'http://localhost:8080'
    }
  }
});
