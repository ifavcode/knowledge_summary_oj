import { defineStore } from "pinia"

export const useGlobalStore = defineStore('global', {
  state: () => ({
    isLoginElementShow: false as boolean,
    userInfo: null as null | User | undefined,
    authorization: '' as string,
    isAdmin: false as boolean,
    userAuth: [] as Array<string>,
    aiUrl: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k' as string
  }),
  getters: {
  },
  actions: {
    setUserInfo(userInfo: null | User | undefined) {
      this.userInfo = userInfo
    }
  },
})