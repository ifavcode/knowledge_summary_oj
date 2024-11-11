<script setup lang='ts'>
import { useGlobalStore } from '~/store/globalStore';
import { Add } from '@vicons/ionicons5'
import { request } from '~/utils/request';
//@ts-ignore
import Cookies from 'js-cookie'
import { useMessage } from 'naive-ui';
window.$message = useMessage()
const navList = ref<NavBar[]>([
  {
    id: 0,
    name: '知识库'
  },
  {
    id: 3,
    name: '题库'
  },
  // {
  //   id: 1,
  //   name: '小知问答'
  // },
  // {
  //   id: 2,
  //   name: 'AI作图'
  // }
  {
    id: 4,
    name: '案例'
  },
])
const navListMap: any = {
  '/xz_ask': 2,
  '/home': 0,
  '/': 0,
  '/ai_image': 3,
  '/profile/files': -1
}
const activeNavIndex = ref<number>(0)
const userAvatarPopupShow = ref<boolean>(false)
const globalStore = useGlobalStore()
const avatarPopup = ref()
const router = useRouter()
const route = useRoute()
const navbarAvatarRef = ref()

onClickOutside(avatarPopup, () => {
  userAvatarPopupShow.value = false
}, {
  ignore: [navbarAvatarRef]
})

const handleClickAvatar = (): void => {
  if (globalStore.authorization != '') {
    userAvatarPopupShow.value = !userAvatarPopupShow.value
  } else {
    globalStore.isLoginElementShow = true
  }
}

const handlePubBtn = () => {
  router.push('/publish_article')
  activeNavIndex.value = -1
}

const message = useMessage()
async function getUserInfo() {
  const res: AjaxResult<User> = await request('/api/user/info')
  if (res) {
    globalStore.setUserInfo(res.data)
    //收集用户权限
    res.data?.role.forEach((v: Role) => {
      globalStore.userAuth.push(v.roleAuth as string)
      if (v.roleAuth == '*') globalStore.isAdmin = true
    })
  } else {
    message.info('登录失效，请重新登陆')
    globalStore.isLoginElementShow = true
    Cookies.remove('authorization')
    globalStore.authorization = ''
  }
}

const handleClickLogo = () => {
  router.push('/home')
  activeNavIndex.value = 0
}

const handleClickNavBar = (index: number) => {
  if (activeNavIndex.value == index) return
  activeNavIndex.value = index
  if (navList.value[index].name === '知识库') {
    router.push('/home')
  } else if (navList.value[index].name === '小知问答') {
    router.push('/xz_ask')
  } else if (navList.value[index].name === 'AI作图') {
    router.push('/ai_image')
  } else if (navList.value[index].name === '题库') {
    router.push('/questions')
  }
}

onBeforeMount(() => {
  const str = Cookies.get('authorization')
  if (navListMap[route.path]) {
    activeNavIndex.value = navListMap[route.path]
  }
  if (str) {
    globalStore.authorization = str
    getUserInfo()
  } else {
    message.info('登录失效，请重新登陆')
    // globalStore.isLoginElementShow = true
  }
})

onMounted(() => {
  let path = window.location.hash
  if (path.includes('/home')) {
    activeNavIndex.value = 0
  } else if (path.includes('/xz_ask')) {
    activeNavIndex.value = 2
  } else if (path.includes('/ai_image')) {
    activeNavIndex.value = 3
  } else if (path.includes('/questions')) {
    activeNavIndex.value = 1
  }
})
</script>

<template>
  <n-affix :top="0" :trigger-top="0" class="w-full z-50">
    <div class="total_nav_wrap bg-white">
      <div class="total_nav">
        <div class="nav_left">
          <logo fontSize="24px" marginRight="50px" @click="handleClickLogo" style="cursor: pointer;" />
          <div class="nav_list">
            <p v-for="(item, index) in navList" :class="activeNavIndex == index ? 'nav_active' : ''"
              @click="handleClickNavBar(index)">
              {{ item.name }}
            </p>
          </div>

          <SearchPage v-if="!$route.path.includes('/search')" />
        </div>
        <div class="nav_right">
          <div class="pub_btn" v-if="!route.path.includes('/publish_article')">
            <n-button style="min-width:70px;" type="primary" @click="handlePubBtn">
              <template #icon>
                <n-icon>
                  <Add />
                </n-icon>
                <span style="font-size: 14px;">发布</span>
              </template>
            </n-button>
          </div>
          <div ref="navbarAvatarRef" class="flex items-center">
            <n-avatar style="cursor: pointer;" v-if="globalStore.authorization != ''" class="avatar" round size="small"
              :src="globalStore.userInfo?.userAvatar" @click="handleClickAvatar" />
            <n-avatar style="cursor: pointer;" v-else round size="small" class="avatar" @click="handleClickAvatar">
              <i class="iconfont" style="color:rgb(86, 86, 86)">&#xe62e;</i>
            </n-avatar>
          </div>
        </div>
        <Transition appear name="avatar-box">
          <div class="avatar_popup" v-show="userAvatarPopupShow" ref="avatarPopup">
            <UserAvatarPopup @closePopupShow="userAvatarPopupShow = false" />
          </div>
        </Transition>
      </div>
    </div>
  </n-affix>
</template>

<style lang='scss' scoped>
.total_nav_wrap {
  width: 100%;
  height: 50px;
  box-shadow: 0px 1px 3px #0000000a, 0px 2px 8px #00000014;
  display: flex;
  justify-content: center;
  align-items: center;

  @media screen and (max-width:480px) {
    display: none;
  }

  .total_nav {
    width: 100%;
    height: 100%;
    max-width: 1200px;
    display: flex;
    align-items: center;
    position: relative;
    transition: all .5s;
    white-space: nowrap;

    @media screen and (min-width: 700px) and (max-width: 1200px) {
      padding: 0 60px;
    }

    @media screen and (max-width: 700px) {
      padding: 0 40px;
    }

    .nav_left {
      flex: 2;
      display: flex;

      .nav_list {

        p {
          cursor: pointer;
          margin-right: 50px;
          font-size: 16px;
          display: inline-block;
          line-height: 50px;
        }

        .nav_active {
          border-bottom: #3F95FD solid 3px;
        }
      }
    }

    .nav_right {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: flex-end;

      .avatar {
        cursor: pointer;
        margin-left: 20px;
      }
    }

    .avatar_popup {
      position: absolute;
      right: 0;
      top: 0;
      transform: translateY(40px);
      z-index: 1000;
    }
  }
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
</style>