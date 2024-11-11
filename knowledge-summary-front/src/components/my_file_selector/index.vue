<script setup lang="ts">
import { Key } from 'ant-design-vue/es/_util/type';
import { CloudFile } from '~/interface/base';
import FolderImage from '../../assets/folder.png'
import { getCloudFileListApi } from '~/api/CloudFileApi';
import { formatFileSize, getFileSuffix } from '~/utils';
import { FolderOutlined, RightOutlined } from '@ant-design/icons-vue';


const columns = [
  {
    title: '文件浏览',
    dataIndex: 'fileUrl'
  },
  {
    title: '文件名',
    dataIndex: 'fileName'
  },
  {
    title: '文件大小',
    dataIndex: 'fileSize'
  },
  {
    title: '上传时间',
    dataIndex: 'createTime'
  },
  {
    title: '操作',
    dataIndex: 'operation',
  }
]
const filesList = ref<CloudFile[]>([])
const filePath = ref<string[]>(['/'])

const filePathComputed = computed(() => filePath.value.join('/').replace(/\/+/g, '/'))

function enterDir(dir: string) {
  filePath.value.push(dir)
  getCloudFileList()
}

let outCt: AbortController
async function getCloudFileList(fileName?: string, init?: boolean) {
  if (init) {
    if (localStorage.getItem('myFilePath')) {
      filePath.value = JSON.parse((localStorage.getItem('myFilePath') as string))
    }
  }
  if (outCt) {
    outCt.abort()
  }
  outCt = new AbortController()
  const res = await getCloudFileListApi(filePathComputed.value, outCt, fileName)
  if (res.data) {
    filesList.value = res.data.map(v => ({
      key: v.id,
      ...v
    }))
  }
}

function toggleDir(idx: number) {
  filePath.value = filePath.value.slice(0, idx + 1)
  getCloudFileList()
}

const emit = defineEmits(['chooseFileChange'])
function chooseFile(record: CloudFile) {
  emit('chooseFileChange', record)
}

onMounted(() => {
  getCloudFileList('', true)
})

</script>

<template>
  <div class="w-[800px]">
    <div class="text-gray-600">
      <div class="flex flex-wrap gap-2 items-center">
        <div class="flex items-center mr-2">
          <FolderOutlined class="text-base" />
        </div>
        <!-- <p v-if="filePath.length === 1"
            class="bg-gray-100 text-gray-500 px-2 py-1 rounded-md cursor-pointer hover:bg-gray-200 hover:text-gray-600">
            /</p> -->
        <div v-for="(path, idx) in filePath" :key="idx" class="flex gap-2 items-center">
          <div
            class="w-fit bg-gray-100 text-gray-500 px-2 py-1 rounded-md cursor-pointer hover:bg-gray-200 hover:text-gray-600"
            @click="toggleDir(idx)">
            {{ path }}
          </div>
          <div class="w-fit text-gray-500 px-2 py-1 rounded-md hover:text-gray-600 flex items-center">
            <RightOutlined class="text-xs" />
          </div>
        </div>
      </div>
    </div>
    <a-table class="mt-2 overflow-x-auto" :columns="columns" :data-source="filesList" :pagination="false">
      <template #emptyText>
        <a-empty :image="FolderImage" :imageStyle="{ display: 'flex', justifyContent: 'center' }"
          description="文件夹是空的" />
      </template>
      <template #bodyCell="{ column, text, record }: { column: any, text: any, record: CloudFile }">
        <template v-if="column.dataIndex === 'operation'">
          <a-button size="small" type="primary" v-if="record.dirFlag === '0'" @click="chooseFile(record)">
            <p class="text-xs">选择</p>
          </a-button>
        </template>
        <template v-if="column.dataIndex === 'fileName'">
          <p v-if="record.dirFlag === '0'" class="text-black">{{ text }}</p>
          <div v-else class="flex items-center gap-1 cursor-pointer hover:text-primary-color w-fit"
            @click="enterDir(text)">
            <FolderOutlined class="text-base" />
            <p>{{ text }}</p>
          </div>
        </template>
        <template v-if="column.dataIndex === 'fileUrl'">
          <div class="w-16 h-16 flex items-center justify-center" v-show="record.dirFlag === '0'">
            <a :href="text" target="_blank"
              v-if="['webp', 'jpg', 'jpeg', 'png', 'svg'].includes(getFileSuffix(record.fileName as string))">
              <img class="cursor-pointer w-16 h-16 object-cover" :src="text" alt="">
            </a>
            <a v-else :href="text" target="_blank">在线浏览</a>
          </div>
        </template>
        <template v-if="column.dataIndex === 'fileSize'">
          <p v-if="record.dirFlag === '0'">{{ formatFileSize(text * 1) }}</p>
        </template>
      </template>
    </a-table>
  </div>
</template>