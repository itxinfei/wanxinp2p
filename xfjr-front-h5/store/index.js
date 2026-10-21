import { createStore } from 'vuex'

const store = createStore({
	state: {
		/**
		 * 是否需要强制登录
		 */
		openStorageData: {},
		lendInfo: {}
	},
	mutations: {
		setOpenStorageData(state, storageData) {
			state.openStorageData = storageData
		},
		setLendInfo(state, userInfo) {
			state.lendInfo = userInfo
		}
	}
})

export default store