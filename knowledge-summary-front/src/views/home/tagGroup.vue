<script setup lang='ts'>
import { request } from '~/utils/request';
import { publishUserisAdmin } from '~/utils/index';
import moment from 'moment'
import 'moment/dist/locale/zh-cn'

moment.locale('zh-cn')

const tagList = ref<TagInfo[]>([])
const articlesList = ref<Articles[]>([])
// const preColor: Array<string> = []

const getAllTag = async () => {
  const res: AjaxResult<TagInfo[]> = await request('/api/tag/all')
  tagList.value = res.data as TagInfo[]
}

/**
 * 
 * @param article 开始加载
 */
// const getArticles = async () => {
//   const res: AjaxResult<Articles[]> = await request('/api/articles/all', {
//     params: {
//       pageNum: 1,
//       pageSize,
//     }
//   })
//   const regExp = /[~!@#$%^&*()+=|{}':;',\[\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]/
//   res.data?.map(v => {
//     set.add(v.articleId)
//     v.articleContent = v.articleContent?.replace(regExp, '')
//   })
//   articlesList.value = res.data as Articles[]
// }

// const handleArticleContent = (content: string): string => {
//   let str: string = ''
//   let i: number = 0;
//   let reg = /[\u4e00-\u9fa5]/
//   while (str.length < 100 && i < content.length) {
//     let c = content.charAt(i++)
//     if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || reg.test(c)) {
//       str += c
//     }
//   }
//   return str
// }

const enterArticle = (article: Articles) => {
  window.open(window.location.origin + '#/article/' + article.articleId)
}

function formatDate(createTime: string) {
  if (new Date().getTime() - new Date(createTime).getTime() <= 1000 * 60 * 60 * 24 * 7) {
    return moment(createTime, 'YYYY/MM/DD HH:mm:ss').startOf('hour').fromNow()
  } else {
    return moment(createTime, 'YYYY/MM/DD HH:mm:ss').format('YYYY/MM/DD HH:mm:ss')
  }
}

/**
 * 加载更多
 */
let pageNum = 0
let pageSize = 10
const nomore = ref(false)
const bottomEle = ref()
let set = new Set()

const loadmore = async () => {
  if (!nomore.value) {
    const res: AjaxResult<Articles[]> = await request('/api/articles/all', {
      params: {
        pageNum: ++pageNum,
        pageSize,
      }
    })
    const regExp = /[~!@#$%^&*()+=|{}':;',\[\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]/
    res.data?.map(v => {
      v.articleContent = v.articleContent?.replace(regExp, '')
    })
    res.data?.forEach(v => {
      if (!set.has(v.articleId)) {
        set.add(v.articleId)
        articlesList.value.push(v)
      }
    })
    if (res.data?.length === 0) {
      nomore.value = true
    }
  }

}

onBeforeMount(() => {
  getAllTag()
})

let scrollFd: NodeJS.Timeout

onMounted(() => {
  nextTick(() => {
    loadmore()
    window.onscroll = () => {
      clearTimeout(scrollFd)
      scrollFd = setTimeout(() => {
        // 滚动视口高度(也就是当前元素的真实高度)
        let scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;
        // 可见区域高度
        let clientHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
        // 滚动条顶部到浏览器顶部高度
        let scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        if (clientHeight + scrollTop + 50 >= scrollHeight) {
          loadmore()
        }
      }, 200);
    }
  })

})
</script>
 
<template>
  <div class="tag_group_wrap">
    <h2 class="text-xl mb-2">最新</h2>
    <!-- <div class="tag_wrap">
      <div class="tag_item" v-for="(item, index) in tagList">
        <n-tag :type="index < 3 ? 'success' : 'default'"> {{ item.tagName }} </n-tag>
      </div>
    </div> -->
    <div class="articles_wrap">
      <div class="articles_item bg-white hover:bg-gray-50" v-for="item in articlesList" @click="enterArticle(item)">
        <div class="top">
          <div class="tot_left">
            <div class="top_user_pub_info">
              <div class="author_img">
                <n-avatar round size="small" :src="item.publishUser?.userAvatar"
                  fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
              </div>
              <div class="author tracking-wide"
                :style="{ color: publishUserisAdmin(item.publishUser?.role as Role[]) ? '#FB7299' : '' }">{{
                  item.publishUser?.nickName || '未命名' }}</div>
              <div class="pub_time">{{ formatDate(item.createTime as string) }}</div>
              <div class="tags">
                <span v-for="(tag, i) in item.tagInfoList"> {{ tag.tagName + (i != (item.tagInfoList?.length as number) -
                  1
                  ?
                  ' |' : '') }} </span>
              </div>
            </div>
            <div class="bottom_article_info">
              <div class="left">
                <div class="w-full text-lg text-black my-1">
                  {{ item.articleTitle }}
                </div>
                <div class="content text-gray-600 line-clamp-3 text-base">
                  <!-- {{ handleArticleContent(item.articleContent as string) }} -->
                  {{ item.rawText }}
                </div>
              </div>
            </div>
            <div class="flex mt-3">
              <div class="mr-3">
                <i class="iconfont mr-2">&#xe7d0;</i>
                <span>{{ item.articlesData?.view }}</span>
              </div>
              <div class="mr-3">
                <i class="iconfont mr-2">&#xe8c3;</i>
                <span>{{ item.articlesData?.like }}</span>
              </div>
              <div class="mr-3">
                <i class="iconfont mr-2">&#xe687;</i>
                <span>{{ item.articlesData?.comment }}</span>
              </div>
            </div>
            <img class="mobile_img" v-if="item.articleImage != null && item.articleImage != ''" :src="item.articleImage"
              alt="">
          </div>
          <div class="tot_right">
            <img v-if="item.articleImage != null && item.articleImage != ''" :src="item.articleImage" alt="">
          </div>
        </div>
        <!-- <p>浏览信息</p> -->
      </div>

      <div ref="bottomEle" class="w-full text-center py-5" v-show="!nomore">
        <n-spin size="small" />
      </div>
      <n-divider dashed v-show="nomore">
        <span class="text-gray-500 text-sm">到底啦~</span>
      </n-divider>

    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.tag_group_wrap {
  margin-top: 10px;

  .articles_wrap {
    margin-top: 20px;
    width: 100%;
    box-shadow: 1px 1px 6px #e7e7e7;
    border-radius: 10px;
    padding-bottom: 1px;

    .articles_item {
      width: 100%;
      height: fit-content;

      &:nth-child(n + 2) {
        border-top: solid 1px #e7e7e7;
      }

      padding: 20px;

      .top {
        cursor: pointer;
        display: flex;

        .tot_left {
          width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;

          .top_user_pub_info {
            display: flex;
            align-items: center;

            .author {
              color: #353535;
              font-size: 15px;
              margin-left: 20px;
            }

            .author_img {}

            .pub_time {
              margin-left: 20px;
              color: #606060;
              font-size: 12px;

              @media screen and (max-width:480px) {
                white-space: nowrap;
                overflow-y: auto;
              }
            }

            .tags {
              @media screen and (max-width:480px) {
                white-space: nowrap;
                overflow-y: auto;
              }

              span {
                color: #606060;
                padding-left: 10px;
                font-size: 12px;
              }
            }
          }

          .bottom_article_info {
            width: 800px;
            display: flex;

            .left {
              .title {
                font-size: 20px;
                letter-spacing: 1px;
                font-family: '微软雅黑';
                margin: 5px 0px;

                &:hover {
                  color: #3a3a3a;
                  text-decoration: underline;
                }
              }

              .content {}
            }
          }

          .mobile_img {
            display: none;

            @media screen and (max-width:480px) {
              width: 50%;
              max-height: 400px;
              display: block;
              margin-top: 8px;
            }
          }
        }

        .tot_right {
          max-width: 150px;
          max-height: 150px;
          margin-left: 20px;

          @media screen and (max-width:480px) {
            display: none;
          }

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
      }
    }
  }

  .tag_wrap {
    display: flex;
    flex-wrap: wrap;

    @media screen and (max-width:480px) {
      padding-left: 10px;
    }

    .tag_item {
      margin: 0 5px;

      @media screen and (max-width:480px) {
        margin: 5px;
      }
    }
  }

  h2 {
    color: #424242;
    font-weight: normal;

    @media screen and (max-width:480px) {
      padding-left: 10px;
    }
  }

  .table_wrap {
    margin-top: 30px;
  }
}
</style>