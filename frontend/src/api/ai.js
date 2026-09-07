import http from './http'

export const getAiStatus = () => http.get('/ai/status')
export const saveAiConfig = (data) => http.post('/ai/config', data)
export const testAiConfig = (data) => http.post('/ai/test', data)
export const initAiDb = () => http.post('/ai/init')
export const resetAiDb = () => http.post('/ai/reset')
export const genQuestion = () => http.post('/ai/question')
export const executeSql = (data) => http.post('/ai/execute', data)
export const getAiStats = () => http.get('/ai/stats')
