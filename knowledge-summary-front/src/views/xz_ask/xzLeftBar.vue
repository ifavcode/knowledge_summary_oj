<script setup lang='ts'>
import TypeIt from "typeit";
import { request } from "~/utils/request";
import { emitter } from '~/utils/index'
import { useMessage } from "naive-ui";
import { useGlobalStore } from "~/store/globalStore";

const message = useMessage()
const globalStore = useGlobalStore()
const emit = defineEmits(['szContentScrollDown'])
const currentSelectGroupId = ref(-1)
const diaGroupList = ref<XzGroup[]>([
])
const getAllGroup = async () => {
  const res: AjaxResult<XzGroup[]> = await request('/flux/dialogue/user/group/get')
  diaGroupList.value = res.data as XzGroup[]
}

const handleClickGroup = (index: number) => {
  isLeftBarShow.value = false
  if (index >= 0 && currentSelectGroupId.value != diaGroupList.value[index].groupId || index < 0) {
    currentSelectGroupId.value = index >= 0 ? diaGroupList.value[index].groupId : -1
    emitter.emit('changeGroupId', currentSelectGroupId.value)
  }
}

const handleDeleteClick = async (index: number) => {
  const res: boolean = await request('/flux/dialogue/user/group/delete/' + diaGroupList.value[index].groupId, {
    method: 'post'
  })
  if (res) {
    message.success('删除成功')
    getAllGroup()
  } else {
    message.error('服务器异常')
  }
}

const initFunc = () => {
  getAllGroup()
}

const logoutSuccess = () => {
  currentSelectGroupId.value = -1
  diaGroupList.value = []
}

onMounted(() => {
  initFunc()
  nextTick(() => {
    //你好，我叫小知~
    //作为你的智能伙伴，我既能写文案、想点子，又能陪你聊天、答疑解惑。输入你的问题，我们开始对话吧！
    new (TypeIt as any)(".xz-hello", {
      waitUntilVisible: false,
      afterComplete: function (instance: any) {
        instance.destroy();
      },
    })
      .type('<span style="font-size:18px;">你好，我叫小知~</span><br/>')
      .pause(1500)
      .type('<span style="font-size:16px;">作为你的智能伙伴，我既能写文案、想点子，又能陪你聊天、答疑解惑。输入你的问题，我们开始对话吧！</span>')
      .go();
  })
  emitter.on('refreshGroup', async (groupId) => {
    await getAllGroup()
    currentSelectGroupId.value = groupId as number
  })
  emitter.on('loginSuccess', () => {
    initFunc()
  })
  emitter.on('logoutSuccess', () => {
    logoutSuccess()
  })
})

/**
 * 移动端侧边列表
 */

const isLeftBarShow = ref(false)
let firstShow = true

const handleClickHistoryIcon = () => {
  isLeftBarShow.value = true
  if (isLeftBarShow.value) {
    nextTick(() => {
      new (TypeIt as any)(".xz-hello2", {
        afterComplete: function (instance: any) {
          instance.destroy();
        },
        speed: firstShow ? 100 : 5
      })
        .type('<span style="font-size:18px;">你好，我叫小知~</span><br/>')
        .pause(firstShow ? 1500 : 0)
        .type('<span style="font-size:16px;">作为你的智能伙伴，我既能写文案、想点子，又能陪你聊天、答疑解惑。输入你的问题，我们开始对话吧！</span>')
        .go();
      firstShow = false
    })
  }
}

const handleClickScrollDown = () => {
  emit('szContentScrollDown')
}

//AI模型选择部分
const aiModelOptions = [
  {
    label: '对话Chat',
    value: '',
    disabled: true
  },
  {
    label: "ERNIE-Speed-128K",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k',
  },
  {
    label: "ERNIE-4.0-8K",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro',
  },
  {
    label: "ERNIE-4.0-8K-Latest",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.0-8k-latest',
  },
  {
    label: "ERNIE-Lite-8K-0922",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant',
  },
  {
    label: "EB-turbo-AppBuilder专用版",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ai_apaas',
  },
  {
    label: "Llama-2-70b-chat",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/llama_2_70b',
  },
  {
    label: "BLOOMZ-7B",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/bloomz_7b1',
  },
  {
    label: "Yi-34B-Chat",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/yi_34b_chat',
  },
  {
    label: "Mixtral-8x7B-Instruct",
    value: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/mixtral_8x7b_instruct',
  },
]

</script>

<template>
  <div class="bg-gray-200 sm:flex sm:flex-col">
    <p class="fixed top-16 left-3 max-sm:block sm:hidden z-20">
      <n-button size="small" secondary strong circle @click="handleClickHistoryIcon">
        <i class="iconfont text-lg">&#xe659;</i>
      </n-button>
    </p>
    <p class="fixed bottom-14 left-3 max-sm:block sm:hidden z-20">
      <n-button v-show="currentSelectGroupId != -1" class="mt-3" size="small" secondary strong circle
        @click="handleClickScrollDown">
        <i class="iconfont text-lg font-bold">&#xe62b;</i>
      </n-button>
    </p>
    <div class="p-3">
      <p>选择AI模型</p>
      <div class="mt-1">
        <n-select v-model:value="globalStore.aiUrl" :options="aiModelOptions" />
      </div>
    </div>
    <n-scrollbar style="max-height: inherit;" class="max-sm:hidden">
      <div class="w-80 px-3 pb-4 relative">
        <!-- 上部分 -->
        <div class="">
          <div class="h-26">
            <div class="h-24">
              <p class="xz-hello text-pink-500"></p>
            </div>
            <div class="mt-3 w-full animate-pulse">
              <n-button class="w-full" color="#E44598" @click="handleClickGroup(-1)">
                <span>创建提问</span>
              </n-button>
            </div>
          </div>
        </div>
        <!-- 下半部分 -->
        <div class="mt-3">
          <h2 class="text-xl text-purple-800">历史提问</h2>
          <p class="text-gray-500 mt-1" v-show="diaGroupList.length === 0">还没有问过哦，快去提问吧~</p>
          <div>
            <n-scrollbar style="max-height: inherit">
              <transition appear name="xz-group" v-for="(item, index) in diaGroupList">
                <div @click="handleClickGroup(index)" class="group hover:bg-purple-600 hover:text-white rounded-md p-2 :not:first:mt-3 [&:nth-child(n+1)]:mt-3
               text-purple-600 line-clamp-3 cursor-pointer flex justify-between"
                  :class="[currentSelectGroupId == diaGroupList[index].groupId ? 'bg-purple-600 text-white' : '']">
                  <span>{{ item.groupTitle }}</span>
                  <i class="iconfont hidden group-hover:inline-block mr-1 hover:text-red-500"
                    @click.stop="handleDeleteClick(index)">&#xeaf2;</i>
                </div>
              </transition>
            </n-scrollbar>
          </div>
        </div>
      </div>
    </n-scrollbar>
    <n-drawer v-model:show="isLeftBarShow" :width="300" placement="left">
      <n-drawer-content title="历史提问">
        <div class="w-full">
          <!-- 上部分 -->
          <div class="h-36">
            <p class="xz-hello2 text-pink-500"></p>
          </div>
          <!-- 下半部分 -->
          <div class="mt-1 w-full animate-pulse">
            <n-button class="w-full" color="#E44598" @click="handleClickGroup(-1)">
              <span>创建提问</span>
            </n-button>
          </div>
          <div class="h-5/6 overflow-y-auto mt-3">
            <p class="text-gray-500 mt-1" v-show="diaGroupList.length === 0">还没有问过哦，快去提问吧~</p>
            <div>
              <transition appear name="xz-group" v-for="(item, index) in diaGroupList">
                <div @click="handleClickGroup(index)" class="group hover:bg-purple-600 hover:text-white rounded-md p-2 :not:first:mt-3 [&:nth-child(n+1)]:mt-1
               text-purple-600 line-clamp-3 cursor-pointer flex justify-between"
                  :class="[currentSelectGroupId == diaGroupList[index].groupId ? 'bg-purple-600 text-white' : '']">
                  <span>{{ item.groupTitle }}</span>
                  <i class="iconfont hidden group-hover:inline-block mr-1 hover:text-red-500"
                    @click.stop="handleDeleteClick(index)">&#xeaf2;</i>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style lang='scss' scoped>
.xz-group-enter-active {
  animation: xz-group-animation 1s ease-in-out forwards;
}

.xz-group-level-active {
  animation: xz-group-animation 1s ease-in-out forwards reverse;
}

@keyframes xz-group-animation {
  0% {
    transform: translateX(-16px);
    opacity: 0;
  }

  100% {
    transform: translateX(0px);
    opacity: 1;
  }
}
</style>