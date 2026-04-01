import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // 🔥 This tells Vite: if the URL starts with /auth, send it to Spring Boot
      '/auth': {
        target: 'http://localhost:9091',
        changeOrigin: true,
        secure: false,
      },
      '/api': {
        target: 'http://localhost:9091',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})