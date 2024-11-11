<script setup lang='ts'>
import { useGlobalStore } from '~/store/globalStore';

const router = useRouter()
const activeNavName = ref<string>('home')
const globalStore = useGlobalStore()
const avatarPopup = ref()
const navbarShow = ref(false)

const chooseList = ref<ChooseType[]>([
  {
    icon: '&#xea3f;',
    funcFlag: 'home',
    targetPath: '/home',
    displayName: '知识库'
  },
  {
    icon: '&#xe64b;',
    funcFlag: 'xz_ask',
    targetPath: '/xz_ask',
    displayName: '小知问答'
  },
  {
    icon: '&#xe628;',
    funcFlag: 'ai_image',
    targetPath: '/ai_image',
    displayName: 'AI作图'
  },
  {
    icon: '&#xe652;',
    funcFlag: 'search',
    targetPath: '/search?keyWord=',
    displayName: '搜索'
  },
  {
    icon: '&#xe618;',
    funcFlag: 'myArticles',
    targetPath: `/profile/`,
    displayName: '我的文章'
  },
  {
    icon: '&#xe621;',
    funcFlag: 'publishArticle',
    targetPath: '/publish_article',
    displayName: '发布文章'
  },
])

const handleClickLogo = () => {
  router.push('/home')
}

const userAvatarPopupShow = ref<boolean>(false)
const handleClickAvatar = (): void => {
  if (globalStore.authorization != '') {
    userAvatarPopupShow.value = !userAvatarPopupShow.value
  } else {
    globalStore.isLoginElementShow = true
  }
}

const handleClickNavBarIcon = () => {
  navbarShow.value = true
}

const handleClickNavBar = (item: ChooseType, _index: number) => {
  if (item.funcFlag === 'myArticles') {
    if (!globalStore.userInfo?.userId) return
    router.push(item.targetPath + `${globalStore.userInfo?.userId}?is_article=1`)
  } else {
    router.push(item.targetPath)
  }
  activeNavName.value = item.funcFlag
  setTimeout(() => {
    navbarShow.value = false
  }, 300);
}

onClickOutside(avatarPopup, () => {
  userAvatarPopupShow.value = false
})

onMounted(() => {
  let path = window.location.hash
  if (path.includes('/home')) {
    activeNavName.value = 'home'
  } else if (path.includes('/search')) {
    activeNavName.value = 'search'
  } else if (path.includes('/publish_article')) {
    activeNavName.value = 'publishArticle'
  } else if (path.includes('/xz_ask')) {
    activeNavName.value = 'xz_ask'
  } else if (path.includes('/ai_image')) {
    activeNavName.value = 'ai_image'
  }
})

</script>

<template>
  <n-affix :top="0" :trigger-top="48" class="w-full z-50">
    <div class="navbar_mobile_wrap bg-white">
      <div class="w-16 h-full flex items-center bg-primary-color justify-center">
        <div @click="handleClickNavBarIcon">
          <i class="iconfont text-2xl text-white">&#xe604;</i>
        </div>
      </div>
      <div class="w-full flex justify-center">
        <logo fontSize="20px" width="36px" marginRight="0px" @click="handleClickLogo" style="cursor: pointer;" />
      </div>
      <div class="nav_list">
      </div>

      <div class="nav_right w-16 h-full flex items-center justify-center">
        <!-- <div class="pub_btn hidden">
        <n-button color="#3f95fd" @click="handlePubBtn" style="width: 60px;">
          <template #icon>
            <n-icon>
              <Add />
            </n-icon>
            <span style="font-size: 14px;">发布</span>
          </template>
</n-button>
</div> -->
        <n-avatar style="cursor: pointer;" v-if="globalStore.authorization != ''" class="avatar" round size="medium"
          :src="globalStore.userInfo?.userAvatar" @click="handleClickAvatar" />
        <n-avatar style="cursor: pointer;" v-else round size="medium" class="avatar" @click="handleClickAvatar">
          <i class="iconfont" style="color:rgb(86, 86, 86)">&#xe62e;</i>
        </n-avatar>
      </div>

      <Transition appear name="avatar-box">
        <div class="avatar_popup" v-show="userAvatarPopupShow" ref="avatarPopup">
          <UserAvatarPopup @closePopupShow="userAvatarPopupShow = false" />
        </div>
      </Transition>

      <n-drawer v-model:show="navbarShow" :width="300" placement="left">
        <n-drawer-content>
          <logo fontSize="20px" width="36px" marginRight="0px" style="cursor: pointer;" />
          <div class="mt-3 border-t">
            <!-- 每一项 -->
            <div class="flex items-center mt-3 py-1 px-3 ease-in-out duration-200 relative"
              v-for="(item, index) in chooseList" :class="activeNavName == item.funcFlag ? 'active_nav' : ''"
              @click="handleClickNavBar(item, index)">
              <i class="iconfont text-xl" v-html="item.icon"></i>
              <span class="text-base ml-2">{{ item.displayName }}</span>
              <Transition name="b-bar" appear>
                <div v-show="activeNavName == item.funcFlag"
                  class="absolute w-20 bg-primary-color h-1 bottom-0 rounded-lg">
                </div>
              </Transition>
            </div>
          </div>
        </n-drawer-content>
      </n-drawer>
    </div>
  </n-affix>
</template>

<style lang='scss' scoped>
.navbar_mobile_wrap {
  @media screen and (min-width:480px) {
    display: none;
  }

  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  box-shadow: 0px 1px 3px #0000000a,
  0px 2px 8px #00000014;

  .nav_right {

    .avatar {
      cursor: pointer;
    }
  }

  .nav_list {

    p {
      cursor: pointer;
      margin-right: 30px;
      font-size: 16px;
      display: inline-block;
      line-height: 50px;
    }

    .nav_active {
      border-bottom: #3f95fd solid 3px;
    }
  }

  .avatar_popup {
    position: absolute;
    right: 0;
    top: 0;
    transform: translateY(40px);
    z-index: 100;
  }
}

.active_nav {
  position: relative;
  color: $primaryColor;
  font-weight: 500;

  /* ::before {
    content: '';
    position: absolute;
    bottom: -2px;
    width: 50px;
    background: $primaryColor ;
    height: 6px;
    border-radius: 10px;
  } */
}
</style>

<style lang="scss" scoped>
.avatar-box-enter-active {
  animation: fadeIn .3s forwards;
}

.avatar-box-leave-active {
  animation: fadeIn .2s reverse forwards;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
    transform: translateY(32px);
  }

  100% {
    opacity: 1;
    transform: translateY(40px);
  }
}

.b-bar-enter-active {
  animation: bBar .2s forwards;
}

.b-bar-leave-active {
  animation: bBar .2s reverse forwards;
}

@keyframes bBar {
  0% {
    @apply w-0;
  }

  100% {
    @apply w-20;
  }
}
</style>