<script setup lang='ts'>
import { Search } from '@vicons/ionicons5'
import { request } from '~/utils/request';

const searchHistoryList = ref<SearchHistory[]>([])
const hotSearchHistoryList = ref<SearchHistory[]>([])
const keyWord = ref<string>('')

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

const searchList = ref<Articles[]>([])
const searchIsLoading = ref(false)
let inputChangeKey: NodeJS.Timeout;
const inputChange = (value: string) => {
  if(keyWord.value == value && searchList.value.length > 0) return
  keyWord.value = value
  clearTimeout(inputChangeKey)
  inputChangeKey = setTimeout(async () => {
    searchList.value = []
    searchIsLoading.value = true
    if (keyWord.value != '') {
      const res: AjaxResult<Articles[]> = await request('/api/search/articles', {
        params: {
          keyWord: keyWord.value
        }
      })
      searchList.value = res.data as Articles[]
    }
    searchIsLoading.value = false
    let reg = new RegExp(keyWord.value)
    searchList.value.map(v => {
      v.rawText = v.articleTitle
      v.articleTitle = v.articleTitle?.replace(reg, `<span style="color:#3F95FD"'>${keyWord.value}</span>`)
      return v
    })
  }, 500);
}

const searchContent = async () => {
  window.open(window.location.origin + '#/search?keyWord=' + keyWord.value)
  await request('/api/search/history/add', {
    method: 'post',
    data: {
      searchContent: keyWord.value
    }
  })
  getSearchHistoryList()
}

const chooseKeyWord = (word: string) => {
  keyWord.value = word
  searchContent()
}

</script>
 
<template>
  <div class="search_control" ref="searchControl">
    <div class="search_wrap">
      <n-input-group>
        <n-input clearable class="search_input" @keydown.enter="searchContent" placeholder="搜索..." round
          @focus="inputFocus" v-model:value="keyWord" :on-update:value="inputChange" />
        <n-button type="primary" round @click="searchContent">
          <template #icon>
            <n-icon>
              <Search />
            </n-icon>
          </template>
        </n-button>
      </n-input-group>

      <div class="tip_box" v-show="isInputFocus && (keyWord == '' || (keyWord != '' && searchList.length > 0))">
        <div class="empty_content" v-show="keyWord == ''" ref="emptyContent">
          <div class="history" v-show="searchHistoryList.length > 0">
            <p class="title">
            <p class="left">搜索历史</p>
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
            <p class="left">热门搜索</p>
            <div class="right" @click="clearSearchHistory"></div>
            </p>
            <div class="item_wrap">
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
          <div class="search_item" v-for="item in searchList" v-html="item.articleTitle" @click="chooseKeyWord(item.rawText as string)">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.search_control {
  display: flex;

  .search_wrap {
    display: flex;
    align-items: center;
    position: relative;

    .tip_box {
      position: absolute;
      width: 370px;
      height: fit-content;
      box-shadow: 0px 1px 3px #0000000a, 0px 2px 8px #00000014;
      background: #fff;
      z-index: 110;
      top: 50px;
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
        min-height: 55px;

        .search_item {
          height: 35px;
          padding: 0px 20px;
          line-height: 35px;
          cursor: pointer;
          text-overflow: ellipsis;
          overflow: hidden;

          &:hover {
            background: #E3E5E7;
          }
        }
      }


    }

    .search_input {
      width: 320px;
      transition: all .4s;
    }
  }
}
</style>