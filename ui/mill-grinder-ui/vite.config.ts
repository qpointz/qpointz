/// <reference types="vitest/config" />
import {defineConfig} from 'vitest/config'
import react from '@vitejs/plugin-react-swc'
import Inspect from 'vite-plugin-inspect'
import {visualizer} from 'rollup-plugin-visualizer';


export default defineConfig({
    plugins: [
        react(),
        Inspect({
            build: true,
            outputDir: '.vite-inspect'
        }),
        visualizer({open: false})
    ],
    base: '/app/',
    build: {
        outDir: '../mill-grinder-service/src/main/resources/static/app/v1',
        emptyOutDir: true,
        sourcemap: false,
    },
    server: {
        hmr: true,
        watch: {
            usePolling: true,
            interval: 300
        },
        proxy : {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    },
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: './src/test/setup.ts',
        css: true,
    }
})
