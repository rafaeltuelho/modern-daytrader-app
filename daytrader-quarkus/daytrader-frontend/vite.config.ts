import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/accounts': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/profiles': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/holdings': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/portfolio': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/quotes': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/market': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
})
