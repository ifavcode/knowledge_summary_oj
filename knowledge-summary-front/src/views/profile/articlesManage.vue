<script setup lang='ts'>
import { request } from '~/utils/request';
import moment from 'moment'
import { useMessage } from 'naive-ui';
import { useGlobalStore } from '~/store/globalStore';
import router from '~/router';
import { Search } from '@vicons/ionicons5'

const route = useRoute()
const articlesList = ref<Articles[]>([])
const pinnedArticlesList = ref<Articles[]>([])
let pageNum = 0
let pageSize = 10
const nomore = ref(false)
const bottomEle = ref()
const globalStore = useGlobalStore()

const message = useMessage()

const intersectionObserver = new IntersectionObserver((entries) => {
  // 如果 intersectionRatio 为 0，则目标在视野外，
  // 我们不需要做任何事情。
  if (entries[0].intersectionRatio <= 0) return;
  getUserPublishArticlesId()
});

//不置顶
const getUserPublishArticlesId = async () => {
  if (!nomore.value) {
    const res: AjaxResult<Articles[]> = await request('/api/articles/userPublish/' + route.params.userId, {
      params: {
        pageNum: ++pageNum,
        pageSize,
        type: 'time',
        flag: '1' //0取全部，1取不置顶，2取置顶
      }
    })
    let set = new Set()
    articlesList.value.forEach(v => set.add(v.articleId))
    res.data?.forEach(v => {
      if (!set.has(v.articleId)) {
        articlesList.value.push(v)
      }
    })
    if (res.data?.length === 0) {
      nomore.value = true
    }
  }
}

//置顶
const getUserPublishPinnedArticlesId = async () => {
  const res: AjaxResult<Articles[]> = await request('/api/articles/userPublish/' + route.params.userId, {
    params: {
      pageNum: 1,
      pageSize: 30,
      type: 'time',
      flag: '2' //0取全部，1取不置顶，2取置顶
    }
  })
  pinnedArticlesList.value = res.data as Articles[]
}

const handleDeleteClick = async (articleId: number) => {
  const res: boolean = await request('/api/articles/delete/' + articleId, {
    method: 'post'
  })
  if (res) {
    message.success('删除成功')
    pageNum = 0
    articlesList.value = []
    getUserPublishArticlesId()
  } else {
    message.error('删除失败')
  }
}

const handleClickItem = (articleId: number) => {
  window.open(window.location.origin + '#/article/' + articleId)
}

/**
 * 编辑文章
 */

const editArticle = (item: Articles) => {
  if (item.articleType === 'richtext') {
    router.push('/_edit_article/' + item.articleId)
  } else if (item.articleType === 'markdown') {
    router.push('/edit_article/' + item.articleId)
  }
}

const reload = () => {
  pageNum = 0
  nomore.value = false
  articlesList.value = []
  pinnedArticlesList.value = []
  getUserPublishPinnedArticlesId()
}

/**
 * 搜索用户文章
 */

let uaPageNum = 1
let uaPageSize = 10
const usNomore = ref(false)
const keyWord = ref('')
const isSearchMode = ref(false)
const searchArticlesList = ref<Articles[]>([])
const searchLoading = ref(false)
const searchBottomEle = ref()
const searchUserArticles = async (init: boolean = true) => {
  if (keyWord.value == '') {
    isSearchMode.value = false
    return
  }
  if (init) {
    uaPageNum = 0
    searchArticlesList.value = []
  }
  isSearchMode.value = true
  searchLoading.value = true
  try {
    const res: AjaxResult<Articles[]> = await request(`/api/search/keyWord/${route.params.userId}`, {
      params: {
        pageNum: uaPageNum++,
        pageSize: uaPageSize,
        keyWord: keyWord.value,
        userId: route.params.userId
      }
    })
    if (res) {
      searchArticlesList.value = [...searchArticlesList.value, ...res.data as Articles[]]
      if (res.data?.length === 0) {
        usNomore.value = true
      }
    }
  } catch (error) {

  } finally {
    searchLoading.value = false
  }
}

const searchIntersectionObserver = new IntersectionObserver((entries) => {
  // 如果 intersectionRatio 为 0，则目标在视野外，
  // 我们不需要做任何事情。
  if (entries[0].intersectionRatio <= 0) return;
  searchUserArticles(false)
});

/**
 * (取消)置顶文章
 */
const pinnedArticle = async (article: Articles, flag: boolean) => {
  const res: AjaxResult<any> = await request(`/api/articles/pinned/${article.articleId}/${flag}`, { method: 'post' })
  if (res.code == 200) {
    message.success(res.msg)
    if (flag) {
      await getUserPublishPinnedArticlesId()
      articlesList.value = articlesList.value.filter(v => {
        return v.articleId != article.articleId
      })
    } else {
      pinnedArticlesList.value = pinnedArticlesList.value.filter(v => v.articleId != article.articleId)
      articlesList.value.unshift(article)
    }
  } else {
    message.error(res.msg)
  }
}

onMounted(() => {
  nextTick(() => {
    intersectionObserver.observe(bottomEle.value)
    searchIntersectionObserver.observe(searchBottomEle.value)
  })
  getUserPublishPinnedArticlesId()
})

defineExpose({ getUserPublishArticlesId, reload })

</script>
 
<template>
  <div class="flex-1 ml-8 rounded-xl max-sm:mt-7 max-sm:ml-0 max-sm:pb-3" style="box-shadow: 1px 1px 6px #dbdbdb;">
    <h2 class="px-6 py-4 flex items-center max-sm:py-3 max-sm:px-4">
      <i class="iconfont text-gray-600 mr-3">&#xe618;</i>
      <span>文章列表</span>
      <div class="flex-1 ml-3 max-w-md">
        <n-input-group>
          <n-input placeholder="搜索文章（标题，内容，标签）" v-model:value="keyWord" :on-clear="() => { isSearchMode = false }"
            clearable @keydown.enter="searchUserArticles()" />
          <n-button type="primary" @click="searchUserArticles()">
            <template #icon>
              <n-icon>
                <Search />
              </n-icon>
            </template>
          </n-button>
        </n-input-group>
      </div>
    </h2>
    <div>
      <!-- 每一项 -->
      <div v-show="!isSearchMode">
        <!-- 置顶 -->
        <div v-for="item in pinnedArticlesList" :key="item.articleId" @click="handleClickItem(item.articleId as number)"
          class="group/item border-t border-gray-30 p-6 cursor-pointer hover:bg-gray-100 relative bg-gray-50">
          <!-- 置顶图钉 -->
          <div class="absolute top-2 left-2">
            <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;" :arrow-style="{ background: '#2A2A2A' }">
              <template #trigger>
                <i class="iconfont text-base text-gray-500 hover:text-primary-color">&#xe651;</i>
              </template>
              <span>此文章为置顶文章</span>
            </n-popover>
          </div>
          <div class="flex items-center">
            <n-avatar round :src="item.publishUser?.userAvatar" />
            <span class="text-gary-600 ml-3 text-base">{{ item.publishUser?.nickName }}</span>
            <span class="ml-3 text-gray-500">{{ moment(item.createTime, 'YYYY/MM/DD HH:mm:ss').startOf('hour').fromNow()
            }}</span>
            <!-- 标签 -->
            <div class="flex justify-between flex-1 h-5">
              <div class="flex ml-3">
                <div class="text-gray-500 mr-3" v-for="tag in item.tagInfoList">
                  {{ tag.tagName }}
                </div>
              </div>
              <!-- 右侧按钮 -->
              <div class="hidden group-hover/item:inline-block">
                <div class="flex" v-if="globalStore.userInfo?.userId.toString() === route.params.userId">
                  <span @click="(e) => e.stopPropagation()">
                    <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;"
                      :arrow-style="{ background: '#2A2A2A' }">
                      <template #trigger>
                        <div class="hover:bg-primary-color group rounded-md w-6 h-6 flex items-center justify-center mr-1"
                          @click="pinnedArticle(item, false)">
                          <i class="iconfont text-lg text-primary-color group-hover:text-white">&#xe6d8;</i>
                        </div>
                      </template>
                      <span>取消置顶</span>
                    </n-popover>
                  </span>
                  <span @click="(e) => e.stopPropagation()">
                    <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;"
                      :arrow-style="{ background: '#2A2A2A' }">
                      <template #trigger>
                        <div class="hover:bg-primary-color group rounded-md w-6 h-6 flex items-center justify-center mr-1"
                          @click="editArticle(item)">
                          <i class="iconfont text-lg text-primary-color group-hover:text-white">&#xe602;</i>
                        </div>
                      </template>
                      <span>编辑文章</span>
                    </n-popover>
                  </span>
                  <span @click="(e) => e.stopPropagation()">
                    <n-popconfirm @positive-click="handleDeleteClick(item.articleId as number)">
                      <template #trigger>
                        <div class="hover:bg-red-400 rounded-md w-6 h-6 group flex items-center justify-center">
                          <i class="iconfont text-xl text-red-500 group-hover:text-white">&#xeaf2;</i>
                        </div>
                      </template>
                      <span>删除文章</span>
                    </n-popconfirm>
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="flex w-full justify-end">
            <div class="w-full">
              <!-- 内容部分 -->
              <div class="mt-3">
                <div class="text-base" v-if="item.articleTitle">{{ item.articleTitle }}</div>
                <div class="text-base italic" v-else>未命名的标题</div>
                <div class="line-clamp-3 mt-3 text-gray-600">
                  {{ item.rawText }}
                </div>
              </div>
              <!-- 图片 -->
              <div class="w-36 mt-3 overflow-hidden" v-if="item.articleImage != '' && item.articleImage != null">
                <img class="rounded-md object-cover" :src="item.articleImage" alt="">
              </div>
              <!-- 数据部分 -->
              <div class="mt-3">
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe7d0;</i>{{ item.articlesData?.view }}</span>
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe8c3;</i>{{ item.articlesData?.like }}</span>
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe687;</i>{{ item.articlesData?.comment
                }}</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 不置顶 -->
        <div v-for="item in articlesList" :key="item.articleId" @click="handleClickItem(item.articleId as number)"
          class="group/item border-t border-gray-30 p-6 cursor-pointer hover:bg-gray-50">
          <div class="flex items-center">
            <n-avatar round :src="item.publishUser?.userAvatar" />
            <span class="text-gary-600 ml-3 text-base">{{ item.publishUser?.nickName }}</span>
            <span class="ml-3 text-gray-500">{{ moment(item.createTime, 'YYYY/MM/DD HH:mm:ss').startOf('hour').fromNow()
            }}</span>
            <!-- 标签 -->
            <div class="flex justify-between flex-1 h-5">
              <div class="flex ml-3">
                <div class="text-gray-500 mr-3" v-for="tag in item.tagInfoList">
                  {{ tag.tagName }}
                </div>
              </div>
              <!-- 右侧按钮 -->
              <div class="hidden group-hover/item:inline-block">
                <div class="flex" v-if="globalStore.userInfo?.userId.toString() === route.params.userId">
                  <span @click="(e) => e.stopPropagation()">
                    <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;"
                      :arrow-style="{ background: '#2A2A2A' }">
                      <template #trigger>
                        <div class="hover:bg-primary-color group rounded-md w-6 h-6 flex items-center justify-center mr-1"
                          @click="pinnedArticle(item, true)">
                          <i class="iconfont text-base text-primary-color group-hover:text-white">&#xe62a;</i>
                        </div>
                      </template>
                      <span>置顶文章</span>
                    </n-popover>
                  </span>
                  <span @click="(e) => e.stopPropagation()">
                    <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;"
                      :arrow-style="{ background: '#2A2A2A' }">
                      <template #trigger>
                        <div class="hover:bg-primary-color group rounded-md w-6 h-6 flex items-center justify-center mr-1"
                          @click="editArticle(item)">
                          <i class="iconfont text-lg text-primary-color group-hover:text-white">&#xe602;</i>
                        </div>
                      </template>
                      <span>编辑文章</span>
                    </n-popover>
                  </span>
                  <span @click="(e) => e.stopPropagation()">
                    <n-popconfirm @positive-click="handleDeleteClick(item.articleId as number)">
                      <template #trigger>
                        <div class="hover:bg-red-400 rounded-md w-6 h-6 group flex items-center justify-center">
                          <i class="iconfont text-xl text-red-500 group-hover:text-white">&#xeaf2;</i>
                        </div>
                      </template>
                      <span>删除文章</span>
                    </n-popconfirm>
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="flex w-full justify-end">
            <div class="w-full">
              <!-- 内容部分 -->
              <div class="mt-3">
                <div class="text-base" v-if="item.articleTitle">{{ item.articleTitle }}</div>
                <div class="text-base italic" v-else>未命名的标题</div>
                <div class="line-clamp-3 mt-3 text-gray-600">
                  {{ item.rawText }}
                </div>
              </div>
              <!-- 图片 -->
              <div class="w-36 mt-3 overflow-hidden" v-if="item.articleImage != '' && item.articleImage != null">
                <img class="rounded-md object-cover" :src="item.articleImage" alt="">
              </div>
              <!-- 数据部分 -->
              <div class="mt-3">
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe7d0;</i>{{ item.articlesData?.view }}</span>
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe8c3;</i>{{ item.articlesData?.like }}</span>
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe687;</i>{{ item.articlesData?.comment
                }}</span>
              </div>
            </div>
          </div>
        </div>

        <div ref="bottomEle" class="w-full text-center" v-show="!nomore">
          <n-spin size="small" />
        </div>
        <n-divider dashed v-show="nomore">
          <span class="text-gray-500 text-sm">到底啦~</span>
        </n-divider>
      </div>
      <!-- 搜索部分 -->
      <div v-show="isSearchMode">
        <div v-for="item in searchArticlesList" :key="item.articleId" @click="handleClickItem(item.articleId as number)"
          class="group/item border-t border-gray-30 p-6 cursor-pointer hover:bg-gray-50">
          <div class="flex items-center">
            <n-avatar round :src="item.publishUser?.userAvatar" />
            <span class="text-gary-600 ml-3 text-base">{{ item.publishUser?.nickName }}</span>
            <span class="ml-3 text-gray-500">{{ moment(item.createTime, 'YYYY/MM/DD HH:mm:ss').startOf('hour').fromNow()
            }}</span>
            <!-- 标签 -->
            <div class="flex justify-between flex-1 h-5">
              <div class="flex ml-3">
                <div class="text-gray-500 mr-3" v-for="tag in item.tagInfoList">
                  {{ tag.tagName }}
                </div>
              </div>
              <!-- 右侧按钮 -->
              <div class="hidden group-hover/item:inline-block">
                <div class="flex" v-if="globalStore.userInfo?.userId.toString() === route.params.userId">
                  <span @click="(e) => e.stopPropagation()">
                    <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;"
                      :arrow-style="{ background: '#2A2A2A' }">
                      <template #trigger>
                        <div class="hover:bg-primary-color group rounded-md w-6 h-6 flex items-center justify-center mr-1"
                          @click="editArticle(item)">
                          <i class="iconfont text-lg text-primary-color group-hover:text-white">&#xe602;</i>
                        </div>
                      </template>
                      <span>编辑文章</span>
                    </n-popover>
                  </span>
                  <span @click="(e) => e.stopPropagation()">
                    <n-popconfirm @positive-click="handleDeleteClick(item.articleId as number)">
                      <template #trigger>
                        <div class="hover:bg-red-400 rounded-md w-6 h-6 group flex items-center justify-center">
                          <i class="iconfont text-xl text-red-500 group-hover:text-white">&#xeaf2;</i>
                        </div>
                      </template>
                      <span>删除文章</span>
                    </n-popconfirm>
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="flex w-full justify-end">
            <div class="w-full">
              <!-- 内容部分 -->
              <div class="mt-3">
                <div class="text-base" v-if="item.articleTitle">{{ item.articleTitle }}</div>
                <div class="text-base italic" v-else>未命名的标题</div>
                <div class="line-clamp-3 mt-3 text-gray-600">
                  {{ item.rawText }}
                </div>
              </div>
              <!-- 图片 -->
              <div class="w-36 mt-3 overflow-hidden" v-if="item.articleImage != '' && item.articleImage != null">
                <img class="rounded-md object-cover" :src="item.articleImage" alt="">
              </div>
              <!-- 数据部分 -->
              <div class="mt-3">
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe7d0;</i>{{ item.articlesData?.view }}</span>
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe8c3;</i>{{ item.articlesData?.like }}</span>
                <span class="text-gray-500 mr-3"><i class="iconfont mr-2">&#xe687;</i>{{ item.articlesData?.comment
                }}</span>
              </div>
            </div>
          </div>
        </div>

        <div ref="searchBottomEle" class="w-full text-center" v-show="!usNomore">
          <n-spin size="small" />
        </div>
        <n-divider dashed v-show="usNomore">
          <span class="text-gray-500 text-sm">到底啦~</span>
        </n-divider>
      </div>
    </div>
  </div>
</template>
 
<style lang='scss' scoped></style>