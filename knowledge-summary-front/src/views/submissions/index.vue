<script setup lang="ts">
import { LoadingOutlined } from '@ant-design/icons-vue';
import { getSubmissionDetailsApi } from '~/api/OJApi';
import { getSubmissionsDbApi } from '~/api/QuestionApi';
import * as monaco from 'monaco-editor';
import xcoode from 'monaco-themes/themes/Xcode_default.json'
import { getOnlineFileContentApi } from '~/api/CloudFileApi';
import { RS } from '~/interface/base';

const route = useRoute()
const router = useRouter()
const submitLoading = ref(false)
const isError = ref(false)
const detailsModalShow = ref(false)
const detailsIdx = ref(0)

const submisstionDetails = ref<Record<string, any>[]>([])
const submisstionDbDetails = ref<Record<string, any>>({})

let timer: NodeJS.Timeout
async function getSubmissionDetails(token: string) {
  const res = await getSubmissionDetailsApi(token)
  if (res.code === 200 && res.data) {
    isError.value = false
    submisstionDetails.value = res.data
  } else {
    isError.value = true
    submitLoading.value = false
    return
  }
  if (totalProcess.value >= 100) {
    submitLoading.value = false
    clearTimeout(timer)
    return
  }
  timer = setTimeout(() => {
    getSubmissionDetails(token)
  }, 1000);
}
const totalProcess = computed<number>(() => {
  let processCnt = submisstionDetails.value.reduce((pre, cur) => {
    pre += cur.status.id > 2 ? 1 : 0
    return pre
  }, 0)
  return processCnt / submisstionDetails.value.length * 100
})

let codeEditor: monaco.editor.IStandaloneCodeEditor
const codePreviewRef = ref()
function initPreview() {
  monaco.editor.defineTheme('xcode', xcoode as any)
  codeEditor = monaco.editor.create(codePreviewRef.value, {
    value: submisstionDbDetails.value.sourceCode,
    language: 'python',
    contextmenu: false,
    readOnly: true,
    automaticLayout: true,
    insertSpaces: true,
    fixedOverflowWidgets: true,
    minimap: {
      enabled: false
    },
    suggest: {
      preview: true,
    },
    theme: 'xcode',
    scrollBeyondLastLine: false,
    domReadOnly: true,
  });
  setEditorLanguage()
}

function setEditorLanguage() {
  if (codeEditor.getModel() != null) {
    let rawStr = submisstionDbDetails.value.languages.languageName
    let idx = rawStr.indexOf('(')
    if (idx > -1) {
      rawStr = rawStr.slice(0, idx)
    }
    rawStr = rawStr.toLocaleLowerCase().trim()
    monaco.editor.setModelLanguage(codeEditor.getModel()!, rawStr)
  }
}

async function getSubmissionsDb(token: string) {
  const res = await getSubmissionsDbApi(token)
  if (res.data) {
    submisstionDbDetails.value = res.data
    initPreview()
  }
}

let curInput = ref('')
let curOutput = ref('')
async function showDetails(index: number) {
  curInput.value = ''
  curOutput.value = ''
  detailsModalShow.value = true
  detailsIdx.value = index
  curInput.value = await getOnlineFileContentApi(submisstionDetails.value[index].inFile.fileUrl as string)
  curOutput.value = await getOnlineFileContentApi(submisstionDetails.value[index].outFile.fileUrl as string)
}

const totalTime = computed(() => {
  return submisstionDetails.value.reduce((pre: number, cur: RS) => {
    pre += cur.time * 1
    return pre
  }, 0) + 's'
})

const maxTime = computed(() => {
  let mx = 0
  submisstionDetails.value.forEach(v => {
    if (v.time * 1 > mx) {
      mx = v.time * 1
    }
  })
  return (mx == 0 ? '' : mx) + 's'
})

const maxMemory = computed(() => {
  let mx = 0
  submisstionDetails.value.forEach(v => {
    if (v.memory * 1 > mx) {
      mx = v.time * 1
    }
  })
  return (mx == 0 ? '' : mx) + 'MB'
})

function openProfile(path: string) {
  window.open(router.options.history.base + path)
}

onMounted(() => {
  getSubmissionDetails(route.params.token as string)
  getSubmissionsDb(route.params.token as string)
})

onBeforeUnmount(() => {
  clearInterval(timer)
})

</script>

<template>
  <div class="mt-20 w-[1200px] mx-auto max-sm:w-full flex justify-between">
    <div class="w-[890px]">
      <div class="text-center" v-if="submitLoading">
        <LoadingOutlined />
      </div>
      <div v-if="isError">
        <div class="w-full flex justify-center flex-col items-center bg-gray-50 pb-6 rounded-md h-80">
          <img src="../../assets/not_found/empty.svg" alt="">
          <p class="text-gray-600">未查到此次提交的内容</p>
          <p class="text-primary-color underline cursor-pointer mt-2"
            @click="getSubmissionDetails(route.params.token as string)">重新加载</p>
          <p class="text-primary-color underline cursor-pointer mt-2" @click="$router.back()">返回</p>
        </div>
      </div>
      <div v-else>
        <div>
          <h1 v-if="submisstionDbDetails.questions" class="text-2xl hover:underline cursor-pointer w-fit"
            @click="$router.push('/questions/' + (submisstionDbDetails && submisstionDbDetails.questions.questionCode))">
            {{
              submisstionDbDetails.questions.questionCode
            }}
            {{
              submisstionDbDetails.questions.title
            }}
          </h1>
          <p class="text-xs text-gray-500">{{ submisstionDetails.length }}个测试样例</p>
        </div>
        <div class="w-full mt-4 shadow-sm">
          <div class="w-full h-10 flex items-center p-2">
            <p class="w-1/4">#</p>
            <p class="w-1/4">状态</p>
            <p class="w-1/4">内存</p>
            <p class="w-1/4">用时</p>
          </div>
          <div v-for="(item, index) in submisstionDetails" title="查看详情" @click="showDetails(index)"
            class="w-full h-10 flex items-center p-2 border-l-4 cursor-pointer transition-all duration-50"
            :class="item.status.id < 3 ? 'border-gray-500 bg-gray-50 hover:gray-100' :
              item.status.id === 3 ? 'border-green-500 bg-green-50 hover:bg-green-100' : 'border-red-500 bg-red-50 hover:bg-red-100'">
            <p class="w-1/4">{{ item.inFile.fileName }}</p>
            <p class="w-1/4 font-semibold"
              :class="item.status.id < 3 ? 'text-gray-500' : item.status.id === 3 ? 'text-green-500' : 'text-red-500'">
              {{
                item.status.description }}</p>
            <p class="w-1/4">{{ (item.memory / 1024).toFixed(2) }} MB</p>
            <p class="w-1/4">{{ item.time }}s</p>
          </div>
        </div>
        <!-- code -->
        <div class="w-full mt-8">
          <div ref="codePreviewRef" class="w-full h-[800px] max-sm:h-screen"></div>
        </div>
      </div>
    </div>
    <div class="w-[280px] h-[300px] bg-gray-50 p-4">
      <h1 class="text-lg">信息</h1>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">提交者</span>
        <a-avatar class="cursor-pointer" :src="submisstionDbDetails.user && submisstionDbDetails.user.userAvatar"
          @click="openProfile(`/profile/${submisstionDbDetails.user.userId}`)"
          :title="submisstionDbDetails.user && submisstionDbDetails.user.nickName" />
      </p>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">题目</span>
        <span class="text-primary-color font-semibold hover:underline cursor-pointer"
          @click="$router.push('/questions/' + (submisstionDbDetails && submisstionDbDetails.questions.questionCode))">
          {{ submisstionDbDetails.questions && submisstionDbDetails.questions.questionCode }}
          {{ submisstionDbDetails.questions && submisstionDbDetails.questions.title }}
        </span>
      </p>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">语言</span>
        <span>{{ submisstionDbDetails.languages && submisstionDbDetails.languages.languageName }}</span>
      </p>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">提交时间</span>
        <span>{{ submisstionDbDetails.questions && submisstionDbDetails.questions.createTime }}</span>
      </p>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">总耗时</span>
        <span>{{ totalTime }}</span>
      </p>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">峰值时间</span>
        <span>{{ maxTime }}</span>
      </p>
      <p class="mt-3">
        <span class="text-gray-700 text-sm inline-block w-20">峰值内存</span>
        <span>{{ maxMemory }}</span>
      </p>
    </div>

    <a-modal v-model:open="detailsModalShow" title="提交详情">
      <p>输入：</p>
      <p class="whitespace-pre-line w-full bg-gray-100 px-4 py-2 rounded-md my-2 max-h-44 overflow-y-auto">
        {{ curInput }}
      </p>
      <p>预期输出：</p>
      <p class="whitespace-pre-line w-full bg-gray-100 px-4 py-2 rounded-md my-2 max-h-44 overflow-y-auto">
        {{ curOutput }}
      </p>
      <p>输出：</p>
      <p class="whitespace-pre-line w-full bg-gray-100 px-4 py-2 rounded-md my-2 max-h-44 overflow-y-auto">
        {{ submisstionDetails[detailsIdx].stdout }}
      </p>
      <p>结果：</p>
      <p class="font-semibold text-base"
        :class="submisstionDetails[detailsIdx].status.id === 3 ? 'text-green-500' : 'text-red-500'">
        {{ submisstionDetails[detailsIdx].status.description }}
      </p>
      <p class="text-red-500">{{ submisstionDetails[detailsIdx].stderr }}</p>
      <template #footer>

      </template>
    </a-modal>
  </div>
</template>
