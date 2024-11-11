<script setup lang='ts'>
import { useMessage } from 'naive-ui';
import { useGlobalStore } from '~/store/globalStore';
import { checkLogin, getAssetsImages } from '~/utils';
import { request } from '~/utils/request';
import { emitter } from '~/utils/index'
//@ts-ignore
import VditorPreview from 'vditor/dist/method.min.js'
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-light.css'

let inputValue = ref('')
const message = useMessage()
const loading = ref(false)
//AI首次响应
const isBack = ref(true)
const globalStore = useGlobalStore()
const currentGroupId = ref(-1)
const screenWidth = document.body.clientWidth
const contentScrollbar = ref()
const contentScrollbarEle = ref()

hljs.configure({
  ignoreUnescapedHTML: true
});

//对话列表
const dialogueList = ref<Array<any>>([
])
const dialogueApi = ref<Array<any>>([
])

const initGroup = async () => {
  //未选择 则创建
  let tmpInputStr = inputValue.value
  inputValue.value = ''
  if (currentGroupId.value == -1) {
    const groupId: number = await request('/flux/dialogue/user/group/add', {
      method: 'post',
      data: {
        groupTitle: tmpInputStr.substring(0, 50)
      }
    })
    currentGroupId.value = groupId
    //reload group
    emitter.emit('refreshGroup', currentGroupId.value)
  }
}

let key: NodeJS.Timer
const sendMessage = async () => {
  if (loading.value) {
    message.warning('点击频率过快!!')
    return
  }
  if (!checkLogin(true)) return
  if (inputValue.value == '') {
    message.warning('输入内容不能为空')
    return
  }
  loading.value = true
  isBack.value = false
  dialogueList.value.push({
    role: 'user',
    content: await VditorPreview.md2html(inputValue.value)
  })
  dialogueApi.value.push({
    role: 'user',
    content: inputValue.value
  })
  if (screenWidth > 480) {
    scrollDown()
  } else {
    mobileScrollDown()
  }
  try {
    //初始化组
    await initGroup()
    await request('/flux/bigData/send', {
      method: 'post',
      data: {
        messages: dialogueApi.value,
        groupId: currentGroupId.value,
        stream: true,
        aiUrl: globalStore.aiUrl
      }
    })
  } catch (error) {
    console.log(error);
    dialogueApi.value.pop()
    dialogueList.value.pop()
  } finally {
    loading.value = false
  }
}

//PC端滚动
const scrollDown = () => {
  nextTick(() => {
    contentScrollbar.value.scrollTo({
      top: contentScrollbarEle.value.offsetHeight,
      behavior: 'smooth'
    })
  })
}

//移动端滚动
const mobileScrollDown = () => {
  nextTick(() => {
    window.scrollTo({
      top: document.body.offsetHeight,
      behavior: 'smooth'
    })
  })
}

const getMessagesByGroupId = async () => {
  if (currentGroupId.value < 0) {
    dialogueApi.value = []
    dialogueList.value = []
  }
  const res: AjaxResult<XzDialogue[]> = await request('/flux/dialogue/group/all', {
    params: {
      groupId: currentGroupId.value
    }
  })
  let arr: any = []
  let parsedArr: any = []
  for (let v of res.data as any) {
    const str = await VditorPreview.md2html(v.diaContent, {
    });
    parsedArr.push({
      role: v.diaRole,
      content: str
    })
    arr.push({
      role: v.diaRole,
      content: v.diaContent
    })
  }
  dialogueList.value = [...parsedArr]
  dialogueApi.value = [...arr]
  nextTick(() => {
    hljs.highlightAll()
    if (screenWidth > 480) {
      contentScrollbar.value.scrollTo({
        top: contentScrollbarEle.value.offsetHeight,
      })
    } else {
      window.scrollTo({
        top: document.body.offsetHeight,
      })
    }
    for (let i = 0; i < dialogueList.value.length; i++) {
      VditorPreview.mathRender(document.getElementsByClassName('xz-content' + i)[0], {
        math: {
          inlineDigit: true,
          engine: 'KaTeX'
        },
      })
    }
  })
}

//内容滚动触发
const handleContentScroll = () => {
}

//链接
let es: EventSource
// let typeit: any = null
let isFirst = false
let idx = 0
let tmpStr: string = ''
const esConnect = () => {
  if (globalStore.authorization) {
    es = new EventSource(`/flux/bigData/connect/${globalStore.authorization}`)
    es.onopen = () => {
      console.log('连接成功');
    }
    es.onmessage = async (e) => {
      if (e.data == 'connected') return
      if (!isFirst) {
        isFirst = true
        idx = dialogueList.value.length
        dialogueList.value.push({
          role: 'assistant',
          content: ''
        })
        dialogueApi.value.push({
          role: 'assistant',
          content: ''
        })
      }
      isBack.value = true
      const parsedObj = JSON.parse(e.data)
      //加入数据
      tmpStr += parsedObj.result
      dialogueApi.value[idx].content = dialogueApi.value[idx].content + parsedObj.result
      clearInterval(key)
      key = setInterval(() => {
        if (screenWidth > 480) {
          scrollDown()
        } else {
          mobileScrollDown()
        }
      }, 100)
      // const parsedRes: string = await VditorPreview.md2html(parsedObj.result)
      dialogueList.value[idx].content = await VditorPreview.md2html(tmpStr, {
      })
      if (parsedObj.is_end) {
        clearInterval(key)
        if (screenWidth > 480) {
          scrollDown()
        } else {
          mobileScrollDown()
        }
        idx = 0;
        isFirst = false
        tmpStr = ''
        loading.value = false
      }
      nextTick(() => {
        VditorPreview.mathRender(document.getElementsByClassName('xz-content' + (dialogueList.value.length - 1))[0], {
          math: {
            inlineDigit: true,
            engine: 'KaTeX'
          },
        })
        hljs.highlightAll()
      })
      // if (typeit == null) {
      //   typeit = new (TypeIt as any)(".xz-content" + idx, {
      //     waitUntilVisible: false,
      //     speed: 20,
      //   })
      // }
      // if (parsedObj.is_end) {
      //   typeit.type(parsedObj.result).flush().destroy(true)
      //   clearInterval(key)
      //   scrollDown()
      //   idx = 0;
      //   isFirst = false
      //   typeit = null
      // } else {
      //   typeit.type(parsedObj.result).flush()
      // }
      // hljs.highlightAll()
    }
  } else {

  }
}

const loginSuccess = () => {
  esConnect()
}

const logoutSuccess = () => {
  inputValue.value = ''
  loading.value = false
  isBack.value = true
  currentGroupId.value = -1
  dialogueList.value = []
  dialogueApi.value = []

}

onMounted(() => {
  emitter.on('changeGroupId', (groupId) => {
    currentGroupId.value = groupId as number
    getMessagesByGroupId()
  })
  emitter.on('loginSuccess', () => {
    loginSuccess()
  })
  emitter.on('logoutSuccess', () => {
    logoutSuccess()
  })
  esConnect()
})
</script>

<template>
  <div class="w-full py-4 px-6 max-sm:py-0 max-sm:px-2 relative">
    <div class="w-5/6 m-auto h-full pt-3 max-sm:w-full sm:pb-24 max-sm:pb-0 relative">
      <!-- 对话部分 -->
      <n-scrollbar style="max-height: inherit" ref="contentScrollbar" :on-scroll="handleContentScroll"
        :x-scrollable="false">
        <div class="pb-3 h-full max-sm:pb-14" ref="contentScrollbarEle">
          <div style="height: 100%" class="w-full flex justify-center items-center flex-col"
            v-show="currentGroupId < 0">
            <img class="w-1/2 max-sm:w-72" src="../../assets/xuexi.svg" />
            <p class="text-center text-gray-600">—— 开始提问吧 ——</p>
          </div>
          <div v-for="(item, index) in dialogueList" class="w-full h-fit text-purple-600 text-base">
            <div class="h-fit flex items-start mt-3" :class="item.role == 'user' ? 'self_ask' : ''">
              <div class="avatar">
                <n-avatar :style="{ backgroundColor: 'transparent' }" round :size="screenWidth <= 480 ? 28 : 44"
                  :src="item.role == 'user' ? globalStore.userInfo?.userAvatar : getAssetsImages('logo.svg')" />
              </div>
              <div style="font-family: 'Open Sans', 'Seravek', 'Segoe UI', 'Verdana', 'PingFang SC';"
                :class="['xz-content' + index, index % 2 == 1 ? 'text-black' : '']" id="xz_element" class="max-w-[70%] max-sm:max-w-[81%] mx-3 min-h-[48px] bg-white p-2
                 rounded-md test whitespace-normal overflow-x-auto" v-html="item.content">
              </div>
            </div>
          </div>
          <div v-show="!isBack && currentGroupId >= 0" class="flex mt-3">
            <div class="avatar">
              <n-avatar :style="{ backgroundColor: 'transparent' }" round :size="screenWidth <= 480 ? 32 : 44"
                :src="getAssetsImages('logo.svg')" />
            </div>
            <div style="font-family: 'Open Sans', 'Seravek', 'Segoe UI', 'Verdana', 'PingFang SC';" class="max-w-[70%] max-sm:max-w-full mx-3 min-h-[48px] bg-white p-2 rounded-md test whitespace-normal
               text-black flex items-center justify-center">
              <n-spin size="small" />
            </div>
          </div>
        </div>
      </n-scrollbar>
      <!-- 输入部分 -->
      <div class="w-full h-min mt-3 max-sm:hidden absolute bottom-0 left-0 right-0">
        <!-- <div class="w-full h-full bg-white absolute top-0 left-0 opacity-30 blur-sm"></div> -->
        <div class="w-full max-w-2xl m-auto relative">
          <n-input v-model:value="inputValue" type="textarea" placeholder="输入问题，提问吧~" :maxlength="2000" />
          <n-button type="primary" class="absolute bottom-2 right-2" size="small" @click="sendMessage"
            :loading="loading">{{ loading ? '响应中...' : '发送🛩' }}</n-button>
        </div>
      </div>
    </div>
    <!-- 输入部分移动端 -->
    <div class="sm:hidden">
      <div class="w-full m-auto fixed bottom-0 left-0 right-0">
        <n-input :rows="1" class="pr-16" v-model:value="inputValue" type="textarea" placeholder="输入问题，提问吧~"
          :maxlength="2000" />
        <n-button type="primary" class="absolute bottom-0 right-0" @click="sendMessage" :loading="loading">{{ loading ?
        '响应中...' : '发送🛩' }}</n-button>
      </div>
    </div>
  </div>
</template>

<style lang='scss'>
#xz_element {}

.self_ask {
  flex-direction: row-reverse;
}

li {
  margin-left: 20px;
}

.test {
  &::before {
    display: inline-block;
    height: 0px;
  }
}
</style>