import {defineConfig} from 'vite'
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
        visualizer({open: true})
    ],
    base: '/app/',
    build: {
        outDir: '../mill-grinder-service/src/main/resources/static/app',
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
    }
})
