<script setup lang='ts'>
import { emitter } from '~/utils/index'
//@ts-ignore
import { OnClickOutside } from '@vueuse/components'
const props = defineProps<{
  commentList: QdComment[],
  depth: number,
  index: number,
}>()

const activeInputReply = ref(-1)


const handleChildrenReply = (item: QdComment) => {
  emitter.emit('childrenClickReply', { item, index: props.index })
  console.log(item.commentId);
  emitter.emit('allInputCancel')
  activeInputReply.value = item.commentId
}

onMounted(() => {
  emitter.on('allInputCancel', () => {
    activeInputReply.value = -1
  })
})
</script>
 
<template>
  <div class="children_comment">
    <div class="comment_item" v-for="item in props.commentList" :style="{ marginLeft: depth <= 2 ? '30px' : '0px' }">
      <div class="user_box" :class="activeInputReply === item.commentId ? 'focus_input' : ''">
        <div class="avatar">
          <n-avatar size="small" :src="item.user.userAvatar" circle />
        </div>
        <div class="box">
          <p class="nick_name" v-if="depth <= 1">{{ item.user.nickName }}</p>
          <p class="nick_name" v-else>{{ item.user.nickName }}
            <span style="font-weight: 800;display: inline-block;" v-show="item.user.userId != item.targetUser.userId">&gt;
              {{ item.targetUser.nickName }}</span>
          </p>
          <p class="comment_content">
            {{ item.commentContent }}
          </p>
          <p class="create_time">
            {{ item.createTime }}
            <span @click="handleChildrenReply(item)">回复</span>
          </p>
        </div>
      </div>
      <childrenComment :commentList="item.childrenComment" :depth="depth + 1" :index="index" />
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.children_comment {

  .comment_item {
    background: #fff;

    .focus_input {
      background: #f6f6f6;
    }

    .user_box {
      display: flex;
      align-items: center;
      transition: all .4s;
      padding: 10px;
      border-radius: 10px;

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
</style>