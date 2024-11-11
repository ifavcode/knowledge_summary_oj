<script setup lang='ts'>
import { FormItemRule, FormRules, UploadFileInfo, useMessage } from 'naive-ui';
import { request } from '~/utils/request';
import { useGlobalStore } from '~/store/globalStore';
import 'vditor/dist/index.css'
import Vditor from 'vditor'
import Cookies from 'js-cookie';

const modelRef = ref({
  tags: [] as Array<number>,
  image: '',
  title: '',
  iframeAddr: '',
  rawText: ''
})
const vditorRef = ref()

const globalStore = useGlobalStore()
const loading = ref(false)
const formRef = ref()
const message = useMessage()
const router = useRouter()
const route = useRoute()
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

function handlePubArticleClick(e: Event) {
  e.preventDefault()
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      //标签没有，先创建出来
      let notCreateTag: Array<any> = []
      //收集所有标签id
      let tagIds: Array<number> = []
      modelRef.value.tags.forEach((v, _i) => {
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
          articleContent: vditor.value?.getValue(),
          articleTitle: modelRef.value.title,
          articleImage: modelRef.value.image,
          iframeAddr: modelRef.value.iframeAddr,
          //总的标签id
          tags: tagIds,
          rawText: modelRef.value.rawText,
          articleType: 'markdown',
          articleId: route.params.articleId,
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

const options = ref<Array<{
  label: string,
  value: number
}>>([])

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

/**
 * 初始化编辑器
 */
const vditor = ref<Vditor>()
const initEditor = () => {
  vditor.value = new Vditor(vditorRef.value, {
    height: 'auto',
    minHeight: 250,
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
      after(length) {
        editorTextNum.value = length
      },
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
      success(_editor, msg) {
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
      getArticleContent()
    },
  })
}

/**
 * 提取文章关键字
 */
const getArticlesRawText = () => {
  modelRef.value.rawText = vditor.value?.getValue().substring(0, 256).replace(/\n|\r/mg, '') as string
}

const opearBox = ref<HTMLElement>()
const editorTextNum = ref(0)

const scrollToEditorArea = () => {
  window.scrollTo({
    top: opearBox.value?.offsetTop,
    behavior: 'smooth'
  })
}

/**
 * 和发布文章不同的地方
 */

const curPublishUser = ref<User>()

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
  curPublishUser.value = res.data?.publishUser
  vditor.value?.setValue(res.data?.articleContent as string)
  modelRef.value.title = res.data?.articleTitle as string
  res.data?.tagInfoList?.forEach(tag => {
    modelRef.value.tags.push(tag.tagId as number)
  })
  modelRef.value.iframeAddr = res.data?.iframeAddr as string
  modelRef.value.rawText = res.data?.rawText as string
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

onMounted(() => {
  getAllTag()
  nextTick(() => {
    initEditor()
  })
})

</script>
 
<template>
  <div class="total">
    <div class="publish_article_wrap">
      <div class="content_box">
        <input class="title_input" @input="handleTitleChange" placeholder="请输入标题..." style="height: 70px;"
          :value="modelRef.title" />
        <div id="vditor" ref="vditorRef"> </div>
        <div class="opear_box" ref="opearBox">
          <n-form class="login_form" ref="formRef" :model="modelRef" :rules="rules" label-placement="left">
            <n-form-item path="tags" label="标签">
              <n-select v-model:value="modelRef.tags" filterable multiple tag :options="options"
                placeholder="选择或输入自定义标签..." />
            </n-form-item>
            <n-form-item path="rawText" label="文章关键字">
              <div class="w-full">
                <n-input placeholder="文章关键字越详细，越容易被搜索到" type="textarea" maxlength="256" show-count clearable
                  v-model:value="modelRef.rawText" />
                <n-button class="mt-3" @click="getArticlesRawText">提取关键字</n-button>
              </div>
            </n-form-item>
            <n-form-item path="iframeAddr" label="功能演示地址">
              <n-input placeholder="页面url..." />
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
        <div class="flex items-center flex-1 overflow-x-auto text-red-500 whitespace-nowrap max-sm:pr-3">
          <span class="font-thin" v-if="globalStore.userInfo?.userId != curPublishUser?.userId">注意：你正在修改其他用户的文章——{{ curPublishUser?.nickName
          }}</span>
        </div>
        <div class="flex items-center justify-end">
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
#vditor {}

.total {
  width: 100%;
  max-width: 800px;
  margin: 0px auto;
  padding-bottom: 30px;

  .publish_article_wrap {
    margin: 0px auto;
    max-width: 1200px;
    display: flex;
    min-width: 300px;

    @media screen and (min-width: 700px) and (max-width: 1200px) {
      padding: 0 60px;
    }

    @media screen and (max-width: 700px) {
      padding: 0 0px;
    }

    .content_box {
      width: 100%;

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