<script setup lang='ts'>
import { useMessage } from 'naive-ui';
import { GenerateImage } from '~/interface/base';
import { request } from '~/utils/request';
import { base64ToBlob } from '~/utils/index';
import FileSaver from 'file-saver';
import { DownloadOutline } from '@vicons/ionicons5'


const sendObj = reactive<GenerateImage>({
  prompt: '',
  size: '1024x1024',
  negativePrompt: '',
  n: 1,
  steps: 20,
  samplerIndex: 'Euler a',
})

const loading = ref(false)
const message = useMessage()
const curBase64ImgArr = ref('')

const fillValueObj: Record<string, string> = {
  '人物壁纸': 'Stunning wallpaper featuring a costume-clad girl, moonlit sky, auspicious clouds, classical patterns, soft moonlight, petals drifting down, colorful sparkles, laser lights, romantic hues, light pink, geometric composition, intricate details',
  '炫酷机甲': 'A cool mecha rabbit wearing a space helmet stands on the moon with debris from spacecrafts around it. The high-definition picture features rich details and laser lights',
  '唯美植物': 'A close-up shot of rain-soaked roses, revealing their delicate details, crystal-clear transparency, and a tempting bloom in the morning light. High definition',
  '梦幻城堡': 'A building, miniaturized and shown inside a crystal ball, surrounded by snow-capped mountains with snowflakes swirling around. A fairytale castle straight out of a movie, with high levels of detail and superior quality. Hyperrealism and surrealism are blended with a touch of fantasy and a pastel color scheme',
  '游戏场景': 'A pirate ship under attack by monsters during a raging storm at night. The picture captures the perfect battle without any imperfections, with an anime style reminiscent of Japanese visual kei. Double exposure and an ultra-wide angle create an unforgettable image',
  '家装设计': 'A creative living room with a noble blue color scheme and floral accents. The room is filled with natural light from the gold-colored rays, creating a surrealistic ambiance. The design is a prize-winning masterpiece with mind-blowing details'
}

const handleTagClick = (key: string) => {
  sendObj.prompt = fillValueObj[key]
}

const selectRange = [
  {
    label: '768x768',
    value: '768x768'
  },
  {
    label: '768x1024',
    value: '768x1024'
  },
  {
    label: '1024x768',
    value: '1024x768'
  },
  {
    label: '576x1024',
    value: '576x1024'
  },
  {
    label: '1024x1024',
    value: '1024x1024'
  },
  {
    label: '1024x576',
    value: '1024x576'
  },
]

const samplerIndexOption = [
  {
    label: 'Euler',
    value: 'Euler'
  },
  {
    label: 'Euler a',
    value: 'Euler a'
  },
  {
    label: 'DPM++ 2M',
    value: 'DPM++ 2M'
  },
  {
    label: 'DPM++ 2M Karras',
    value: 'DPM++ 2M Karras'
  },
  {
    label: 'LMS Karras',
    value: 'LMS Karras'
  },
  {
    label: 'DPM++ SDE',
    value: 'DPM++ SDE'
  },
  {
    label: 'DPM++ SDE Karras',
    value: 'DPM++ SDE Karras'
  },
  {
    label: 'DPM2 a Karras',
    value: 'DPM2 a Karras'
  },
  {
    label: 'Heun',
    value: 'Heun'
  },
  {
    label: 'DPM++ 2M SDE',
    value: 'DPM++ 2M SDE'
  },
  {
    label: 'DPM++ 2M SDE Karras',
    value: 'DPM++ 2M SDE Karras'
  },
  {
    label: 'DPM2',
    value: 'DPM2'
  },
  {
    label: 'DPM2 Karras',
    value: 'DPM2 Karras'
  },
  {
    label: 'DPM2 a',
    value: 'DPM2 a'
  },
  {
    label: 'LMS',
    value: 'LMS'
  },
]

const generateImageApi = async () => {
  if (sendObj.prompt === '' || sendObj.prompt === undefined) {
    message.warning('提示词不能为空')
    return
  }
  try {
    loading.value = true
    const res: Record<string, any> = await request('/flux/bigData/generateImage', {
      method: 'POST',
      data: sendObj
    })
    curBase64ImgArr.value = res.data.data.reduce((pre: string[], cur: any) => {
      pre.push('data:image/png;base64,' + cur.b64_image)
      return pre
    }, [])
    if (res.code != 200) {
      message.error(res.data.error_msg)
    }
  } catch (error) {
    message.error('出错了')
  } finally {
    loading.value = false
  }
}

const downloadImage = (b64: string) => {

  FileSaver.saveAs(base64ToBlob(b64.replace('data:image/png;base64,', '')), "download.png")
}

</script>

<template>
  <div class="max-w-[1200px] h-[100px] my-0 mx-auto pt-14 max-xl:px-5">
    <div class="my-3">
      <n-space>
        <n-tag class="cursor-pointer hover:bg-blue-400 hover:text-white hover:border-blue-400 transition-none"
          v-for="item in Object.keys(fillValueObj)" @click="handleTagClick(item)">{{ item }}</n-tag>
      </n-space>
      <div class="sm:flex w-full gap-4">
        <div class="sm:w-[50%] max-sm:w-full">
          <p class="mt-3 font-bold text-base">必填参数</p>
          <p>提示词</p>
          <n-input v-model:value="sendObj.prompt" type="textarea"
            placeholder="提示词，即用户希望图片包含的元素。长度限制为1024字符，建议使用英文单词，总数量不超过150个" show-count
            :autosize="{ maxRows: 10, minRows: 3 }" />
          <p class="mt-3">大小</p>
          <n-select class="w-32" v-model:value="sendObj.size" :options="selectRange" />
          <p class="mt-3 font-bold text-base">选填参数</p>
          <p>反向提示词</p>
          <n-input v-model:value="sendObj.negativePrompt" type="textarea"
            placeholder="反向提示词，即用户希望图片不包含的元素。长度限制为1024字符，建议中文或者英文单词总数量不超过150个" show-count
            :autosize="{ maxRows: 10, minRows: 3 }" />
          <p class="mt-3">数量</p>
          <n-input-number class="w-24" v-model:value="sendObj.n" clearable :min="1" :max="4" />
          <p class="mt-3">迭代轮次<span class="text-gray-500 text-xs">（默认20，10 ~ 50）</span></p>
          <n-input-number class="w-24" v-model:value="sendObj.steps" clearable :min="10" :max="50" />
          <p class="mt-3">采样方式</p>
          <n-select class="w-64" v-model:value="sendObj.samplerIndex" :options="samplerIndexOption" />
          <n-button class="mt-3 w-full" secondary type="success" @click="generateImageApi"
            :loading="loading">生成</n-button>
        </div>

        <div class="sm:w-[50%] max-sm:w-full">
          <div class="mt-3 w-full h-full ">
            <n-skeleton height="100%" width="100%" v-if="loading" />
            <div v-else v-for="b64 in curBase64ImgArr">
              <div class="relative">
                <img :src="b64" alt="">
                <p class="absolute bottom-2 left-0 right-3 text-right">
                  <n-icon size="24" color="#fff" @click="downloadImage(b64)" class=" cursor-pointer" title="保存图片到本地">
                    <DownloadOutline />
                  </n-icon>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang='scss' scoped></style>