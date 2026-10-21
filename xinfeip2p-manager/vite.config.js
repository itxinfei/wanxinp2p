import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import path from 'path'

const resolve = dir => path.join(__dirname, dir)

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const port = 8079 // dev port

  return {
    plugins: [
      vue(),
      createSvgIconsPlugin({
        iconDirs: [resolve('src/icons/svg')],
        symbolId: 'icon-[name]'
      })
    ],
    resolve: {
      alias: {
        '@': resolve('src')
      },
      extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    css: {
      preprocessorOptions: {
        scss: {
          silenceDeprecations: ['legacy-js-api', 'import']
        }
      }
    },
    server: {
      port: port,
      open: true,
      proxy: {
        [env.VITE_BASE_API || '/dev-api']: {
          target: 'http://127.0.0.1:53010',
          changeOrigin: true,
          rewrite: p => p.replace(new RegExp('^' + (env.VITE_BASE_API || '/dev-api')), '')
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'static',
      sourcemap: false,
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          manualChunks: {
            'element-plus': ['element-plus'],
            'vue-vendor': ['vue', 'vue-router', 'vuex']
          }
        }
      }
    }
  }
})