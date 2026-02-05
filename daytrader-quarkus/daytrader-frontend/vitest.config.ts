import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    // Ensure each test file gets a fresh module state
    isolate: true,
    // Pool options for better isolation
    pool: 'forks',
    // Set environment variables for tests
    env: {
      NODE_ENV: 'test',
    },
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/dist/',
        'src/main.tsx',
        'src/vite-env.d.ts',
      ],
    },
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
  },
})

