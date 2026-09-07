import http from './http'

// AI 相关接口会调用远程大模型，耗时较长，单独放宽超时（后端最多等 60s）
const AI_TIMEOUT = { timeout: 120000 }

export const getAiStatus = () => http.get('/ai/status')
export const saveAiConfig = (data) => http.post('/ai/config', data)
export const testAiConfig = (data) => http.post('/ai/test', data, AI_TIMEOUT)
export const initAiDb = () => http.post('/ai/init')
export const resetAiDb = () => http.post('/ai/reset')
export const genQuestion = () => http.post('/ai/question', null, AI_TIMEOUT)
export const executeSql = (data) => http.post('/ai/execute', data, AI_TIMEOUT)
export const getAiStats = () => http.get('/ai/stats')
