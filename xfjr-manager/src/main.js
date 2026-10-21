import { createApp } from 'vue'

import 'normalize.css/normalize.css' // A modern alternative to CSS resets

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ElMessage, ElMessageBox } from 'element-plus'

import '@/styles/index.scss' // global css

import App from './App'
import store from './store'
import router from './router'

import 'virtual:svg-icons-register' // svg icon sprite
import { setupIcons } from '@/icons' // icon
import '@/permission' // permission control

const app = createApp(App)

// Element Plus
app.use(ElementPlus, { locale: zhCn })
// 挂载 $message / $confirm 以兼容原有页面中的 this.$message / this.$confirm 调用
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$confirm = ElMessageBox.confirm

// Vuex / Vue Router
app.use(store)
app.use(router)

// 全局注册 svg-icon 组件
setupIcons(app)

app.mount('#app')