<script setup lang='ts'>
//@ts-ignore
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { FormItemRule, UploadFileInfo, useMessage } from 'naive-ui';
import { FormRules } from 'naive-ui/lib';
import { useGlobalStore } from '~/store/globalStore';
import { request } from '~/utils/request';
import Cookies from 'js-cookie';

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()
const route = useRoute()

const modelRef = ref<any>({
  tags: [],
  image: '',
  title: '',
  iframeAddr: '',
  rawText: ''
})
const rules: FormRules = {
  tags: [
    {
      required: true,
      validator(_rule: FormItemRule, value: string) {
        return value.length > 0
      },
      message: '至少选择一个标签'
    }
  ],
  rawText: [
    {
      required: true,
      validator(_rule: FormItemRule, value: string) {
        return value.length > 0
      },
      message: '文章关键字不能为空'
    }
  ],
}

//文章内容 HTML
const articleContent = ref('')
const globalStore = useGlobalStore()
const toolbarConfig: Partial<IToolbarConfig> = {
  insertKeys: {
    index: 0,
    keys: [
      'insertFormula', // “插入公式”菜单
      'editFormula' // “编辑公式”菜单
    ],
  },
}
type InsertFnType = (url: string, alt: string, href: string) => void
type InsertVideoFnType = (url: string, poster: string) => void
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入内容...',
  // 选中公式时的悬浮菜单
  hoverbarKeys: {
    formula: {
      menuKeys: ['editFormula'], // “编辑公式”菜单
    },
  },
  scroll: false,
  maxLength: 65536,
  MENU_CONF: {
    uploadImage: {
      server: '/api/tag/upload',
      customInsert(res: any, insertFn: InsertFnType) {
        insertFn(res.msg, "", "")
      },
      headers: {
        Authorization: globalStore.authorization || Cookies.get('authorization'),
      },
      maxFileSize: 8 * 1024 * 1024, //8M
      fieldName: 'file',
    },
    uploadVideo: {
      server: '/api/tag/upload',
      customInsert(res: any, insertFn: InsertVideoFnType) {
        insertFn(res.msg, "")
      },
      headers: {
        Authorization: globalStore.authorization || Cookies.get('authorization'),
      },
      maxFileSize: 100 * 1024 * 1024, //100M
      fieldName: 'file',
    }
  }
}

/**
 * 上传图片的配置
 * 
 */

const loading = ref(false)
const options = ref<Array<{
  label: string,
  value: number
}>>([])

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const formRef = ref()
const message = useMessage()
const router = useRouter()
const mode = ref('default')

const handleCreated = (editor: any) => {
  //获取文章内容
  getArticleContent()
  editorRef.value = editor
}

const handleTitleChange = (e: any) => {
  modelRef.value.title = e.target.value
}

const handleFinish = ({ file, event }: { file: UploadFileInfo, event?: any }) => {
  const res = JSON.parse(event.target.response)
  file.url = res.msg
  modelRef.value.image = res.msg
  return file
}

const getAllTag = async () => {
  const res: AjaxResult<TagInfo[]> = await request('/api/tag/all')
  res.data?.forEach((v: TagInfo, _i: number) => {
    options.value.push({
      label: v.tagName as string,
      value: v.tagId as number
    })
  })
}

function handlePubArticleClick(e: Event) {
  e.preventDefault()
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      //标签没有，先创建出来
      let notCreateTag: Array<any> = []
      //收集所有标签id
      let tagIds: Array<number> = []
      modelRef.value.tags.forEach((v: TagInfo, _i: number) => {
        if (typeof v == 'string') {
          notCreateTag.push({
            tagName: v
          })
        } else if (typeof v == 'number') {
          //已经创建的标签 收集id
          tagIds.push(v)
        }
      })
      const res2: AjaxResult<TagInfo[]> = await request('/api/tag/add', {
        method: 'post',
        data: notCreateTag
      })
      res2.data?.forEach(v => {
        //新加的标签收集起来
        if (v.tagId != null) {
          tagIds.push(v.tagId as number)
        }
      })
      const res: boolean = await request('/api/articles/update', {
        method: 'post',
        data: {
          articleContent: articleContent.value,
          articleTitle: modelRef.value.title,
          articleImage: modelRef.value.image,
          iframeAddr: modelRef.value.iframeAddr,
          //总的标签id
          tags: tagIds,
          rawText: modelRef.value.rawText,
          articleId: route.params.articleId
        }
      })
      if (res) {
        message.success('修改成功啦')
        router.push('/profile/' + globalStore.userInfo?.userId + '?is_article=1')
      } else {
        message.error('糟糕，发生了一点错误！请稍后再试')
        // }
      }
    }
  })
}

const opearBox = ref<HTMLElement>()
const editorTextNum = ref(0)
const editorTextChange = async () => {
  editorTextNum.value = editorRef.value.getText().replace(/\n|\r/mg, '').length
}

const scrollToEditorArea = () => {
  window.scrollTo({
    top: opearBox.value?.offsetTop,
    behavior: 'smooth'
  })
}

/**
 * 提取文章关键字
 */
const getArticlesRawText = () => {
  modelRef.value.rawText = editorRef.value.getText().substr(0, 256).replace(/\n|\r/mg, '')
}

/**
 * 和发布文章不同的地方
 */

const getArticleContent = async () => {
  const res: AjaxResult<Articles> = await request('/api/articles/id', {
    params: {
      articleId: route.params.articleId
    }
  })
  //不是当前用户发布的文章
  if (globalStore.userInfo?.userId !== res.data?.publishUser?.userId) {
    if (!globalStore.isAdmin && !globalStore.userAuth.includes('edit:user:article')) {
      router.replace('/not_found/not_auth')
      return;
    }
  }
  articleContent.value = res.data?.articleContent as string
  modelRef.value.title = res.data?.articleTitle as string
  res.data?.tagInfoList?.forEach(tag => {
    modelRef.value.tags.push(tag.tagId)
  })
  modelRef.value.iframeAddr = res.data?.iframeAddr
  modelRef.value.rawText = res.data?.rawText
  if (res.data?.articleImage != '' && res.data?.articleImage != null) {
    defaultFileList.value.push({
      id: '文章图片',
      name: '文章图片',
      status: 'finished',
      url: res.data?.articleImage
    })
  }
}

const defaultFileList = ref<UploadFileInfo[]>([])

onBeforeMount(() => {
  getAllTag()
})
</script>
 
<template>
  <!-- <div class="markdown_btn" @click="router.push('/publish_article')">
    <span>md</span>
    <span>编辑器</span>
  </div> -->
  <div class="top_toolbar">
    <Toolbar style="border-bottom: 1px solid #ccc;border-top: 1px solid #ccc;justify-content: center;display: flex;"
      :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
  </div>
  <div id="wang_editor_wrap">
    <div class="bottom_editor">
      <input class="title_input" @input="handleTitleChange" placeholder="请输入标题" style="height: 70px;"
        :value="modelRef.title" />
      <Editor style="min-height: 300px; overflow-y: hidden;" v-model="articleContent" :defaultConfig="editorConfig"
        :mode="mode" @onCreated="handleCreated" @on-change="editorTextChange"
        @on-max-length="message.error('字数不能超过最大长度10,000')" />
      <div class="opear_box" ref="opearBox">
        <n-form class="login_form" ref="formRef" :model="modelRef" :rules="rules" label-placement="left">
          <n-form-item path="tags" label="标签">
            <n-select v-model:value="modelRef.tags" filterable multiple tag :options="options" placeholder="选择或输入自定义标签" />
          </n-form-item>
          <n-form-item path="rawText" label="文章关键字">
            <div class="w-full">
              <n-input placeholder="文章关键字越详细，越容易被搜索到" type="textarea" maxlength="256" show-count clearable
                v-model:value="modelRef.rawText" />
              <n-button class="mt-3" @click="getArticlesRawText">提取关键字</n-button>
            </div>
          </n-form-item>
          <n-form-item path="iframeAddr" label="功能演示地址">
            <n-input placeholder="页面url" />
          </n-form-item>
          <n-form-item path="image" label="封面">
            <n-upload action="/api/tag/upload" list-type="image-card" :max="1" :headers="{
              'Authorization': globalStore.authorization
            }" @finish="handleFinish" :default-file-list="defaultFileList">
              点击上传
            </n-upload>
          </n-form-item>
        </n-form>
      </div>
    </div>
  </div>

  <div class="test">
    <div class="bottom_wrap">
      <div class="center_content">
        <div class="icon_item">
          <span>共 {{ editorTextNum }} 字</span>
        </div>
        <div class="icon_item cursor-pointer" @click="scrollToEditorArea">
          前往编辑区
        </div>
        <div class="flex items-center flex-1 justify-end">
          <div class="w-fit">
            <n-button class="btn" :loading="loading" type="primary" @click="handlePubArticleClick">
              修改
            </n-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.markdown_btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  box-shadow: 1px 1px 10px #3c3c3c55;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #a1a1a1;
  flex-direction: column;
  font-size: 12px;
  cursor: pointer;

  @media screen and (max-width:480px) {
    display: none;
  }
}

.top_toolbar {
  width: 100%;
  background: #FFFFFF;
  position: sticky;
  top: -1px;
  z-index: 100;
}

#wang_editor_wrap {
  width: 100%;
  max-width: 800px;
  margin: 0px auto;
  padding-bottom: 30px;

  .bottom_editor {
    margin-top: 10px;

    .title_input {
      border: none;
      padding: 0 10px;
      width: 100%;
      border-top-left-radius: 5px;
      border-top-right-radius: 5px;
      font-size: 24px;
      outline: none;

      &:focus-within {
        border: none;
      }

      &::placeholder {
        font-size: 24px;
      }
    }

    .opear_box {
      background: #fff;
      border: #DADDE6 solid 1px;
      margin-top: 20px;
      padding: 20px;
      border-radius: 5px;
    }
  }
}

.test {
  position: fixed;
  bottom: 0;
  height: 50px;
  z-index: 100;

  .bottom_wrap {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 50px;
    border-top: solid 1px #dfdfdf;
    background: #fff;
    transition: all .4s;
    -webkit-overflow-scrolling: touch;

    @media screen and (max-width: 1200px) and (min-width: 480px) {
      padding: 0 60px;
    }

    @media screen and (max-width:480px) {
      padding: 0 10px;
    }

    .center_content {
      width: 100%;
      max-width: 850px;
      margin: 0 auto;
      display: flex;
      height: 50px;

      .send_input {
        flex: 1;
        display: flex;
        height: 100%;
        align-items: center;
      }

      .icon_item {
        display: flex;
        align-items: center;
        height: 100%;
        margin-right: 20px;

        .active_icon {
          color: #F75F5E;
        }

        span {
          margin-left: 5px;
        }

        i {
          font-size: 20px;
        }
      }
    }
  }
}
</style>