import { createPinia } from "pinia";

import piniaPluginPersistedstate from 'pinia-plugin-persistedstate' //引入持久化插件
 
const pinia = createPinia() //创建pinia实例
pinia.use(piniaPluginPersistedstate) //将插件添加到 pinia 实例上

export default pinia

// 导出所有 store
export * from './modules/user'