<script setup lang='ts'>
import { useGlobalStore } from '~/store/globalStore';
import { request } from '~/utils/request';
import { getAssetsImages, publishUserisAdmin } from '~/utils/index'
import moment from 'moment'
import 'moment/dist/locale/zh-cn'
import { UploadFileInfo, useMessage } from 'naive-ui';
import { useAxios } from '@vueuse/integrations/useAxios'
import { client } from '~/utils/request'


moment.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userInfo = ref<userInfo>()
const globalStore = useGlobalStore()
const message = useMessage()
const articlesManageRef = ref()

const isEdit = ref(false)
const isArticle = ref(false)

const getUserById = async () => {
  const res: AjaxResult<userInfo> = await request(`/api/user/info/${route.params.userId}`)
  if (!res.data) router.replace('/not_found/user')
  userInfo.value = res.data;
  console.log(userInfo.value);
  (globalStore.userInfo as User).userAvatar = userInfo.value?.userAvatar as string
}

const userViews = ref<UserViews[]>([])
const getUserViews = async () => {
  const res: AjaxResult<UserViews[]> = await request('/api/user/views', {
    params: {
      userId: route.params.userId || globalStore.userInfo?.userId
    }
  })
  if (res) {
    userViews.value = (res.data as UserViews[]).filter(v => v.articles != null)
  }
}

const userLikes = ref<Articles[]>([])
const getUserLikes = async () => {
  const res: AjaxResult<Articles[]> = await request('/api/user/likes', {
    params: {
      userId: route.params.userId || globalStore.userInfo?.userId
    }
  })
  userLikes.value = res.data as Articles[]
}

const handleClickViewItem = (item: UserViews | Articles) => {
  window.open(window.location.origin + '#/article/' + item.articleId)
}

function formatDate(createTime: string) {
  if (new Date().getTime() - new Date(createTime).getTime() <= 1000 * 60 * 60 * 24 * 7) {
    return moment(createTime, 'YYYY/MM/DD HH:mm:ss').calendar()
  } else {
    return moment(createTime, 'YYYY/MM/DD HH:mm:ss').format('l')
  }
}

const userArticlesInfo = ref<UserArticlesData>()
const getUserArticlesInfo = async () => {
  userArticlesInfo.value = {}
  const res: AjaxResult<UserArticlesData> = await request('/api/user/articles/info', {
    params: {
      userId: route.params.userId || globalStore.userInfo?.userId
    }
  })
  userArticlesInfo.value = res.data as UserArticlesData
}

//用户信息更新了
const userInfoUpdate = () => {
  isEdit.value = false
  getUserById()
}

const initPageLoading = () => {
  getUserById()
  getUserViews()
  getUserLikes()
  getUserArticlesInfo()
  if (isArticle.value) {
    articlesManageRef.value.getUserPublishArticlesId()
  }
}

const handleFinish = ({ file, event }: { file: UploadFileInfo, event?: any }) => {
  const res = JSON.parse(event.target.response)
  if (res) {
    console.log(res);

    message.success('修改头像成功')
    getUserById()
  } else {
    message.error('网络异常')
  }
  return file
}

const toggleShowArticle = () => {
  isArticle.value = !isArticle.value;
  if (isArticle.value) {
    // articlesManageRef.value.getUserPublishArticlesId()
    if (!route.query['is_article']) {
      router.replace(route.fullPath + '?is_article=1')
    }
  }
}

watch(route, () => {
  if (route.params.userId != globalStore.userInfo?.userId.toString()) {
    isEdit.value = false
  }
  initPageLoading()
  articlesManageRef.value.reload()
})

onMounted(() => {
  if (route.query.is_article == '1') {
    isArticle.value = true
  }
})

//获取浏览量,认可量,评论量
const { data: vlcData }: { data: Ref<AjaxResult<any> | undefined> } = useAxios('/api/user/articles/data', {
  params: {
    userId: route.params.userId
  }
}, client)

onBeforeMount(() => {
  initPageLoading()
})
</script>

<template>
  <div class="userinfo_wrap sm:mt-16 max-sm:mt-16 ml-auto mr-auto">
    <div class="user_wrap">
      <n-upload v-if="globalStore.userInfo?.userId.toString() == route.params.userId" style="width: 80px;height: 80px;"
        action="/api/user/avatar/edit" :headers="{
        'Authorization': globalStore.authorization
      }" @finish="handleFinish" :file-list-style="{ display: 'none' }">
        <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;" :arrow-style="{ background: '#2A2A2A' }">
          <template #trigger>
            <div class="user_img">
              <n-avatar :size="80" :src="userInfo?.userAvatar"
                fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
            </div>
          </template>
          <span>修改头像</span>
        </n-popover>
      </n-upload>
      <div class="relative" v-else>
        <n-avatar :size="80" :src="userInfo?.userAvatar"
          fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
      </div>
      <div class="right">
        <div class="nick_name flex items-center"
          :style="{ color: publishUserisAdmin(userInfo?.roles as Role[]) ? '#fb7299' : '' }">
          <span>
            {{ userInfo?.nickName }}
          </span>
          <span v-if="publishUserisAdmin(userInfo?.roles as Role[])"
            class="ml-3 inline-block font-normal text-xs bg-user-admin-color rounded-lg text-white px-2 leading-5">管理员</span>
        </div>
        <div class="desc">{{ userInfo?.userDesc }}</div>
      </div>
    </div>

    <div class="total_wrap">
      <div class="func_bar">
        <div class="view_like">
          <div></div>
          <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;" :arrow-style="{ background: '#2A2A2A' }">
            <template #trigger>
              <div class="view">
                <i class="iconfont">&#xe7d0;</i>
                <span>{{ vlcData?.data.views || '...' }}</span>
              </div>
            </template>
            <p>浏览总数</p>
          </n-popover>
          <div class="line"></div>
          <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;" :arrow-style="{ background: '#2A2A2A' }">
            <template #trigger>
              <div class="like">
                <i class="iconfont">&#xe8c3;</i>
                <span>{{ vlcData?.data.likes || '...' }}</span>
              </div>
            </template>
            <p>认可总数</p>
          </n-popover>
          <div class="line"></div>
          <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;" :arrow-style="{ background: '#2A2A2A' }">
            <template #trigger>
              <div class="like">
                <i class="iconfont">&#xe687;</i>
                <span>{{ vlcData?.data.likes || '...' }}</span>
              </div>
            </template>
            <p>评论总数</p>
          </n-popover>
          <div></div>
        </div>

        <div class="edit_btn" v-if="globalStore.userInfo?.userId.toString() === route.params.userId">
          <n-button secondary :type="isEdit ? 'warning' : 'primary'" style="width: 100%;" @click="isEdit = !isEdit">{{
        isEdit
          ?
          '< 返回' : '编辑个人资料' }}</n-button>
        </div>
        <div class="mt-3">
          <n-button secondary :type="isArticle ? 'warning' : 'primary'" style="width: 100%;" @click="toggleShowArticle">
            {{ isArticle ? '< 返回' : (globalStore.userInfo?.userId.toString() === route.params.userId ? '我' : 'Ta')
        + '的文章' }} </n-button>
        </div>
        <div class="articles_gx">
          <p>个人简介</p>
          <n-scrollbar style="max-height: 90px">
            <div style="color: #515151;margin-top: 8px;padding: 0 5px; ;border-radius: 10px;">
              {{ userInfo?.userDesc }}
            </div>
          </n-scrollbar>
          <div class="gx_item">
            <img class="icon" :src="getAssetsImages('position.svg')" alt="" />
            <span>{{ userInfo?.userAddr || '未知地区' }}</span>
          </div>
          <div class="gx_item">
            <img class="icon" :src="getAssetsImages('xingbie.svg')" alt="" />
            <i class="iconfont mr-2 text-lg" :class="userInfo?.userSex == '1' ? 'text-primary-color' : 'text-rose-500'"
              v-html="userInfo?.userSex == '1' ? '&#xe609;' : '&#xe60e;'"></i>
            <span>{{ userInfo?.userSex == '1' ? '男' : userInfo?.userSex == '0' ? '女' : '未知' }}</span>
          </div>
          <div class="gx_item">
            <img class="icon" :src="getAssetsImages('user_active.svg')" alt="" />
            <span>活跃于 {{ userInfo?.finalUpTime }}</span>
          </div>
          <div class="gx_item" v-show="userInfo?.userSchool != null && userInfo.userSchool != ''">
            <img class="icon" style="transform: scale(1.4);" :src="getAssetsImages('school.svg')" alt="" />
            <span>{{ userInfo?.userSchool }}</span>
          </div>
        </div>
        <div class="articles_gx" style="margin-top: 10px;">
          <p>文章贡献</p>
          <div class="gx_item">
            <img class="icon" :src="getAssetsImages('views.svg')" alt="" />
            <span>阅读总数</span><span style="margin-left: 10px;">
              {{ userArticlesInfo?.totalViews != null ? userArticlesInfo?.totalViews : '...' }}
            </span>
          </div>
          <div class="gx_item">
            <img class="icon" :src="getAssetsImages('likes.svg')" alt="" />
            <span>认可总数</span><span style="margin-left: 10px;">
              {{ userArticlesInfo?.totalLike != null ? userArticlesInfo?.totalLike : '...' }}
            </span>
          </div>
        </div>
      </div>

      <div class="right_content" v-show="!isArticle">
        <div v-show="!isEdit">
          <div class="inline_box">
            <div class="view_box">
              <div class="title" style="margin-bottom: 3px;">
                <i class="iconfont" style="font-size: 30px;line-height: 20px;">&#xe8bd;</i>
                <p>浏览记录</p>
              </div>
              <div style="height:230px;display: flex;justify-content: center;align-items: center;"
                v-show="userViews.length == 0">
                <n-empty description="这里空空的" />
              </div>
              <n-scrollbar style="max-height: 230px">
                <div class="view_content">
                  <div class="view_item" v-for="item in userViews" @click="handleClickViewItem(item)">
                    <div class="avatar">
                      <n-avatar round :size="30" :src="item.user.userAvatar" />
                    </div>
                    <div class="right">
                      <div class="title">{{ item.articles.articleTitle }}</div>
                      <div class="time">{{ formatDate(item.createTime) }}</div>
                      <div class="tag_list">
                        <div class="tag_item" v-for="tag in item.articles.tagInfoList">
                          <n-tag>{{ tag.tagName }}</n-tag>
                        </div>
                      </div>
                      <div class="data_box">
                        <div class="data_item">
                          <i class="iconfont">&#xe7d0;</i>
                          <span>{{ item.articles.articlesData?.view }}</span>
                        </div>
                        <div class="data_item">
                          <i class="iconfont" :style="{ color: item.articles.isLike ? '#F75F5E' : '#333' }">{{
        item.articles.isLike ?
          '&#xe8c4;' : '&#xe8c3;' }}</i>
                          <span>{{ item.articles.articlesData?.like }}</span>
                        </div>
                        <div class="data_item">
                          <i class="iconfont">&#xe687;</i>
                          <span>{{ item.articles.articlesData?.comment }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </n-scrollbar>
            </div>
            <div class="like_box view_box">
              <div class="title">
                <i class="iconfont" style="font-size: 30px;line-height: 20px;">&#xe8c3;</i>
                <p>认可列表</p>
              </div>
              <div style="height:230px;display: flex;justify-content: center;align-items: center;"
                v-show="userLikes.length == 0">
                <n-empty description="这里空空的" />
              </div>
              <n-scrollbar style="max-height: 230px">
                <div class="view_content">
                  <div class="view_item" v-for="item in userLikes" @click="handleClickViewItem(item)">
                    <div class="avatar">
                      <n-avatar round :size="30" :src="item.publishUser?.userAvatar" />
                    </div>
                    <div class="right">
                      <div class="title">{{ item.articleTitle }}</div>
                      <div class="tag_list overflow-x-auto">
                        <div class="tag_item" v-for="tag in item.tagInfoList?.slice(0, 4)">
                          <n-tag>{{ tag.tagName }}</n-tag>
                        </div>
                        <span v-show="item.tagInfoList?.length as number > 4">...</span>
                      </div>
                      <div class="data_box">
                        <div class="data_item">
                          <i class="iconfont">&#xe7d0;</i>
                          <span>{{ item.articlesData?.view }}</span>
                        </div>
                        <div class="data_item">
                          <i class="iconfont" :style="{ color: item.isLike ? '#F75F5E' : '#333' }">{{
        item.isLike ?
          '&#xe8c4;' : '&#xe8c3;' }}</i>
                          <span>{{ item.articlesData?.like }}</span>
                        </div>
                        <div class="data_item">
                          <i class="iconfont">&#xe687;</i>
                          <span>{{ item.articlesData?.comment }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </n-scrollbar>
            </div>
          </div>

          <div class="line_box">
            <calander-view-graph />
          </div>
        </div>
        <div v-if="isEdit">
          <edit-page :userInfo="(userInfo as userInfo)" @userInfoUpdate="userInfoUpdate" @exitEdit="isEdit = false" />
        </div>
      </div>

      <articles-manage v-show="isArticle" ref="articlesManageRef" />
    </div>
  </div>
</template>

<style lang='scss' scoped>
.userinfo_wrap {
  max-width: 1200px;
  width: 100%;
  min-width: 300px;
  height: fit-content;
  padding-bottom: 20px;

  .total_wrap {
    display: flex;
    margin-top: 20px;

    @media screen and (max-width:480px) {
      display: block;
      padding: 0 20px;
    }


    .func_bar {
      width: 320px;
      height: 544px;
      box-shadow: 1px 1px 6px #dbdbdb;
      border-radius: 10px;
      padding: 20px;

      @media screen and (max-width:480px) {
        width: 100%;
      }

      .articles_gx {
        margin-top: 20px;

        p {
          font-size: 17px;
        }

        .gx_item {
          display: flex;
          align-items: center;
          margin-top: 10px;

          .icon {
            width: 26px;
            margin-right: 10px;
          }

          span {
            color: #515151;
          }
        }
      }

      .edit_btn {
        width: 100%;
        margin: 20px auto 0px auto;
      }

      .view_like {
        display: flex;
        justify-content: space-between;

        .line {
          width: 2px;
          height: 55px;
          border-radius: 10px;
          background: #dbdbdb;
          margin-top: 5px;
        }

        .view {
          display: flex;
          flex-direction: column;
          padding: 0 20px;
          align-items: center;
          justify-content: center;
          cursor: pointer;

          i {
            font-size: 26px;
          }

          span {}
        }

        .like {
          display: flex;
          flex-direction: column;
          padding: 0 20px;
          align-items: center;
          justify-content: center;
          cursor: pointer;

          i {
            font-size: 26px;
          }


          span {}
        }

      }
    }

    .right_content {
      flex: 1;
      height: fit-content;
      margin-left: 30px;

      @media screen and (max-width:480px) {
        margin: 0;
        margin-top: 20px;
        width: 100%;
      }

      .inline_box {
        display: flex;

        @media screen and (max-width:480px) {
          display: block;
        }

        .view_box {
          box-shadow: 1px 1px 6px #dbdbdb;
          min-width: 300px;
          width: 50%;
          height: 300px;
          border-radius: 10px;
          padding: 20px;

          &:nth-child(1) {
            margin-right: 30px;
          }

          @media screen and (max-width:480px) {
            width: 100%;
            margin-right: 0px !important;
          }

          .title {
            display: flex;
            align-items: center;

            p {
              margin-left: 10px;
              font-weight: bold;
              font-size: 18px;
            }
          }

          .view_content {
            overflow-y: auto;

            .view_item {
              width: 100%;
              height: fit-content;
              color: rgb(36, 36, 36);
              border-radius: 10px;
              background: #F7F7F8;
              padding: 10px;
              display: flex;
              cursor: pointer;
              transition: all .4s;

              .avatar {
                margin-right: 10px;
              }

              .right {
                .data_box {
                  display: flex;
                  margin-top: 5px;
                  align-items: center;

                  .data_item {
                    color: #606060;
                    margin-right: 10px;
                    font-size: 14px;

                    span {
                      margin-left: 3px;
                    }
                  }
                }

                .title {
                  letter-spacing: 1px;
                }

                .time {
                  color: #929292;
                  font-size: 12px;
                }

                .tag_list {
                  display: flex;
                  white-space: nowrap;

                  .tag_item {
                    margin-right: 5px;
                  }
                }
              }

              &:nth-child(n + 1) {
                margin-top: 10px;
              }

              &:hover {
                transform: scale(0.95);

                .title {
                  text-decoration: underline;
                }
              }
            }
          }
        }


        .like_box {
          box-shadow: 1px 1px 6px #dbdbdb;
          min-width: 300px;
          width: 48%;
          height: 300px;
          border-radius: 10px;
          padding: 20px;

          @media screen and (max-width:480px) {
            width: 100%;
            margin-right: 0px !important;

            &:nth-child(n+1) {
              margin-top: 20px;
            }
          }
        }

      }

      .line_box {
        width: 100%;
        box-shadow: 1px 1px 6px #dbdbdb;
        margin-top: 30px;
        border-radius: 10px;
        height: fit-content;
        padding-bottom: 10px;
      }
    }
  }

  .user_wrap {
    height: 80px;
    width: 300px;
    display: flex;

    @media screen and (max-width:480px) {
      padding: 0 20px;
    }

    .user_img {
      position: relative;
      cursor: pointer;

      &:hover {
        &::before {
          content: '+';
          width: 80px;
          height: 80px;
          background: #00000044;
          position: absolute;
          left: 0;
          right: 0;
          top: 0;
          bottom: 0;
          z-index: 10;
          color: #fff;
          font-size: 50px;
          display: flex;
          justify-content: center;
          align-items: center;
        }
      }
    }

    .right {
      margin-left: 20px;
      width: 300px;

      .nick_name {
        font-weight: bold;
        font-size: 20px;
      }

      .desc {
        color: #515151;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
      }
    }

  }

  @media screen and (min-width: 700px) and (max-width: 1200px) {
    padding-left: 60px;
    padding-right: 60px;
  }

  @media screen and (max-width: 700px) and (min-width: 480px) {
    padding-left: 60px;
    padding-right: 60px;
  }

  @media screen and (max-width:480px) {}
}
</style>