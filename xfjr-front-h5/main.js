import App from './App'
import { createSSRApp } from 'vue'
import store from './store'
import utils from './utils/utils.js'
import request from './utils/request.js'

export function createApp() {
	const app = createSSRApp(App)

	app.use(store)

	app.config.globalProperties.utils = utils
	app.config.globalProperties.request = request.request
	app.config.globalProperties.baseUrl = request.baseUrl
	app.config.globalProperties.fileServiceUrl = request.fileServiceUrl

	return {
		app
	}
}