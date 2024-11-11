<script setup lang="ts">
import { getLastAcUploadApi, getQuestionDetailsApi } from '~/api/QuestionApi';
import { Questions, Submissions } from '~/interface/base';
//@ts-ignore
import VditorPreview from 'vditor/dist/method.min.js'
import 'vditor/dist/index.css'
import * as monaco from 'monaco-editor';
import { SelectValue } from 'ant-design-vue/es/select';
import { DefaultOptionType } from 'ant-design-vue/es/vc-cascader';
import { execCodeTestApi, getLanguagesApi, getSubmissionDetailsApi, getSubmissionsSelfApi, queryTestStatusApi, submitCodeApi } from '~/api/OJApi';
import { getExecCodeStatus } from '~/utils';
import Cookies from 'js-cookie';
import xcoode from 'monaco-themes/themes/Xcode_default.json'
import { useGlobalStore } from '~/store/globalStore';
import { EditOutlined } from '@ant-design/icons-vue';
import { RouterLink } from 'vue-router';
import { message } from 'ant-design-vue';


const route = useRoute()
const router = useRouter()
const question = ref<Questions>()
const questionRef = ref()
const codeEditorRef = ref()
const statusModelShow = ref(true)
const testModelShow = ref(true)
const submitLoading = ref(false)
const globalStore = useGlobalStore()

let curToken = ref('')
const barOption = reactive({
  languageId: 71
})
const languageMap: Record<string, string> = {}
const testInput = ref('')

const codeTypeChange = (value: SelectValue, _options: DefaultOptionType | DefaultOptionType[]) => {
  if (typeof value === 'number') {
    barOption.languageId = value
    setEditorLanguage()
    localStorage.setItem('preLanguageId', '' + value)
  }
};

function setEditorLanguage() {
  if (codeEditor.getModel() != null) {
    let rawStr = languageMap[barOption.languageId]
    let idx = rawStr.indexOf('(')
    if (idx > -1) {
      rawStr = rawStr.slice(0, idx)
    }
    rawStr = rawStr.toLocaleLowerCase().trim()
    monaco.editor.setModelLanguage(codeEditor.getModel()!, rawStr)
  }
}

async function getQuestionDetails() {
  const res = await getQuestionDetailsApi(route.params.questionCode as string)
  if (res.data) {
    question.value = res.data
    document.title = '【' + question.value?.questionCode + '】' + question.value?.title
  }
  renderQuestion()
}

function renderQuestion() {
  const localCodeStyleChange: string | null = localStorage.getItem('codeStyleChange')
  const localCodeLineChange: string | null = localStorage.getItem('codeLineChange')
  VditorPreview.preview(questionRef.value, question.value?.questionContent, {
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

let codeEditor: monaco.editor.IStandaloneCodeEditor
function initEditor() {
  monaco.editor.defineTheme('xcode', xcoode as any)
  codeEditor = monaco.editor.create(codeEditorRef.value, {
    value: Cookies.get('editCodeMap' + barOption.languageId),
    contextmenu: false,
    automaticLayout: true,
    minimap: {
      enabled: false
    },
    theme: 'xcode',
    scrollBeyondLastLine: false
  });
  if (localStorage.getItem('editorWidth')) {
    const { left, right } = JSON.parse(localStorage.getItem('editorWidth') as string)
    leftRef.value.style.width = left
    rightRef.value.style.width = right
  } else {
    leftRef.value.style.width = Math.floor(document.body.clientWidth / 2) - 4 + 'px'
    rightRef.value.style.width = Math.floor(document.body.clientWidth / 2) + 'px'
  }
  codeEditor.layout()
}

let endTime: NodeJS.Timeout
async function submitCode() {
  try {
    submisstionDetails.value = []
    submitLoading.value = true
    curToken.value = ''
    const res = await submitCodeApi({
      languageId: barOption.languageId,
      sourceCode: codeEditor.getValue(),
      questionCode: route.params.questionCode as string
    })
    if (res.token) {
      curToken.value = res.token
      getSubmissionDetails(curToken.value)
    } else {
      message.error('error')
    }
  } catch (error) {
    submitLoading.value = false
    setTimeout(() => {
      clearTimeout(endTime)
    }, 500);
  } finally {
    // 10s保底
    endTime = setTimeout(() => {
      submitLoading.value = false
    }, 10000);
  }
}

const submisstionDetails = ref<Record<string, any>[]>([])
let timer: NodeJS.Timeout
async function getSubmissionDetails(token: string) {
  const res = await getSubmissionDetailsApi(token)
  if (res.data) {
    submisstionDetails.value = res.data
  }
  if (totalProcess.value >= 100) {
    submitLoading.value = false
    clearTimeout(timer)
    clearTimeout(endTime)
    return
  }
  timer = setTimeout(() => {
    getSubmissionDetails(token)
  }, 1000);
}

function colorMap(p: string) {
  switch (p) {
    case 'Accepted':
      return 'text-green-500'
    case 'Wrong Answer':
      return 'text-red-500'
    case 'Time Limit Exceeded':
      return 'text-yellow-500'
    default:
      return 'text-black'
  }
}

const totalProcess = computed<number>(() => {
  let processCnt = submisstionDetails.value.reduce((pre, cur) => {
    pre += cur.status.id > 2 ? 1 : 0
    return pre
  }, 0)
  return processCnt / submisstionDetails.value.length * 100
})
const submisstionComputedMap = computed(() => {
  let backMap: Record<string, number> = {}
  // if (!backMap[getExecCodeStatus(3)]) backMap[getExecCodeStatus(3)] = 0
  // if (!backMap[getExecCodeStatus(4)]) backMap[getExecCodeStatus(4)] = 0
  // if (!backMap[getExecCodeStatus(5)]) backMap[getExecCodeStatus(5)] = 0
  submisstionDetails.value.forEach(details => {
    if (!backMap[getExecCodeStatus(details.status.id as number)]) {
      backMap[getExecCodeStatus(details.status.id as number)] = 0
    }
    backMap[getExecCodeStatus(details.status.id as number)] += 1
  })
  return backMap
})

const languagesList = ref<Record<string, any>[]>([])
async function getLanguages() {
  const res = await getLanguagesApi()
  if (res.data != null) {
    languagesList.value = res.data
    res.data.map(v => {
      languageMap[v.id] = v.name
    })
  }
  initLanguage()
}

let saveCodeTimer: NodeJS.Timer
function autoSaveCode() {
  saveCodeTimer = setInterval(() => {
    Cookies.set('editCodeMap' + barOption.languageId, codeEditor.getValue(), {
      expires: 7
    })
  }, 5000);
}

let testCodeTimer: NodeJS.Timer
let endTestCodeTimer: NodeJS.Timer
let testCodeLoading = ref(false)
const testDetailsObj = ref<Record<string, any>>({})
async function execCodeTest() {
  testCodeLoading.value = true
  endTestCodeTimer = setTimeout(() => {
    clearInterval(testCodeTimer)
    testCodeLoading.value = false
  }, 20000);
  const res = await execCodeTestApi({
    questionCode: route.params.questionCode as string,
    sourceCode: codeEditor.getValue(),
    languageId: barOption.languageId,
    stdin: testInput.value
  })
  if (res.token) {
    testCodeTimer = setInterval(async () => {
      const resInner = await queryTestStatusApi(res.token)
      testDetailsObj.value = resInner
      if (resInner.status.id >= 3) {
        clearInterval(testCodeTimer)
        clearTimeout(endTestCodeTimer)
        testCodeLoading.value = false
      }
    }, 1000)
  }
}

const leftRef = ref()
const centerRef = ref()
const rightRef = ref()
const boxRef = ref()

function initCenterBar() {
  centerRef.value.onmousedown = (e: MouseEvent) => {
    const startX = e.clientX; // 记录坐标起始位置
    leftRef.value.left = leftRef.value.offsetWidth; // 左边元素起始宽度
    document.onmousemove = e => {
      const endX = e.clientX; // 鼠标拖动的终止位置
      console.log(leftRef.value.left);

      let moveLen = leftRef.value.left + (endX - startX); // 移动的距离 =  endX - startX，左边区域最后的宽度 = resizeDom.left + 移动的距离
      const maxWidth = boxRef.value.clientWidth - centerRef.value.offsetWidth; // 左右两边区域的总宽度 = 外层容器宽度 - 中间区域拖拉框的宽度
      // 限制左边区域的最小宽度为 leftMinWidth
      if (moveLen < 200) {
        moveLen = 200;
      }
      // 右边区域最小宽度为 rightMinWidth
      if (moveLen > maxWidth - 200) {
        moveLen = maxWidth - 200;
      }
      leftRef.value.style.width = (moveLen / maxWidth) * 100 + "%"; // 设置左边区域的宽度，通过换算为百分比的形式，实现窗体放大缩小自适应
      rightRef.value.style.width = ((maxWidth - moveLen) / maxWidth) * 100 + "%"; // 右边区域 = 总大小 - 左边宽度 - 拖动条宽度
    };
    document.onmouseup = () => {
      document.onmousemove = null;
      document.onmouseup = null;
      centerRef.value.releaseCapture && centerRef.value.releaseCapture(); // 鼠标捕获释放
      codeEditor.layout()
      localStorage.setItem('editorWidth', JSON.stringify({
        left: leftRef.value.style.width,
        right: rightRef.value.style.width
      }))
    };
    centerRef.value.setCapture && centerRef.value.setCapture(); // 启用鼠标捕获
    return false;
  };
}

function openTokenDetails(token: string) {
  window.open(router.options.history.base + '/submissions/' + token)
}

function initLanguage() {
  let preLanguageId = localStorage.getItem('preLanguageId')
  if (preLanguageId) {
    barOption.languageId = parseInt(preLanguageId)
  }
  setEditorLanguage()
}

// 侧边菜单栏
const menuIndex = ref(0)
let selfTimer: NodeJS.Timer
function toggleMenuIndex(index: number) {
  menuIndex.value = index
  if (index == 0) {
    clearTimeout(selfTimer)
    nextTick(() => {
      renderQuestion()
    })
  } else if (index === 1) {
    getSubmissionsSelf()
    getNewlySubmisstionsSelf()
  }
}

function getNewlySubmisstionsSelf() {
  selfTimer = setTimeout(async () => {
    await getSubmissionsSelf()
    getNewlySubmisstionsSelf()
  }, 5000);
}

const submissionList = ref<Submissions[]>([])
async function getSubmissionsSelf() {
  const res = await getSubmissionsSelfApi(route.params.questionCode as string)
  if (res.data) {
    submissionList.value = res.data
  }
}

function dyColor(v: number) {
  if (!v) return 'text-red-500'
  if (v <= 20) {
    return 'text-red-500'
  } else if (v <= 70) {
    return 'text-yellow-500'
  } else {
    return 'text-green-500'
  }
}

onMounted(() => {
  getQuestionDetails()
  initEditor()
  getLanguages()
  autoSaveCode()
  initCenterBar()
})

onBeforeUnmount(() => {
  clearInterval(saveCodeTimer)
  clearTimeout(timer)
  clearTimeout(endTime)
  clearInterval(testCodeTimer)
  clearTimeout(endTestCodeTimer)
  clearTimeout(selfTimer)
})

</script>

<template>
  <div class="mt-12 w-full flex" style="height: calc(100vh - 50px);" ref="boxRef">
    <div class="flex" ref="leftRef">
      <div class="w-[68px] h-full bg-gray-100 flex flex-col text-gray-600 items-center pt-6 gap-4">
        <div
          class="flex justify-center items-center flex-col w-full py-1 rounded-md hover:text-gray-100 hover:bg-primary-color cursor-pointer"
          :class="menuIndex === 0 ? 'text-gray-100 bg-primary-color' : ''" @click="toggleMenuIndex(0)">
          <p class="iconfont text-3xl">&#xe656;</p>
          <p class="text-sm">题目</p>
        </div>
        <div
          class="flex justify-center items-center flex-col w-full py-1 rounded-md hover:text-gray-100 hover:bg-primary-color cursor-pointer"
          :class="menuIndex === 1 ? 'text-gray-100 bg-primary-color' : ''" @click="toggleMenuIndex(1)">
          <p class="iconfont text-3xl">&#xeaf7;</p>
          <p class="text-sm">提交记录</p>
        </div>
      </div>
      <div class="p-6 flex-1" v-if="menuIndex === 0">
        <div class="flex gap-4 items-center">
          <p v-if="question?.questionDesc" @click="$router.push('/submissions/' + question.token)"
            class="px-2 h-6 border-l-4 font-thin flex items-center hover:underline cursor-pointer"
            :class="question?.acFlag ? 'border-green-500 text-green-500' : 'border-red-500'">
            {{ question?.questionDesc || '' }}
          </p>
          <h1 class="text-2xl">
            {{ question?.questionCode }}
            {{ question?.title }}
          </h1>
          <!-- 管理员工具条 -->
          <div v-if="globalStore.isAdmin">
            <RouterLink target="_blank" :to="`/questions/edit/${route.params.questionCode}`">
              <EditOutlined />
            </RouterLink>
          </div>
        </div>
        <div class="mt-4">
          <div ref="questionRef"></div>
        </div>
      </div>
      <div class="flex-1 overflow-y-auto" v-if="menuIndex === 1">
        <a-affix :offset-top="50">
          <div class="w-full h-10 flex items-center px-2 bg-gray-50 shadow-md text-sm">
            <p class="flex-[2]">#</p>
            <p class="flex-1">内存</p>
            <p class="flex-1">时间</p>
            <p class="flex-[2]">语言</p>
            <p class="flex-1 text-center"></p>
          </div>
        </a-affix>
        <div class="w-full h-10 flex items-center px-2 text-gray-600 text-[13px] hover:bg-gray-100 cursor-pointer"
          v-for="submission in submissionList" @click="openTokenDetails(submission.token as string)">
          <p class="flex-[2]">{{ submission.createTime }}</p>
          <p class="flex-1">
            {{ (submission.execResults && submission.execResults?.totalMemory) ? (submission.execResults?.totalMemory /
              1000).toFixed(2) + 'MB' : '' }}
          </p>
          <p class="flex-1">
            {{ (submission.execResults && submission.execResults?.totalTime) ? submission.execResults?.totalTime + 's' :
              '' }}
          </p>
          <p class="flex-[2]">{{ submission.languages?.languageName }}</p>
          <p class="flex-1 text-center" v-if="submission.execResults"
            :class="dyColor(submission.execResults?.acCnt / submission.execResults?.totalCnt * 100)">
            <span class="text-green-500" v-if="submission.execResults?.totalCnt === 0">100%</span>
            <span v-else>
              {{
                (submission.execResults?.acCnt / submission.execResults?.totalCnt * 100).toFixed(0)
              }}%
            </span>
          </p>
          <p class="flex-1 text-center" v-else>
            0%
          </p>
        </div>
      </div>
    </div>
    <div class="w-[2px] bg-gray-100 cursor-move hover:bg-primary-color transition-all duration-200" ref="centerRef">
    </div>
    <div class="relative" ref="rightRef">
      <div class="w-full h-11 bg-gray-100 flex items-center px-2">
        <n-space>
          <a-button class="flex items-center" @click="execCodeTest" :loading="testCodeLoading">
            <i class="iconfont">&#xe608;</i>
            <span class="ml-1">运行自测</span>
          </a-button>
          <a-button @click="submitCode" :loading="submitLoading" type="primary">
            <i class="iconfont">&#xe630;</i>
            <span class="ml-1">运行提交</span>
          </a-button>
          <a-select class="w-40" v-model:value="barOption.languageId" @change="codeTypeChange">
            <a-select-option :value="language.id" :label="language.name" :key="language.id"
              v-for="language in languagesList">{{ language.name }}</a-select-option>
          </a-select>
          <button @click="testModelShow = !testModelShow"
            class="w-14 h-full bg-pink-500 text-white rounded-md hover:bg-pink-400 transition-all duration-200">
            自测
          </button>
          <button @click="statusModelShow = !statusModelShow"
            class="w-14 h-full bg-pink-500 text-white rounded-md hover:bg-pink-400 transition-all duration-200">
            提交
          </button>
        </n-space>
      </div>
      <div ref="codeEditorRef" class="w-full" style="height: calc(100% - 2.75rem);">

      </div>
      <div v-show="statusModelShow || testModelShow" class="absolute bottom-0 w-full h-fit z-50">
        <div v-show="statusModelShow">
          <div class="h-8 flex items-center px-2 bg-primary-color">
            <p class="text-white mr-4">提交</p>
            <p class="text-white hover:underline cursor-pointer text-xs" v-show="curToken !== ''"
              @click="openTokenDetails(curToken)">查看详情</p>
          </div>
          <div class="w-full bg-white relative">
            <div class="bg-green-100 absolute h-full z-0 transition-all" :style="{ width: totalProcess + '%' }">
            </div>
            <div class="relative z-10  p-2">
              <div v-if="Object.keys(submisstionComputedMap).length !== 0" class="flex gap-8">
                <p v-for="k in Object.keys(submisstionComputedMap)" class="text-gray-600 font-semibold"
                  :class="colorMap(k)">
                  <span>{{ k }}</span>
                  <span class="px-1">×</span>
                  <span>{{ submisstionComputedMap[k] }}</span>
                </p>
              </div>
              <p v-else class="text-gray-600 text-sm">
                {{ submitLoading ? '等待...' : '暂未提交' }}
              </p>
            </div>
          </div>
        </div>
        <div v-show="testModelShow">
          <div class="h-8 flex items-center px-2 bg-primary-color">
            <p class="text-white">自测</p>
          </div>
          <div class="flex bg-white">
            <div class="w-1/2 h-52 px-2">
              <a-textarea v-model:value="testInput" placeholder="输入" :bordered="false"
                :autoSize="{ minRows: 8, maxRows: 8 }" />
            </div>
            <div class="w-1/2 border-l-2 h-52 py-2 px-4">
              <div v-if="Object.keys(testDetailsObj).length > 0">
                <p v-if="testDetailsObj.status.id <= 2" v-html="testDetailsObj.status.description"></p>
                <p v-else-if="testDetailsObj.status.id <= 4" class="whitespace-pre-line" v-html="testDetailsObj.stdout">
                </p>
                <p v-else v-html="testDetailsObj.status.description"></p>
                <p class="text-red-500" v-html="testDetailsObj.stderr"></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>