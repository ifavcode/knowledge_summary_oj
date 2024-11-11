<script setup lang='ts'>
import { request } from '~/utils/request';
import { Search } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui';

const searchHistoryList = ref<SearchHistory[]>([])
const hotSearchHistoryList = ref<SearchHistory[]>([])
const keyWord = ref<string>('')
const route = useRoute()
const router = useRouter()
const message = useMessage()

const getSearchHistoryList = async () => {
  const res: AjaxResult<SearchHistory[]> = await request('/api/search/history/all')
  searchHistoryList.value = res.data as SearchHistory[]
}

//删除搜索历史记录（1项）
const deleteSearchOne = async (item: SearchHistory) => {
  request('/api/search/history/' + item.searchId, {
    method: 'post'
  })
  searchHistoryList.value = searchHistoryList.value.filter(v => v.searchId != item.searchId)
}

//清空搜索历史纪录
const clearSearchHistory = async () => {
  request('/api/search/history/clear', {
    method: 'post'
  })
  searchHistoryList.value = []
}

//热门20条搜索
const getHotSearchHistory = async () => {
  const res: AjaxResult<SearchHistory[]> = await request('/api/search/history/hot')
  hotSearchHistoryList.value = res.data as SearchHistory[]
}

const emptyContent = ref<HTMLElement>()
const searchControl = ref<HTMLElement>()
let isInputFocus = ref<boolean>(false)
let key: NodeJS.Timeout
const inputFocus = async () => {
  inputChange(keyWord.value)
  clearTimeout(key)
  isInputFocus.value = true
  if (searchHistoryList.value.length == 0) {
    getSearchHistoryList()
  }
  if (hotSearchHistoryList.value.length == 0) {
    getHotSearchHistory()
  }
}
onClickOutside(emptyContent, () => {
  isInputFocus.value = false
}, {
  ignore: [searchControl]
})

const searchContent = async () => {
  await request('/api/search/history/add', {
    method: 'post',
    data: {
      searchContent: keyWord.value
    }
  })
  getSearchHistoryList()
}

const chooseKeyWord = (word: string) => {
  inputChange(word, () => {
    getArticleByIds()
  })
  searchContent()
  isInputFocus.value = false

}

/**
 * 索引相关属性方法
 * '综合排序', '最多赞同', '最多浏览', '最新发布'
 */
const articleIdx = ref(0)
const articleChildren = [
  {
    name: '综合排序',
    key: ''
  },
  {
    name: '最多赞同',
    key: 'love'
  },
  {
    name: '最多浏览',
    key: 'views'
  },
  {
    name: '最新发布',
    key: 'time'
  },
]
const handleArticleClick = async (index: number) => {
  articleIdx.value = index
  getArticleByIds(articleChildren[index].key)
}

const searchList = ref<Articles[]>([])
const isLoadingSearchList = ref(false)
const searchIsLoading = ref(false)
let inputChangeKey: NodeJS.Timeout;
const inputChange = (value: string, callback?: Function) => {
  isInputFocus.value = true
  if (keyWord.value != '' && keyWord.value == value) return
  keyWord.value = value
  clearTimeout(inputChangeKey)
  searchList.value = []
  searchIsLoading.value = true
  inputChangeKey = setTimeout(async () => {
    if (keyWord.value != '') {
      isLoadingSearchList.value = true
      const res: AjaxResult<Articles[]> = await request('/api/search/articles', {
        params: {
          keyWord: keyWord.value
        }
      })
      searchList.value = res.data as Articles[]
      let reg = new RegExp(keyWord.value)
      searchList.value.map(v => {
        v.rawText = v.articleTitle
        v.articleTitle = v.articleTitle?.replace(reg, `<span style="color:#3F95FD"'>${keyWord.value}</span>`)
        return v
      })
      isLoadingSearchList.value = false
      if (callback) {
        callback()
      }
    }
    searchIsLoading.value = false
  }, 100);
}

/**
 * 根据关键词搜索文章
 */
const articlesList = ref<Articles[]>([])
const isEmpty = ref(false)
let ids = "*"
let preType = "*"
const getArticleByIds = async (type: string = "") => {
  pageNum = 1
  nomore.value = false
  let tmpIds = ""
  searchList.value.forEach(v => {
    tmpIds += v.articleId + ","
  })
  if (ids == tmpIds && type == preType) {
    return
  }
  articlesList.value = []
  ids = tmpIds;
  preType = type
  isEmpty.value = false;
  if (ids == "") {
    isEmpty.value = true
    return
  }

  const res: AjaxResult<Articles[]> = await request('/api/articles/ids/' + ids, {
    params: {
      pageNum,
      pageSize,
      type
    }
  })
  articlesList.value = res.data as Articles[]
  isInputFocus.value = false
}

/**
 * 点击搜索按钮触发
 */
const handleSearchClick = async () => {
  if (keyWord.value == '') {
    message.warning('搜索内容不能为空')
    return
  }
  if (isLoadingSearchList.value) {
    return
  }
  if (curTab.value == 'article') {
    getArticleByIds()
  } else if (curTab.value == 'author') {
    getAuthorList()
  }
  //增加一条搜索历史
  await request('/api/search/history/add', {
    method: 'post',
    data: {
      searchContent: keyWord.value
    }
  })
}

const openArticle = (item: Articles) => {
  window.open(window.location.origin + '#/article/' + item.articleId)
}

/**
 * 加载更多
 */
let pageNum = 1
let pageSize = 10
const nomore = ref(false)
const bottomEle = ref()

const intersectionObserver = new IntersectionObserver((entries) => {
  // 如果 intersectionRatio 为 0，则目标在视野外，
  // 我们不需要做任何事情。
  if (entries[0].intersectionRatio <= 0) return;
  loadmore()
});

const loadmore = async () => {
  if (ids == "" || preType == "*") return
  if (!nomore.value) {
    const res: AjaxResult<Articles[]> = await request('/api/articles/ids/' + ids, {
      params: {
        pageNum: ++pageNum,
        pageSize,
        type: preType
      }
    })
    articlesList.value = [...articlesList.value, ...res.data as Articles[]]
    if (res.data?.length === 0) {
      nomore.value = true
    }
  }
}

/**
 * 作者相关
 */

const curTab = ref('article')
const authorIdx = ref(0)
const authorIsEmpty = ref(false)
const authorChildren = [
  {
    name: '综合排序',
    key: ''
  },
  {
    name: '文章浏览量最多',
    key: 'totalViews_m'
  },
  {
    name: '文章浏览量最少',
    key: 'totalViews_l'
  },
  {
    name: '文章赞同量最多',
    key: 'totalLike_m'
  },
  {
    name: '文章赞同量最少',
    key: 'totalLike_l'
  },
]

const handleAuthorClick = async (index: number) => {
  authorIdx.value = index
  const arr = authorChildren[index].key.split('_')
  getAuthorList({ type: arr[0], flag: arr[1] })
}

const authorList = ref<userInfo[]>([])
const authorToArticlesNum = ref<any>({})
const getAuthorList = async (p: any = {}) => {
  authorList.value = []
  authorToArticlesNum.value = {}
  authorIsEmpty.value = false
  const res: AjaxResult<AuthorsListType> = await request('/api/user/authors', {
    params: {
      keyWord: keyWord.value,
      ...p
    }
  })
  authorList.value = res.data?.userList as userInfo[]
  authorToArticlesNum.value = res.data?.authorToArticlesNum
  if (authorList.value.length == 0) {
    authorIsEmpty.value = true
  }
}

const tabsUpdate = (value: string) => {
  if (value == 'author' && authorList.value.length == 0) {
    getAuthorList()
  }
  console.log(value);

  curTab.value = value
}

const enterUserPage = (item: userInfo) => {
  router.push('/profile/' + item.userId)
}


onMounted(async () => {
  if (!route.query.keyWord || route.query.keyWord == '') {
    isEmpty.value = true
  } else {
    inputChange(route.query.keyWord as string, () => {
      getArticleByIds()
    })
    nextTick(() => {
      intersectionObserver.observe(bottomEle.value)
    })
  }
})
</script>
 
<template>
  <div class="search_wrap max-sm:px-3">
    <div class="center_input" ref="searchControl">
      <n-input-group>
        <n-input size="large" style="height: 60px;padding: 10px 0;" @focus="inputFocus" v-model:value="keyWord"
          :on-update:value="inputChange" @keydown.enter="handleSearchClick" clearable :on-blue="isInputFocus = false">
          <template #prefix>
            <n-icon size="20">
              <Search />
            </n-icon>
          </template>
        </n-input>
        <n-button type="primary" size="large" style="width: 80px;height: 60px;letter-spacing: 3px;"
          @click="handleSearchClick" :disabled="isLoadingSearchList">
          搜索
        </n-button>
      </n-input-group>

      <div class="tip_box" v-show="isInputFocus && (keyWord == '' || (keyWord != '' && searchList.length > 0))">
        <div class="empty_content" v-show="keyWord == ''" ref="emptyContent">
          <div class="history" v-show="searchHistoryList.length > 0">
            <p class="title">
            <p class="left flex items-center">
              <span class="max-sm:inline-block hidden mr-3 text-xl" @click="isInputFocus = false">×</span>
              <span>搜索历史</span>
            </p>
            <div class="right" @click="clearSearchHistory">清空</div>
            </p>
            <div class="search_history">
              <div class="history_item" v-for="item in searchHistoryList" :key="item.searchId"
                @click="chooseKeyWord(item.searchContent as string)">
                <span>{{ item.searchContent }}</span>
                <div class="close" @click.stop="deleteSearchOne(item)">×</div>
              </div>
            </div>
          </div>

          <div class="hot_recommend">
            <p class="title">
            <p class="left">
              <span class="max-sm:inline-block hidden mr-3 text-xl" @click="isInputFocus = false" v-if="searchHistoryList.length == 0">×</span>
              <span>热门搜索</span>
            </p>
            <div class="right" @click="clearSearchHistory"></div>
            </p>
            <div class="item_wrap mt-1">
              <div class="recommend_item" v-for="(item, index) in hotSearchHistoryList" :key="index">
                <span class="idx" @click="chooseKeyWord(item.searchContent as string)">{{ index + 1 }}</span>
                <span class="content" @click="chooseKeyWord(item.searchContent as string)">{{ item.searchContent }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="search_content" v-show="keyWord != ''">
          <div class="loading" style="width: 100%;text-align: center;padding: 20px 0;" v-show="searchIsLoading">
            <n-spin size="medium" />
          </div>
          <div class="search_item" v-for="item in searchList" v-html="item.articleTitle"
            @click="chooseKeyWord(item.rawText as string)">
          </div>
        </div>
      </div>
    </div>
    <n-tabs type="line" animated size="large" :on-update:value="tabsUpdate">
      <n-tab-pane name="article" tab="文章">
        <div class="article_wrap">
          <div class="func_btn_wrap overflow-x-auto">
            <div class="func_btn" v-for="(item, index) in articleChildren">
              <n-button :type="index === articleIdx ? 'primary' : undefined" size="large" secondary
                @click="handleArticleClick(index)">
                {{ item.name }}
              </n-button>
            </div>
          </div>
        </div>

        <div class="article_content_wrap">
          <n-grid x-gap="12" :cols="2" item-responsive>
            <n-gi v-if="articlesList.length == 0 && !isEmpty" span="2 800:1">
              <n-space vertical class="mt-3">
                <n-skeleton height="30px" width="33%" />
                <div class="flex items-center">
                  <n-skeleton height="30px" circle class="mr-3" />
                  <n-skeleton height="30px" width="66%" />
                </div>
                <n-skeleton height="30px" width="66%" :sharp="false" />
                <n-skeleton height="20px" width="66%" :sharp="false" />
                <n-skeleton height="20px" width="66%" :sharp="false" />
              </n-space>
            </n-gi>
            <n-gi v-if="articlesList.length == 0 && isEmpty" span="2 800:1">
              <p class="text-gray-500">未搜索到相关内容</p>
            </n-gi>
            <n-gi v-else v-for="item in articlesList" class="article_content_item" span="2 800:1">
              <div class="article_item p-3 cursor-pointer rounded-md" @click="openArticle(item)">
                <div class="w-full text-lg text-black">
                  {{ item.articleTitle }}
                </div>
                <!-- 作者 -->
                <div class="w-full mb-3 flex items-center mt-3">
                  <img :src="item.publishUser?.userAvatar" class="w-6 rounded-full" />
                  <span class="text-gray-600 text-sm ml-2">{{ item.publishUser?.nickName }}</span>
                  <span class="text-gray-500 text-xs ml-2 tracking-wider">{{ item.createTime }}</span>

                </div>
                <div class="text-gray-600 line-clamp-3 text-base">
                  {{ item.rawText }}
                </div>
                <!-- 标签 -->
                <div class="mt-2 flex flex-wrap">
                  <div v-for="tag in item.tagInfoList" class="mr-3 bg-gray-100 text-gray-500 px-2 py-1 rounded-xl">
                    {{ tag.tagName }}
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
              </div>
            </n-gi>
          </n-grid>
        </div>

        <n-divider dashed>
          <span class="text-gray-500 text-sm" ref="bottomEle">到底啦~</span>
        </n-divider>

      </n-tab-pane>
      <n-tab-pane name="author" tab="作者">
        <div class="article_wrap">
          <div class="func_btn_wrap overflow-x-auto">
            <div class="func_btn" v-for="(item, index) in authorChildren">
              <n-button :type="index === authorIdx ? 'primary' : undefined" size="large" secondary
                @click="handleAuthorClick(index)">
                {{ item.name }}
              </n-button>
            </div>
          </div>
        </div>

        <!-- 每一项 -->
        <div>
          <n-grid x-gap="12" :cols="2" item-responsive>
            <n-gi class="mt-4 p-3 rounded-md hover:bg-gray-50 cursor-pointer" span="2 800:1"
              v-if="authorList.length == 0 && !authorIsEmpty">
              <n-space vertical>
                <div class="flex items-center" style="height: 60px;">
                  <n-skeleton height="60px" width="60px" circle class="mr-3" />
                  <div class="flex w-80 h-full flex-col justify-around">
                    <n-skeleton height="20px" width="20%" />
                    <n-skeleton height="20px" width="60%" />
                  </div>
                </div>
                <n-skeleton height="20px" width="66%" :sharp="false" />
              </n-space>
            </n-gi>
            <n-gi class="mt-3" v-if="authorList.length == 0 && authorIsEmpty" span="2 800:1">
              <p class="text-gray-500">未搜索到相关内容</p>
            </n-gi>
            <n-gi v-for="item in authorList" class="mt-4 p-3 rounded-md hover:bg-gray-50 cursor-pointer" span="2 800:1"
              @click="enterUserPage(item)">
              <div class="flex items-center" style="height: 60px;">
                <div class="avatar mr-3">
                  <n-avatar round :size="60" :src="item.userAvatar"
                    fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg" />
                </div>
                <div class="h-full flex flex-col justify-around">
                  <div class="font-semibold text-lg tracking-wide">{{ item.nickName }}</div>
                  <div>{{ item.userDesc }}</div>
                </div>
              </div>
              <div class="flex mt-2">
                <div class="mr-3 text-gray-500">文章数量<span class="text-sm ml-2">{{
                  authorToArticlesNum[item.userId?.toString() as string].articleTotal }}</span></div>
                <div class="mr-3 text-gray-500">被浏览<span class="text-sm ml-2">{{
                  authorToArticlesNum[item.userId?.toString() as string].totalViews }}</span></div>
                <div class="mr-3 text-gray-500">被赞同<span class="text-sm ml-2">{{
                  authorToArticlesNum[item.userId?.toString() as string].totalLike }}</span></div>
              </div>
            </n-gi>
          </n-grid>
        </div>

      </n-tab-pane>
    </n-tabs>
  </div>
</template>
 
<style lang='scss' scoped>
.search_wrap {
  margin: 0 auto;
  max-width: 1200px;
  margin-top: 20px;
  width: 100%;
  min-width: 300px;

  .article_wrap {
    .func_btn_wrap {
      display: flex;

      .func_btn {
        margin-right: 20px;

      }
    }
  }

  .article_content_wrap {
    margin-top: 10px;

    .article_content_item {

      &:nth-child(n + 3) {
        margin-top: 10px;
      }

      .article_item {
        &:hover {
          background: #F7F7F8;
        }

      }
    }
  }


  .center_input {
    max-width: 800px;
    margin: 50px auto;
    position: relative;

    @media screen and (max-width:480px) {
      margin: 10px auto;
    }

    .tip_box {
      position: absolute;
      width: 100%;
      height: fit-content;
      box-shadow: 0px 1px 3px #0000000a, 0px 2px 8px #00000014;
      background: #fff;
      z-index: 110;
      top: 63px;
      border-radius: 10px;

      .empty_content {
        padding: 10px 20px;

        .hot_recommend {
          .title {
            height: 24px;
            font-size: 16px;
            line-height: 24px;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
          }

          .item_wrap {
            display: flex;
            flex-wrap: wrap;

            .recommend_item {
              width: 50%;
              margin-top: 5px;
              border-radius: 10px;
              cursor: pointer;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;

              &:nth-child(-n + 3) {
                .idx {
                  color: #fff;
                  background: $primaryColor;
                }

              }

              .idx {
                display: inline-block;
                width: 20px;
                height: 20px;
                text-align: center;
                line-height: 21px;
                border-radius: 50%;
                margin-right: 10px;
                font-size: 12px;
              }

              .content {
                &:hover {
                  color: $primaryColor;
                }
              }
            }
          }
        }

        .history {
          .title {
            height: 24px;
            font-size: 16px;
            line-height: 24px;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;

            .left {}

            .right {
              font-size: 12px;
              line-height: 15px;
              height: 15px;
              color: #9499a0;
              cursor: pointer;
            }

          }

          .search_history {
            display: flex;
            flex-wrap: wrap;
            margin-top: 12px;
            margin-right: -10px;
            margin-bottom: 4px;

            .history_item {
              position: relative;
              box-sizing: border-box;
              height: 30px;
              padding: 7px 10px 8px;
              font-size: 12px;
              line-height: 15px;
              background: #f6f7f8;
              border-radius: 4px;
              margin-right: 10px;
              margin-bottom: 10px;
              cursor: pointer;

              &:hover {
                color: $primaryColor;

                .close {
                  display: flex;
                  justify-content: center;
                  align-items: center;
                }
              }

              span {
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                max-width: 96px;
                display: inline-block;
              }

              .close {
                position: absolute;
                width: 14px;
                height: 14px;
                top: -4px;
                right: -4px;
                padding: 2px;
                font-size: 12px;
                background: #CCCCCC;
                display: none;
                border-radius: 50%;
                color: #fff;
              }
            }
          }
        }
      }

      .search_content {
        padding: 10px 0;
        min-height: 60px;

        .search_item {
          height: 40px;
          padding: 0px 20px;
          line-height: 40px;
          cursor: pointer;

          &:hover {
            background: #E3E5E7;
          }
        }
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
}
</style>