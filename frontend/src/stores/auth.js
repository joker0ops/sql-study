import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getMe } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isAuthenticated: (state) => !!state.token
  },
  actions: {
    async login(payload) {
      const res = await loginApi(payload)
      this.setAuth(res.data)
    },
    async register(payload) {
      const res = await registerApi(payload)
      this.setAuth(res.data)
    },
    async fetchMe() {
      const res = await getMe()
      this.user = res.data
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    setAuth(data) {
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
