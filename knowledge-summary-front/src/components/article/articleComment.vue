<script setup lang='ts'>
import { useGlobalStore } from '~/store/globalStore';
import { request, requestAnonymous } from '~/utils/request';
import { emitter } from '~/utils/index'
import { useMessage } from 'naive-ui';
import Cookies from 'js-cookie'

const props = defineProps<{
  articleContent: Articles
}>()
const route = useRoute()

const replyText = ref('')
const commentList = ref<QdComment[]>([])
const globalStore = useGlobalStore()
const curReplyIndex = ref(-1)
const inputTips = ref('开启友善的交流~~~')
//哪条评论高亮？
const activeInputReply = ref(1)
const commentWrap = ref<HTMLElement>()

let key: NodeJS.Timer = setInterval(() => {
  let scrollTop = Cookies.get('scrollTop') ? JSON.parse(Cookies.get('scrollTop') as string) : {}
  scrollTop[route.params.articleId.toString()] = window.scrollY
  Cookies.set('scrollTop', JSON.stringify(scrollTop), {
    expires: 7
  })
}, 5000)

const activeReplyItem = ref<QdComment>()
const message = useMessage()

const getArticleComment = async () => {
  const res: AjaxResult<QdComment[]> = await requestAnonymous('/api/articles/comment', {
    params: {
      articleId: route.params.articleId
    }
  })
  if (res) {
    commentList.value = res.data as QdComment[]
  }
}

const childrenClickReply = ({ item, index }: { item: QdComment, index: number }) => {
  handleClickReply(item, index)
}
const handleClickReply = (item: QdComment, index: number) => {
  curReplyIndex.value = index
  inputTips.value = '回复' + item.user.nickName
  emitter.emit('allInputCancel')
  activeInputReply.value = item.commentId
  activeReplyItem.value = item
}

// onClickOutside(commentWrap, () => {
//   activeInputReply.value = -1
//   curReplyIndex.value = -1
// })
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
      articleId: props.articleContent.articleId,
      oucId: activeReplyItem.value?.commentId,
      commentContent: replyText.value,
      commentImg: '',
      targetUserId: activeReplyItem.value?.userId
    }
  })
  if (res) {
    message.success('回复成功')
    activeInputReply.value = -1
    curReplyIndex.value = -1
    replyText.value = ''
    getArticleComment()
    emitter.emit('childrenReplySuccess')
  }
  loading.value = false
}

//高亮刚回复的评论
const commentListDom = ref<HTMLElement>()
const highlightCurSendReply = (commentId: number, time: number, delay: number) => {
  activeInputReply.value = commentId,
    setTimeout(() => {
      activeInputReply.value = -1
    }, time);
  //滚动到对应位置 
  nextTick(() => {
    setTimeout(() => {
      for (let v of [...commentListDom.value?.children as any]) {
        if (v.getAttribute('commentid') == commentId) {
          window.scrollTo({
            top: v.offsetTop,
            behavior: 'smooth'
          })
          break
        }
      }
    }, delay);
  })
}

onMounted(async () => {
  await getArticleComment()
  emitter.on('childrenClickReply', (obj: any) => {
    childrenClickReply({
      item: obj.item,
      index: obj.index
    })
  })
  emitter.on('allInputCancel', () => {
    activeInputReply.value = -1
  })
  //是否滚动到上次阅读的位置
  let scrollTop = Cookies.get('scrollTop') ? JSON.parse(Cookies.get('scrollTop') as string) : {}
  let id = route.params.articleId.toString()
  setTimeout(() => {
    window.scrollTo({
      top: scrollTop[id] ? scrollTop[id] : 0,
      behavior: 'instant'
    })
  }, 150);
})

onBeforeUnmount(() => {
  clearInterval(key)
})

defineExpose({
  commentWrap, getArticleComment, highlightCurSendReply
})
</script>
 
<template>
  <div id="comment_wrap" ref="commentWrap">
    <div class="title">
      评论
      <span>{{ props.articleContent.articlesData?.comment || '0' }}</span>
    </div>
    <div v-show="props.articleContent.articlesData?.comment == 0">
      <n-empty description="这里空空的">
      </n-empty>
    </div>
    <div class="comment_list" ref="commentListDom">
      <div class="comment_item" v-for="(item, index) in commentList" :commentId="item.commentId">
        <div class="user_box" :class="activeInputReply === item.commentId ? 'focus_input' : ''">
          <div class="avatar">
            <n-avatar size="large" :src="item.user.userAvatar" circle />
          </div>
          <div class="box">
            <p class="nick_name">{{ item.user.nickName }}</p>
            <p class="comment_content">
              {{ item.commentContent }}
            </p>
            <p class="create_time">
              {{ item.createTime }}
              <span @click="handleClickReply(item, index)">回复</span>
            </p>
          </div>
        </div>
        <ChildrenComment :commentList="item.childrenComment" :depth="1" :index="index" />
        <div class="reply_wrap" v-if="index == curReplyIndex">
          <n-avatar class="avatar" size="large" :src="globalStore.userInfo?.userAvatar" circle />
          <n-input class="input" @keydown.enter="sendComment" v-model:value="replyText" :placeholder="inputTips"
            autofocus />
          <n-button class="btn" secondary @click="sendComment" :loading="loading">发布</n-button>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
#comment_wrap {
  margin-top: 20px;


  .title {
    font-size: 24px;
    display: flex;
    align-items: center;

    span {
      color: #a5a5a5;
      font-weight: normal;
      font-size: 18px;
      margin-left: 8px;
    }
  }

  .comment_list {


    .comment_item {
      border-bottom: solid 1px #eaeaea;

      .reply_wrap {
        display: flex;
        align-items: center;
        height: 60px;
        margin: 10px 0;

        .avatar {}

        .input {
          height: 100%;
          width: 60%;
          margin: 0 10px;
        }

        .btn {
          height: 100%;
        }

      }

      .focus_input {
        background: #f6f6f6;
      }

      .user_box {
        display: flex;
        align-items: center;
        border-radius: 10px;
        padding: 10px;
        transition: all .4s;

        .avatar {
          margin-right: 20px;
        }

        .box {
          .nick_name {
            font-size: 14px;
            color: #61666d;
          }

          .comment_content {
            color: #000;
            font-size: 16px;
          }

          .create_time {
            color: #9499A0;

            span {
              color: #9499A0;
              margin-left: 10px;
              cursor: pointer;
            }
          }
        }
      }
    }
  }
}
</style>