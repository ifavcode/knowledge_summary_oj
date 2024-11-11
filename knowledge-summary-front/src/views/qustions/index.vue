<script setup lang="ts">
import { PlusOutlined } from '@ant-design/icons-vue';
import { getQuestionPageApi } from '~/api/QuestionApi';
import { Page, Questions } from '~/interface/base';


const searchValue = ref('')
const questionPageObj = ref<Page<Questions> | undefined>()
const router = useRouter()

function onSearch() {
  getQuestionPage()
}

async function getQuestionPage() {
  questionPageObj.value = undefined
  const res = await getQuestionPageApi(searchValue.value)
  questionPageObj.value = res.data
}

const diffLevelMap: { [key: string]: string } = {
  0: '简单',
  1: '普通',
  2: '困难'
}

function openQuestion(questionCode: string) {
  window.open('/#/questions/' + questionCode)
}

function showSubmitDetails(token: string) {
  window.open('/#/submissions/' + token)
}

function addQuestion() {
  router.push('/questions/publish')
}

onMounted(() => {
  getQuestionPage()
})

</script>

<template>
  <div class="mt-20 w-[1200px] mx-auto max-sm:w-full flex justify-between">
    <div class="w-[890px]">
      <n-space>
        <a-input-search v-model:value="searchValue" placeholder="题目编号或题目标题" style="width: 200px" @search="onSearch" />
      </n-space>
      <div class="w-full mt-4">
        <table class="border-collapse border-slate-400 w-full">
          <thead class="sticky top-12 bg-white shadow-sm">
            <tr class="h-12">
              <td class="px-2">状态</td>
              <td class="px-2 w-96" style="border-right: none;">题目</td>
              <td class="px-2 text-right" style="border-left: none;">显示标签</td>
              <td class="px-2">AC / 尝试</td>
              <td class="px-2">难度</td>
            </tr>
          </thead>
          <tbody class="text-sm">
            <tr v-for="item in questionPageObj?.records">
              <td style="padding: 0;margin: 0;" class="h-full">
                <div class="border-l-4 h-11 font-light flex items-center px-2 "
                  :class="item.questionDesc === null ? 'border-none' : item?.acFlag ? 'border-green-500 text-green-500' : 'border-red-500'">
                  <p class="hover:underline cursor-pointer" @click="showSubmitDetails(item.token as string)">{{
                    item.questionDesc
                    ||
                    '' }}</p>
                </div>
              </td>
              <td class=" hover:underline hover:text-primary-color cursor-pointer" style="border-right: none;"
                @click="openQuestion(item.questionCode)">
                {{ item.questionCode }}
                {{ item.title }}</td>
              <td style="border-left: none;text-align: right;">
                <a-tag v-for="tag in item.tagList">{{ tag.tagName }}</a-tag>
              </td>
              <td>
                <p class="text-gray-600">{{ item.totalAc + ' / ' + item.totalTry }}</p>
              </td>
              <td>
                <a-tag v-show="item.diffLevel" :color="item.diffLevel === '0' ? 'green' : item.diffLevel === '1' ? 'yellow' : 'red'">
                  {{ diffLevelMap[item.diffLevel || '0'] }}
                </a-tag>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div class="w-[280px] h-[300px] mt-12 rounded-sm">
      <div class="flex gap-2 items-center hover:text-primary-color cursor-pointer hover:underline w-fit"
        @click="addQuestion">
        <PlusOutlined />
        <p>新增题目</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
th,
td {
  @apply border-[1px];
  @apply border-gray-300;
}
</style>