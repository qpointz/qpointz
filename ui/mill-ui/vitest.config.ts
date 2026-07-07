import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config';

const isCi = process.env.CI === 'true' || process.env.CI === '1';

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
      css: false,
      testTimeout: 30_000,
      hookTimeout: 30_000,
      maxWorkers: isCi ? 2 : undefined,
      reporters: isCi ? ['default', 'junit'] : ['default'],
      outputFile: isCi ? { junit: '.test/TEST.xml' } : undefined,
      coverage: {
        provider: 'v8',
        reporter: ['text', 'text-summary', 'lcov'],
        include: ['src/**/*.{ts,tsx}'],
        exclude: [
          'src/test/**',
          'src/**/__tests__/**',
          'src/**/*.test.{ts,tsx}',
          'src/**/*.d.ts',
          'src/vite-env.d.ts',
          'src/main.tsx',
        ],
        thresholds: {
          statements: 50,
          branches: 35,
          functions: 45,
          lines: 50,
        },
      },
    },
  }),
);
