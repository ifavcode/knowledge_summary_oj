<script setup lang='ts'>
import { useAxios } from '@vueuse/integrations/useAxios'
import { client } from '~/utils/request'
const { data: articlesList }: { data: Ref<AjaxResult<Articles[]> | undefined> } = useAxios('/api/articles/recommend/all', {

}, client)

const enterArticle = (article: Articles) => {
  window.open(window.location.origin + '#/article/' + article.articleId)
}

</script>
 
<template>
  <div class="contains_title">
    <h2 class="text-xl mb-2">推荐</h2>
    <div class="recommend_wrap">
      <n-scrollbar x-scrollable>
        <div class="box flex">
          <div v-for="_item in 3" class="recommend_item w-52 mr-6 rounded-md overflow-hidden" v-show="!articlesList">
            <n-skeleton height="67%" width="100%" />
            <n-skeleton class="mt-1" height="8%" width="30%" round />
            <div class="w-full h-8 mt-1 flex items-center">
              <n-skeleton class="mr-1" height="100%" width="30px" circle />
              <n-skeleton class="mr-1" width="50%" height="70%" round />
            </div>
            <n-skeleton class="mt-1" width="80%" round />
          </div>
          <div v-show="articlesList && articlesList?.data?.length === 0" class="text-gray-500">暂无推荐</div>
          <div
            class="recommend_item rounded-md w-52 mr-6 bg-white cursor-pointer shadow-md shadow-gray-200 ease-in-out duration-500"
            v-for="item in articlesList?.data" @click="enterArticle(item)">
            <div class="h-4/6">
              <img class="h-full rounded-t-md object-cover w-full" :src="item.articleImage" alt="">
            </div>
            <div class="py-1 px-3 flex flex-col justify-around">
              <div>
                <span class="line-clamp-1">{{ item.articleTitle || '未命名标题' }}</span>
              </div>
              <div class="flex items-center" style="height: 30px;">
                <n-avatar :src="item.publishUser?.userAvatar" round :size="25">
                </n-avatar>
                <span class="ml-2 text-gray-700">{{ item.publishUser?.nickName }}</span>
              </div>
              <div class="flex text-gray-500">
                <div class="mr-3">
                  <i class="iconfont mr-1">&#xe7d0;</i>
                  <span>{{ item.articlesData?.view }}</span>
                </div>
                <div class="mr-3">
                  <i class="iconfont mr-1">&#xe8c3;</i>
                  <span>{{ item.articlesData?.like }}</span>
                </div>
                <div class="mr-3">
                  <i class="iconfont mr-1">&#xe687;</i>
                  <span>{{ item.articlesData?.comment }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </n-scrollbar>
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.contains_title {

  @media screen and (max-width:480px) {
    padding-left: 10px;
  }

  h2 {
    color: #424242;
    font-weight: normal;
  }

  .recommend_wrap {
    width: 100%;
    max-width: 870px;
    height: fit-content;

    .box {
      .recommend_item {
        height: 240px;
      }
    }


  }
}
</style>