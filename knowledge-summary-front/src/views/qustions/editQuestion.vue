<script setup lang="ts">
import Cookies from 'js-cookie';
import Vditor from 'vditor';
import { useGlobalStore } from '~/store/globalStore';
import 'vditor/dist/index.css'
import { editQuestionTestcaseApi, getQuestionDetailsApi, getQuestionTestcaseApi, saveQuestionTagApi, updateQuestionApi } from '~/api/QuestionApi';
import { CloudFile, QuestionFile, Questions } from '~/interface/base';
import { CloseOutlined, EditOutlined, LikeOutlined, PlusOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { formatFileSize } from '~/utils';

const questionOptions = reactive<Questions>({
  title: '',
  questionCode: '',
  questionContent: '',
  diffLevel: '',
})
const modelOpen = ref(false)
const fileSelector = ref()
const route = useRoute()
/**
 * 初始化编辑器
 */
const globalStore = useGlobalStore()
const vditor = ref<Vditor>()
const vditorRef = ref()
const initEditor = () => {
  vditor.value = new Vditor(vditorRef.value, {
    minHeight: 250,
    height: 600,
    typewriterMode: true,
    cache: {
      enable: false
    },
    preview: {
      hljs: {
        enable: true,
        lineNumber: true,
        style: 'xcode'
      },
      math: {
        inlineDigit: true,
        engine: 'KaTeX'
      },
    },
    placeholder: '输入内容',
    counter: {
      enable: true,
      max: 65536,
    },
    toolbarConfig: {
      pin: true
    },
    upload: {
      url: '/api/tag/upload',
      max: 100 * 1024 * 1024,
      headers: {
        Authorization: globalStore.authorization || Cookies.get('authorization') as string,
      },
      fieldName: 'file',
      success(_editor: any, msg: any) {
        let res = JSON.parse(msg);
        let split = res.msg.split('.')
        let suffix: string = split[split.length - 1]
        if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp', 'tiff'].includes(suffix.toLocaleLowerCase())) {
          vditor.value?.insertValue(`![](${res.msg})`)
        }
        if (['mp4', 'mpeg', 'avi', 'mkv', 'mov'].includes(suffix.toLocaleLowerCase())) {
          vditor.value?.insertValue(`
          <video controls>
            <source src="${res.msg}"></source>
            </video>
          `)
        }
      },
    },
    after() {
      getQuestionDetails()
      getQuestionTestcase()
    },
  })
}

async function getQuestionDetails() {
  const res = await getQuestionDetailsApi(route.params.questionCode as string)
  if (res.data) {
    Object.assign(questionOptions, res.data)
    vditor.value?.setValue(res.data.questionContent)
  }
}

const testcaseList = ref<QuestionFile[]>([])
const tagState = reactive({
  tags: [] as TagInfo[],
  inputVisible: false,
  inputValue: '',
});

async function updateQuestion() {
  if (questionOptions.title === '') {
    message.error('请填写问题标题')
    return
  }
  questionOptions.questionContent = vditor.value?.getValue() as string
  const resArr = await Promise.all([updateQuestionApi(questionOptions), editQuestionTestcaseApi(testcaseList.value, route.params.questionCode as string),
  saveQuestionTagApi(questionOptions.id as number, tagState.tags)])
  if (resArr[0].data && resArr[1].data) {
    message.success('保存成功')
  } else {
    if (!resArr[0].data) {
      message.error('更新问题失败')
    } else if (!resArr[1].data) {
      message.error('更新测试用例失败')
    } else if (!resArr[2].data) {
      message.error('更新标签失败')
    }
  }
}

async function getQuestionTestcase() {
  const res = await getQuestionTestcaseApi(route.params.questionCode as string)
  if (res.data) {
    testcaseList.value = res.data
  }
}

const curEditFile = ref<CloudFile | undefined>({})
function chooseFile(file: CloudFile | undefined) {
  curEditFile.value = file
  modelOpen.value = true
}

function chooseFileChange(file: CloudFile) {
  // id 唯一
  for (let i = 0; i < testcaseList.value.length; i++) {
    if (testcaseList.value[i].inFile?.id === curEditFile.value?.id) {
      testcaseList.value[i].inFile = file
      testcaseList.value[i].stdinFileId = file.id
      break
    }
    if (testcaseList.value[i].outFile?.id === curEditFile.value?.id) {
      testcaseList.value[i].outFile = file
      testcaseList.value[i].stdoutFileId = file.id
      break
    }
  }
  modelOpen.value = false
}

function addTestcase() {
  testcaseList.value.push({
    questionCode: route.params.questionCode as string,
  })
}

// 标签相关
const inputRef = ref()
const handleTagClose = (tagName: string) => {
  tagState.tags = tagState.tags.filter(tag => tag.tagName !== tagName)
};
const handleTagInputConfirm = () => {
  const inputValue = tagState.inputValue;
  let tags = tagState.tags;
  if (inputValue && tags.find(v => v.tagName === inputValue) !== null) {
    tags = [...tags, {
      tagName: inputValue
    }];
  }
  Object.assign(tagState, {
    tags,
    inputVisible: false,
    inputValue: '',
  });
};
const showInput = () => {
  tagState.inputVisible = true;
  nextTick(() => {
    inputRef.value.focus();
  });
};

onMounted(() => {
  initEditor()
})

</script>

<template>
  <div class="my-20 w-[1200px] mx-auto max-sm:w-full">
    <div class="w-[1000px] mx-auto">
      <div class="mb-4">
        <input class="title_input" @input="(e: any) => questionOptions.title = e.target?.value" placeholder="请输入题目标题"
          style="height: 70px;" :value="questionOptions.title" />
        <a-input class="px-[20px]" disabled v-model:value="questionOptions.questionCode" :bordered="false"
          placeholder="请输入题目编号" />
        <div class="mt-2 px-[20px]">
          <template v-for="(tag, index) in tagState.tags" :key="tag.id">
            <a-tooltip :title="tag.tagName">
              <a-tag :closable="index !== 0" @close="handleTagClose(tag.tagName as string)">
                {{ tag.tagName }}
              </a-tag>
            </a-tooltip>
          </template>
          <a-input v-if="tagState.inputVisible" ref="inputRef" v-model:value="tagState.inputValue" type="text"
            size="small" :style="{ width: '78px' }" @blur="handleTagInputConfirm"
            @keyup.enter="handleTagInputConfirm" />
          <a-tag v-else style="background: #fff; border-style: dashed" @click="showInput">
            <div class="flex gap-1 items-center">
              <PlusOutlined />
              <span>新标签</span>
            </div>
          </a-tag>
        </div>
        <div class="px-[20px] mt-4">
          <a-tag class="text-sm cursor-pointer" color="green" @click="questionOptions.diffLevel = '0'">
            <div class="flex items-center gap-1">
              <span>简单</span>
              <LikeOutlined v-show="questionOptions.diffLevel === '0'" />
            </div>
          </a-tag>
          <a-tag class="text-sm cursor-pointer" color="yellow" @click="questionOptions.diffLevel = '1'">
            <div class="flex items-center gap-1">
              <span>普通</span>
              <LikeOutlined v-show="questionOptions.diffLevel === '1'" />
            </div>
          </a-tag>
          <a-tag class="text-sm cursor-pointer" color="red" @click="questionOptions.diffLevel = '2'">
            <div class="flex items-center gap-1">
              <span>困难</span>
              <LikeOutlined v-show="questionOptions.diffLevel === '2'" />
            </div>
          </a-tag>
        </div>
      </div>
      <div ref="vditorRef"></div>
      <div class="px-[20px] mt-4">
        <h1 class="text-base">测试样例</h1>
        <p class="text-xs text-gray-600 mb-2">共{{ testcaseList.length }}个</p>
        <div class="flex flex-col gap-2">
          <div v-for="(testcase, index) in testcaseList"
            class="bg-gray-100 p-2 border border-gray-400 rounded-sm relative group">
            <div>
              <a-space>
                <p>
                  <span>输入：</span>
                  <a v-show="testcase.inFile" class="hover:underline" :href="testcase.inFile?.fileUrl">{{
                    testcase.inFile?.fileName }}</a>
                  <span class="text-red-500 font-semibold" v-show="!testcase.inFile">请选择</span>
                </p>
                <p>{{ formatFileSize(testcase.inFile?.fileSize) }}</p>
                <p class="translate-y-[-3px] cursor-pointer hover:text-primary-color" title="编辑"
                  @click="chooseFile(testcase.inFile)">
                  <EditOutlined />
                </p>
              </a-space>
            </div>
            <div>
              <a-space>
                <p>
                  <span>输出：</span>
                  <a v-show="testcase.outFile" class="hover:underline" :href="testcase.outFile?.fileUrl">{{
                    testcase.outFile?.fileName }}</a>
                  <span class="text-red-500 font-semibold" v-show="!testcase.outFile">请选择</span>
                </p>
                <p>{{ formatFileSize(testcase.outFile?.fileSize) }}</p>
                <p class="translate-y-[-3px] cursor-pointer hover:text-primary-color" title="编辑"
                  @click="chooseFile(testcase.outFile)">
                  <EditOutlined />
                </p>
              </a-space>
            </div>
            <div class="absolute right-2 top-0 group-hover:block hidden cursor-pointer hover:text-red-500"
              @click="testcaseList.splice(index, 1)">
              <CloseOutlined class="text-xs" />
            </div>
          </div>
          <a-button class="w-24" @click="addTestcase">添加</a-button>
        </div>
      </div>
    </div>

    <a-modal v-model:open="modelOpen" :title="curEditFile?.fileName || '选择文件'" width="850px">
      <My_file_selector ref="fileSelector" @chooseFileChange="chooseFileChange" />
      <template #footer>

      </template>
    </a-modal>
  </div>
  <div class="h-[50px] fixed bottom-0 left-0 right-0 w-full border-t border-gray-300 bg-white">
    <div class="w-[1000px] mx-auto flex items-center h-full justify-end">
      <a-button class="w-24" type="primary" @click="updateQuestion">保存</a-button>
    </div>
  </div>
</template>

<style scoped>
.title_input {
  padding: 0 20px;
  width: 100%;
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
  font-size: 24px;
  outline: none;
  background: transparent;

  &::placeholder {
    font-size: 24px;
  }

  &:focus-within {
    border: none;
  }
}

.opear_box {
  background: #fff;
  border: #DADDE6 solid 1px;
  margin-top: 20px;
  padding: 20px;
  border-radius: 5px;
}
</style>