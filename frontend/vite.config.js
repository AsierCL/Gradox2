import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
  },
  preview: {
    port: 4173,
  },
  build: {
    rolldownOptions: {
      output: {
        codeSplitting: {
          groups: [
            {
              name: 'react-vendor',
              test: /node_modules\/(react|react-dom|react-router|react-router-dom|scheduler)\//,
            },
            {
              name: 'motion',
              test: /node_modules\/framer-motion\//,
            },
            {
              name: 'data',
              test: /node_modules\/@tanstack\//,
            },
            {
              name: 'icons',
              test: /node_modules\/@phosphor-icons\//,
            },
          ],
        },
      },
    },
  },
})
