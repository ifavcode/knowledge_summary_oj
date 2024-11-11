<script setup lang='ts'>
import { request } from '~/utils/request';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-light.css'
import moment from 'moment'
import 'moment/dist/locale/zh-cn'
import { useGlobalStore } from '~/store/globalStore';
import { emitter, publishUserisAdmin } from '~/utils/index'
import { useMessage } from 'naive-ui';
import router from '~/router';
//@ts-ignore
import DOMPurify from 'dompurify';
import Player from 'xgplayer'
import 'xgplayer/dist/index.min.css'
//@ts-ignore
import VditorPreview from 'vditor/dist/method.min.js'
import 'vditor/dist/index.css'
//md2html

moment.locale('zh-cn')

const globalStore = useGlobalStore()
const message = useMessage()
const viewerRef = ref()

const route = useRoute()
const isLike = ref(false)

const articleContent = ref<Articles>({})

let html = ref('')

const getArticleContent = async () => {
  const res: AjaxResult<Articles> = await request('/api/articles/id', {
    params: {
      articleId: route.params.articleId
    }
  })
  articleContent.value = res.data as Articles
  if (res.data?.articleType == 'richtext') {
    html.value = DOMPurify.sanitize(articleContent.value.articleContent as string)
  } else {
    const localCodeStyleChange: string | null = localStorage.getItem('codeStyleChange')
    const localCodeLineChange: string | null = localStorage.getItem('codeLineChange')
    VditorPreview.preview(viewerRef.value, articleContent.value.articleContent as string, {
      hljs: {
        enable: true,
        lineNumber: localCodeLineChange == 'true',
        style: localCodeStyleChange || 'xcode',
      },
      math: {
        inlineDigit: true,
        engine: 'KaTeX'
      },
    })
  }
  nextTick(() => {
    hljs.highlightAll()
    let videos: HTMLCollectionOf<HTMLVideoElement> = document.getElementsByTagName('video')
    setTimeout(() => {
      for (let i = 0; i < videos.length; i++) {
        const el = document.createElement('div')
        const elFa = document.createElement('div')
        elFa.appendChild(el)
        elFa.style.maxWidth = '600px'
        elFa.style.width = '100%'
        videos.item(i)?.parentNode?.appendChild(elFa)
        new Player({
          el,
          url: videos[i].children[0].getAttribute('src'),
          pip: true,
          lang: 'zh-cn',
          fluid: true,
        })
        videos.item(i)?.parentNode?.removeChild(videos[i])
      }
    }, 0);
  })
}

function formatDate(createTime: string) {
  if (new Date().getTime() - new Date(createTime).getTime() <= 1000 * 60 * 60 * 24 * 7) {
    return moment(createTime, 'YYYY/MM/DD HH:mm:ss').startOf('hour').fromNow()
  } else {
    return moment(createTime, 'YYYY/MM/DD HH:mm:ss').format('l')
  }
}

const likeOptions = ref<Array<{
  userId: number,
  name: string,
  src: string
}>>([])

const getLikeOptions = async (f: boolean = true) => {
  const res: AjaxResult<User[]> = await request('/api/articles/like/user', {
    params: {
      articleId: route.params.articleId
    }
  })
  likeOptions.value = []
  if (res) {
    res.data?.forEach(v => {
      likeOptions.value.push({
        name: v.nickName,
        src: v.userAvatar,
        userId: v.userId
      })
    })
    if (f) {
      if (likeOptions.value.find(v => v.userId == globalStore.userInfo?.userId)) {
        isLike.value = true
      }
    }
  }
}

const likeArticle = async () => {
  const res: boolean = await request(`/api/articles/like/${articleContent.value.articleId}`, {
    method: 'post'
  })
  if (res) {
    isLike.value = !isLike.value
    getLikeOptions(false)
  }
}

const articleComment = ref<any>()
const handleCommentIcon = () => {
  window.scrollTo({
    top: articleComment.value?.commentWrap.offsetTop,
    behavior: 'smooth'
  })
}

const replyText = ref('')
const loading = ref(false)
const sendComment = async () => {
  if (replyText.value == '') {
    message.warning('回复内容不能为空哦')
    return
  }
  loading.value = true
  const res: AjaxResult<QdComment> = await request('/api/articles/comment/send', {
    method: 'post',
    data: {
      articleId: articleContent.value.articleId,
      oucId: -1,
      commentContent: replyText.value,
      commentImg: '',
      targetUserId: -1
    }
  })
  if (res && res.code == 200) {
    message.success('回复成功')
    replyText.value = ''
    await articleComment.value.getArticleComment()
    articleComment.value.highlightCurSendReply(res.data?.commentId, 2000, 150)
  }
  loading.value = false
}

const enterUserInfoPage = () => {
  router.push('/profile/' + articleContent.value.userId)
}

const updateArticle = () => {
  if (articleContent.value.articleType === 'richtext') {
    router.push('/_edit_article/' + articleContent.value.articleId)
  } else if (articleContent.value.articleType === 'markdown') {
    router.push('/edit_article/' + articleContent.value.articleId)
  }
}

const deleteArticle = async () => {
  const res: boolean = await request('/api/articles/delete/' + articleContent.value.articleId, {
    method: 'post'
  })
  if (res) {
    message.success('删除成功')
    router.replace('/home')
  } else {
    message.error('删除失败')
  }
}

onMounted(() => {
  nextTick(() => {
    getArticleContent()
  })
  getLikeOptions()
  emitter.on('loginSuccess', () => {
    getArticleContent()
    getLikeOptions()
  })
  emitter.on('logoutSuccess', () => {
    isLike.value = false
  })
  emitter.on('codeStyleChange', (obj: any) => {
    VditorPreview.preview(viewerRef.value, articleContent.value.articleContent as string, {
      hljs: {
        lineNumber: obj.lineNumber,
        style: obj.style,
        enable: true,
      },
      math: {
        inlineDigit: true,
        engine: 'KaTeX'
      },
    })
  })
})

</script>
 
<template>
  <div class="article_wrap sm:mt-16 max-sm:mt-16 ml-auto mr-auto">
    <div class="author_wrap w-full flex justify-between">
      <div class="flex items-center">
        <div class="author_img" title="进入个人主页" @click="enterUserInfoPage">
          <n-avatar round size="large" :src="articleContent.publishUser?.userAvatar"
            fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
        </div>
        <div class="text-2xl ml-3">{{ articleContent.articleTitle }}</div>
      </div>

      <!-- 用户操作 -->
      <n-popover placement="bottom-end" trigger="hover" raw
        v-if="globalStore.userAuth.includes('*') || globalStore.userInfo?.userId === articleContent.publishUser?.userId">
        <template #trigger>
          <div class="hover:bg-gray-200 flex justify-center iconfont cursor-pointer ml-2
         text-gray-500  transition-all duration-300 w-5 float-right py-1">
            &#xe60c;
          </div>
        </template>
        <div class="w-28">
          <div
            class="w-full px-3 py-2 cursor-pointer hover:bg-gray-100 text-sm hover:text-primary-color transition-colors duration-100"
            @click="updateArticle">
            <i class="iconfont">&#xe602;</i>
            <span class="ml-2">修改文章</span>
          </div>
          <div
            class="w-full px-3 py-2 cursor-pointer hover:bg-gray-100 text-sm hover:text-rose-500 transition-colors duration-100"
            @click="deleteArticle">
            <n-popconfirm @positive-click="deleteArticle">
              <template #trigger>
                <div>
                  <i class="iconfont">&#xeaf2;</i>
                  <span class="ml-2">删除文章</span>
                </div>
              </template>
              确认删除吗？
            </n-popconfirm>
          </div>
        </div>
      </n-popover>
    </div>
    <div class="data_box">
      <div class="author"
        :style="{ color: publishUserisAdmin(articleContent?.publishUser?.role as Role[]) ? '#FB7299' : '' }">{{
          articleContent.publishUser?.nickName }}</div>
      <div class="data_item">
        <i class="iconfont">&#xe7d0;</i>
        <span>{{ articleContent.articlesData?.view }}</span>
      </div>
      <div class="data_item">
        <i class="iconfont">&#xe8c3;</i>
        <span>{{ articleContent.articlesData?.like }}</span>
      </div>
      <div class="data_item">
        <i class="iconfont">&#xe687;</i>
        <span>{{ articleContent.articlesData?.comment }}</span>
      </div>
      <div class="pub_time">{{ formatDate(articleContent.createTime as string) }}</div>
    </div>
    <n-avatar-group :options="likeOptions" :size="30" :max="100" /><span>共 {{ likeOptions.length }} 人赞同</span>
    <!-- 组件功能demo展示 -->
    <div v-if="articleContent && articleContent.componentAddr">
      <ArticleComponentShow :componentAddr="articleContent.componentAddr" />
    </div>

    <!-- 文章 -->
    <div id="viewer" v-html="html" class="mt-3" ref="viewerRef"> </div>
    <!-- 文章 -->
    <div class="test">
      <div class="bottom_wrap">
        <div class="center_content">
          <div class="icon_item">
            <n-button type="info" :secondary="!isLike" @click="likeArticle">
              <i class="iconfont" v-show="!isLike">&#xe8c3;</i>
              <i class="iconfont active_icon" v-show="isLike">&#xe8c4;</i>
              <span>{{ isLike ? '已赞同' : '赞同' }}</span>
            </n-button>
          </div>
          <div class="icon_item">
            <n-badge :value="0" :offset="[0, 5]" :max="99">
              <i title="跳转至评论位置" class="iconfont" style="color: #000;cursor: pointer;"
                @click="handleCommentIcon">&#xe687;</i>
            </n-badge>
          </div>
          <div class="send_input">
            <n-input-group>
              <n-input v-model:value="replyText" placeholder="开启奇妙之旅" style="width: 100%;" @keydown.enter="sendComment" />
              <n-button secondary @click="sendComment" type="info" :loading="loading">发布</n-button>
            </n-input-group>
          </div>
        </div>
      </div>
    </div>
    <n-divider />
    <ArticleComment :articleContent="articleContent" ref="articleComment" />
  </div>
</template>

<style lang='scss' scoped>
.article_wrap {
  max-width: 800px;
  min-width: 300px;
  padding-bottom: 80px;

  @media screen and (max-width:480px) {
    padding-left: 10px;
    padding-right: 10px;
  }

  .test {
    position: fixed;
    bottom: 0;
    height: 50px;
    z-index: 100;

    .bottom_wrap {
      position: fixed;
      left: 0;
      right: 0;
      bottom: 0;
      width: 100%;
      height: 50px;
      border-top: solid 1px #dfdfdf;
      background: #fff;
      transition: all .4s;
      -webkit-overflow-scrolling: touch;

      @media screen and (max-width: 1200px) and (min-width: 480px) {
        padding: 0 60px;
      }

      @media screen and (max-width:480px) {
        padding: 0 10px;
      }

      .center_content {
        width: 100%;
        max-width: 1000px;
        margin: 0 auto;
        display: flex;
        height: 50px;

        .send_input {
          flex: 1;
          display: flex;
          height: 100%;
          align-items: center;
        }

        .icon_item {
          display: flex;
          align-items: center;
          height: 100%;
          margin-right: 20px;

          .active_icon {
            color: #F75F5E;
          }

          span {
            margin-left: 5px;
          }

          i {
            font-size: 20px;
          }
        }
      }
    }
  }


  .data_box {
    display: flex;
    margin-top: 5px;
    align-items: center;
    margin-bottom: 10px;

    .author {
      color: #353535;
      font-size: 15px;
      margin-right: 20px;
    }

    .pub_time {
      margin-left: 10px;
      color: #606060;
      font-size: 12px;
    }

    .data_item {
      color: #606060;
      margin-right: 10px;
      font-size: 14px;

      span {
        margin-left: 3px;
      }
    }
  }

  .author_wrap {
    display: flex;
    align-items: center;

    .author_img {
      cursor: pointer;
    }
  }

  #viewer {
    width: 800px;
    max-width: 100%;
    overflow-x: hidden;

    @media screen and (max-width:480px) {
      padding: 0 5px;
    }
  }

  @media screen and (min-width: 700px) and (max-width: 1200px) {
    padding-left: 60px;
    padding-right: 60px;
  }

  @media screen and (max-width: 700px)and (min-width: 480px) {
    padding-left: 0px;
    padding-right: 0px;
  }
}
</style>
