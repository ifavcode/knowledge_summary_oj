<script setup lang='ts'>
import { useGlobalStore } from '~/store/globalStore';
//@ts-ignore
import Cookies from 'js-cookie'
import { useMessage } from 'naive-ui';
import { request } from '~/utils/request';
import { emitter } from '~/utils/index';
import router from '~/router';
import { useAxios } from '@vueuse/integrations/useAxios'
import { client } from '~/utils/request'
import { TrendingUp } from '@vicons/ionicons5'

const emit = defineEmits(['closePopupShow'])
const message = useMessage()
const globalStore = useGlobalStore()
const settingShow = ref(false)
const codeSize = ref(14)
const codeLine = ref(true)
//点击图像弹出框功能列表
const chooseList = ref<ChooseType[]>([
  {
    icon: '&#xe618;',
    funcFlag: 'myArticles',
    targetPath: '',
    displayName: '我的文章'
  },
  {
    icon: '&#xe869;',
    funcFlag: 'myFiles',
    targetPath: '',
    displayName: '我的文件'
  },
  {
    icon: '&#xe603;',
    funcFlag: 'setting',
    targetPath: '',
    displayName: '设置'
  },
  {
    icon: '&#xe612;',
    funcFlag: 'logout',
    targetPath: '',
    displayName: '退出'
  },
])

const codeStyle = ref('xcode')
const codeStyleList = ref<any>([
])

const arr = ['abap', 'algol', 'algol_nu', 'arduino', 'autumn', 'borland', 'bw', 'colorful', 'dracula', 'emacs', 'friendly',
  'fruity', 'github', 'igor', 'lovelace', 'manni', 'monokai', 'monokailight', 'murphy', 'native', 'paraiso-dark',
  'paraiso-light', 'pastie', 'perldoc', 'pygments', 'rainbow_dash', 'rrt', 'solarized-dark', 'solarized-dark256',
  'solarized-light', 'swapoff', 'tango', 'trac', 'vim', 'vs', 'xcode']
arr.forEach(v => {
  codeStyleList.value.push({
    label: v,
    value: v
  })
})

const chooseItemClick = async (item: ChooseType): Promise<void> => {
  if (item.funcFlag == 'logout') {
    if (await handleLogout()) {
      emit('closePopupShow')
      setTimeout(() => {
        Cookies.remove('authorization')
        globalStore.setUserInfo(null)
        globalStore.authorization = ''
        globalStore.userAuth = []
        globalStore.isAdmin = false
        message.success('已退出')
        emitter.emit('logoutSuccess')
      }, 200);
    }
  }
  if (item.funcFlag == 'myArticles') {
    emit('closePopupShow')
    router.push(`/profile/${globalStore.userInfo?.userId}/?is_article=1`)
  }
  if (item.funcFlag == 'myFiles') {
    emit('closePopupShow')
    router.push(`/profile/files`)
  }
  if (item.funcFlag == 'setting') {
    settingShow.value = !settingShow.value
  }
}

const handleClickAvatar = () => {
  router.push('/profile/' + globalStore.userInfo?.userId)
  emit('closePopupShow')
}

const handleLogout = async () => {
  const res: boolean = await request('/api/user/logout', {
    method: 'post'
  })
  return res;
}

const formatTooltip = (value: number) => {
  return `${value}px`
}

let key: NodeJS.Timeout
const codeSizeChange = (value: number) => {
  codeSize.value = value
  document.getElementsByTagName("body")[0].style.setProperty("--code-font-size", value + 'px');
  clearTimeout(key)
  key = setTimeout(() => {
    //保存至缓存
    localStorage.setItem('codeFontSize', value.toString())
  }, 100);
}

let key2: NodeJS.Timeout
const codeStyleChange = (value: string) => {
  codeStyle.value = value
  emitter.emit('codeStyleChange', {
    style: value,
    lineNumber: codeLine.value
  })
  clearTimeout(key2)
  key2 = setTimeout(() => {
    //保存至缓存
    localStorage.setItem('codeStyleChange', value.toString())
  }, 100);
}

let key3: NodeJS.Timeout
const handleCodeLineChange = (value: boolean) => {
  codeLine.value = value
  emitter.emit('codeStyleChange', {
    style: codeStyle.value,
    lineNumber: value
  })
  clearTimeout(key3)
  key3 = setTimeout(() => {
    //保存至缓存
    localStorage.setItem('codeLineChange', value.toString())
  }, 100);
}

//获取浏览量,认可量,评论量
const { data: vlcData }: { data: Ref<AjaxResult<any> | undefined> } = useAxios('/api/user/articles/data', {}, client)

onMounted(() => {
  const localCodeFontSize: string | null = localStorage.getItem('codeFontSize')
  if (localCodeFontSize != null) {
    codeSize.value = parseInt(localCodeFontSize)
    codeSizeChange(codeSize.value)
  }
  const localCodeStyleChange: string | null = localStorage.getItem('codeStyleChange')
  if (codeStyleChange != null) {
    codeStyle.value = localCodeStyleChange as string
  }
  const localCodeLineChange: string | null = localStorage.getItem('codeLineChange')
  if (localCodeLineChange != null) {
    codeLine.value = localCodeLineChange == 'true'
  }
})
</script>
 
<template>
  <div class="user_avatar_popup">
    <div class="user_info">
      <div class="avatar" @click="handleClickAvatar">
        <n-avatar round :size="60" :src="globalStore.userInfo?.userAvatar" />
      </div>
      <div class="slide">
        <div class="cursor-pointer text-lg" :style="{ color: globalStore.isAdmin ? '#FB7299' : '' }"
          @click="handleClickAvatar">{{ globalStore.userInfo?.nickName || '未命名' }}
          <span v-if="globalStore.isAdmin"
            class="inline-block text-xs bg-user-admin-color rounded-lg text-white px-2 leading-5">管理员</span>
        </div>
        <div class="desc">{{ globalStore.userInfo?.userDesc }}</div>
      </div>
    </div>

    <div class="data_box">
      <div class="data_item">
        <div class="icon">
          <i class="iconfont" style="font-size: 30px;">&#xe7d0;</i>
        </div>
        <div class="data_num">
          {{ vlcData?.data.views || '...' }}
        </div>
      </div>

      <div class="data_item">
        <div class="icon">
          <i class="iconfont" style="font-size: 30px;">&#xe8c3;</i>
        </div>
        <div class="data_num">
          {{ vlcData?.data.likes || '...' }}
        </div>
      </div>

      <div class="data_item">
        <div class="icon">
          <i class="iconfont" style="font-size: 27px;">&#xe687;</i>
        </div>
        <div class="data_num">
          {{ vlcData?.data.comments || '...' }}
        </div>
      </div>
    </div>

    <div class="choose_wrap line-clamp-1">
      <div class="choose_item" v-for="item in chooseList" @click="chooseItemClick(item)">
        <i class="iconfont text-xl text-black" v-html="item.icon"></i>
        <div class="ml-2">{{ item.displayName }}</div>
        <n-collapse-transition :show="settingShow && item.funcFlag === 'setting'" class="inline-block py-2">
          <div class="flex items-center" @click.stop>
            <span class="text-xs text-gray-500">
              代码字体大小:
            </span>
            <div class="w-full px-3">
              <n-slider v-model:value="codeSize" :step="1" :format-tooltip="formatTooltip" :min="12" :max="22"
                :on-update:value="codeSizeChange">
                <template #thumb>
                  <n-icon-wrapper :size="24" :border-radius="12">
                    <n-icon :size="18" :component="TrendingUp" />
                  </n-icon-wrapper>
                </template>
              </n-slider>
            </div>
          </div>
          <div class="flex mt-2 items-center" @click.stop>
            <span class="text-xs text-gray-500">
              代码色彩风格:
            </span>
            <div class="w-full px-3">
              <n-select v-model:value="codeStyle" size="tiny" :options="codeStyleList"
                :on-update:value="codeStyleChange" />
            </div>
          </div>
          <div class="flex mt-2 items-center" @click.stop>
            <span class="text-xs text-gray-500">
              代码显示行号:
            </span>
            <div class="w-full px-3">
              <n-switch v-model:value="codeLine" size="small" @update:value="handleCodeLineChange" />
            </div>
          </div>
        </n-collapse-transition>
      </div>
    </div>

  </div>
</template>
 
<style lang='scss' scoped>
.user_avatar_popup {
  width: 300px;
  height: fit-content;
  border: solid 1px #e4e4e4;
  box-shadow: 0px 1px 3px #0000000a, 0px 2px 8px #00000014;
  border-radius: 10px;
  padding: 20px;
  background: #fff;

  .user_info {
    display: flex;

    .avatar {
      margin-right: 10px;
      cursor: pointer;
    }

    .slide {
      h1 {
        font-size: 20px;
      }

      .desc {
        width: 180px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #aaa;
        font-size: 14px;
      }
    }
  }

  .data_box {
    display: flex;
    justify-content: space-around;
    margin-top: 5px;

    .data_item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .data_num {
        transform: translateY(-8px);
        font-size: 13px;
        color: #686868;
      }
    }
  }

  .choose_wrap {

    .choose_item {
      width: 100%;
      min-height: 36px;
      color: rgb(36, 36, 36);
      border-radius: 10px;
      cursor: pointer;
      display: flex;
      align-items: center;
      padding-left: 10px;
      flex-wrap: wrap;

      &:hover {
        background: #F7F7F8;
      }
    }
  }
}
</style>