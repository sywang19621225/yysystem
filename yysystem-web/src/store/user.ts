import { defineStore } from 'pinia'
import { login, getInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as any
  }),
  actions: {
    async login(userInfo: any) {
      const res: any = await login(userInfo)
      this.token = res.token
      localStorage.setItem('token', res.token)
    },
    async getInfo() {
      const res: any = await getInfo()
      this.userInfo = res
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
