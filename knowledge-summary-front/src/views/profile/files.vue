<script setup lang="ts">
import { CloudDownloadOutlined, DeleteOutlined, EditOutlined, FolderAddOutlined, FolderOutlined, InboxOutlined, RightOutlined } from '@ant-design/icons-vue';
import { UploadChangeParam } from 'ant-design-vue';
import { Key } from 'ant-design-vue/es/_util/type';
import { CloudFile } from '~/interface/base';
import { message } from 'ant-design-vue';
import Cookies from 'js-cookie';
import { useGlobalStore } from '~/store/globalStore';
import { deleteFilePathApi, downloadFileApi, getCloudFileListApi, mkDirApi } from '~/api/CloudFileApi';
import { formatFileSize, getFileSuffix } from '~/utils';
import FolderImage from '../../assets/folder.png'

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

let everyFileDownload = reactive<Record<number, any>>({})
function downloadFile(record: CloudFile) {
  everyFileDownload[record.id as number] = downloadFileApi(record)
}

async function deleteFile(row: any) {
  const res = await deleteFilePathApi(row.id.toString())
  if (res.code === 200) {
    message.success('删除成功')
    getCloudFileList()
    confirmDeleteShow.value = false
  }
}

async function deleteFileMulti() {
  const res = await deleteFilePathApi(selectedRowKeys.value.join(','))
  if (res.code === 200) {
    message.success('删除成功')
    getCloudFileList()
    confirmDeleteShow.value = false
  }
}

function editFile() {

}

const filesList = ref<CloudFile[]>([])
const confirmDeleteShow = ref(false)

const selectedRowKeys = ref<Key[]>([]);
const onSelectChange = (params: Key[]) => {
  selectedRowKeys.value = params;
};

const searchValue = ref('')
const uploadModelOpen = ref(false)
const mkDirModelOpen = ref(false)
const fileList = ref([]);
const filePath = ref<string[]>(['/'])
const mkDirName = ref('')
function onSearch() {
  getCloudFileList(searchValue.value)
}
const handleChange = (info: UploadChangeParam) => {
  const status = info.file.status;
  if (status !== 'uploading') {
    console.log(info.file, info.fileList);
  }
  if (status === 'done') {
    message.success(`${info.file.name} 上传成功.`);
    getCloudFileList()
  } else if (status === 'error') {
    message.error(`${info.file.name} 上传失败.`);
  }
};

const filePathComputed = computed(() => filePath.value.join('/').replace(/\/+/g, '/'))

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

async function mkDir() {
  if (mkDirName.value === '') {
    message.error('文件夹名称不能为空')
    return
  }
  const res = await mkDirApi(filePathComputed.value, mkDirName.value)
  if (res.code === 200) {
    message.success('创建成功')
    getCloudFileList()
    mkDirModelOpen.value = false
  }
}

function enterDir(dir: string) {
  filePath.value.push(dir)
  getCloudFileList()
}

function toggleDir(idx: number) {
  filePath.value = filePath.value.slice(0, idx + 1)
  getCloudFileList()
}

function isDownloading(id: number) {
  return everyFileDownload[id] && everyFileDownload[id].isDownloading
}

let autoSaveTimer: NodeJS.Timer
function autoSaveCurPath() {
  autoSaveTimer = setInterval(() => {
    localStorage.setItem('myFilePath', JSON.stringify(filePath.value))
  }, 5000)
}

onMounted(() => {
  getCloudFileList('', true)
  autoSaveCurPath()
})

onBeforeUnmount(() => {
  clearInterval(autoSaveTimer)
})
</script>

<template>
  <div class="mt-20 w-[1200px] mx-auto max-sm:w-full">
    <div>
      <n-space>
        <a-input-search v-model:value="searchValue" placeholder="文件名" style="width: 200px" @search="onSearch" />
        <a-button @click="uploadModelOpen = true">上传</a-button>
        <a-button @click="mkDirModelOpen = true">
          <FolderAddOutlined />
          <span>新建文件夹</span>
        </a-button>
        <a-button danger type="primary" @click="deleteFileMulti">批量删除</a-button>
      </n-space>
    </div>
    <div class="mt-4">
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
      <a-table class="mt-2 overflow-x-auto" :columns="columns" :data-source="filesList"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }" :pagination="false">
        <template #emptyText>
          <a-empty :image="FolderImage" :imageStyle="{ display: 'flex', justifyContent: 'center' }"
            description="文件夹是空的" />
        </template>
        <template #bodyCell="{ column, text, record }: { column: any, text: any, record: CloudFile }">
          <template v-if="column.dataIndex === 'operation'">
            <div class="editable-row-operations">
              <a-space>
                <a-button class="relative" @click="downloadFile(record)" size="small"
                  :disabled="isDownloading(record.id as number)">
                  <div class="absolute h-full left-0 top-0 bg-green-500 z-0"
                    :style="{ width: isDownloading(record.id as number) ? everyFileDownload[record.id as number].process + '%' : '0%' }">
                  </div>
                  <div class="relative z-10 text-black">
                    <p v-if="isDownloading(record.id as number)">
                      {{ everyFileDownload[record.id as number].process }}%</p>
                    <CloudDownloadOutlined class="text-base" v-else />
                  </div>
                </a-button>
                <!-- <a-button @click="editFile">
                  <EditOutlined class="text-base" />
                </a-button> -->
                <a-popconfirm :title="`确定删除这个${record.dirFlag === '0' ? '文件' : '文件夹'}吗?`" ok-text="确认"
                  :showCancel="false" @confirm="deleteFile(record)">
                  <a-button danger type="primary" size="small">
                    <DeleteOutlined class="text-base" />
                  </a-button>
                </a-popconfirm>
              </a-space>
            </div>
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

    <a-modal v-model:open="uploadModelOpen" :title="'当前路径：' + filePathComputed" @onCancel="() => console.log('1')">
      <div class="mt-4">
        <a-upload-dragger v-model:fileList="fileList" name="file" :multiple="true" action="/api/cloud/file/upload"
          :headers="{
            Authorization: Cookies.get('authorization') || useGlobalStore().authorization
          }" :data="(_file: File) => ({ filePath: filePathComputed })" @change="handleChange">
          <p class="ant-upload-drag-icon">
            <InboxOutlined></InboxOutlined>
          </p>
          <p class="ant-upload-text">单击或拖动文件到此区域进行上传</p>
          <p class="ant-upload-hint">
            支持单个或批量上传。严禁上传敏感文件
          </p>
        </a-upload-dragger>
      </div>
      <template #footer></template>
    </a-modal>
    <a-modal v-model:open="mkDirModelOpen" :closable="false" title="">
      <a-form-item label="文件夹名称：" class="translate-y-2">
        <a-input v-model:value="mkDirName" placeholder="输入文件夹名称" @keydown.enter="mkDir"></a-input>
      </a-form-item>
      <template #footer>
        <div class="w-full flex items-end justify-end">
          <a-button @click="mkDirModelOpen = false">取消</a-button>
          <a-button @click="mkDir" type="primary">确认</a-button>
        </div>
      </template>
    </a-modal>
  </div>
</template>