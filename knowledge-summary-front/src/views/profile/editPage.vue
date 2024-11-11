<script setup lang='ts'>
import { FormInst, useMessage } from 'naive-ui';
import { FormItemRule, FormRules } from 'naive-ui/lib';
import { request } from '~/utils/request';

const props = defineProps<{
  userInfo: userInfo
}>()
const emit = defineEmits(['userInfoUpdate','exitEdit'])

const message = useMessage()

const modelRef = ref<userInfo>({
  nickName: '',
  userDesc: '',
  userAddr: '',
  userSex: '',
  userPhone: '',
  userSchool: '',
})
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const rules: FormRules = {
  nickName: [
    {
      required: true,
      validator(_rule: FormItemRule, value: string) {
        return value.length >= 1 && value.length <= 20
      },
      message: '昵称长度为1-20位'
    }
  ],
}

function handleValidateButtonClick(e: Event) {
  e.preventDefault()
  formRef.value?.validate((errors) => {
    if (!errors) {
      editUserInfo()
    } else {
    }
  })
}

const editUserInfo = async () => {
  loading.value = true
  const res: boolean = await request('/api/user/edit', {
    method: 'post',
    data: {
      ...modelRef.value,
      userId: props.userInfo.userId
    }
  })
  if (res) {
    message.success('修改成果')
    emit('userInfoUpdate')
  } else {
    message.warning('异常修改！')
  }
  loading.value = false
}

onMounted(() => {
  modelRef.value.nickName = props.userInfo.nickName
  modelRef.value.userDesc = props.userInfo.userDesc
  modelRef.value.userAddr = props.userInfo.userAddr
  modelRef.value.userSex = props.userInfo.userSex
  modelRef.value.userPhone = props.userInfo.userPhone
  modelRef.value.userSchool = props.userInfo.userSchool
})

</script>
 
<template>
  <div class="edit_wrap">
    <div class="back">
      <p @click="emit('exitEdit')">
        <i class="iconfont">&#xe601;</i>
        <span>返回</span>
      </p>
    </div>
    <n-scrollbar style="max-height: 544px">
      <n-form class="edit_form" ref="formRef" :model="modelRef" :rules="rules">
        <n-form-item path="nickName" label="用户名称">
          <n-input v-model:value="modelRef.nickName" placeholder="用户名称" />
        </n-form-item>
        <n-form-item path="userSex" label="性别">
          <n-select v-model:value="modelRef.userSex" placeholder="请选择" :options="[
            { label: '男', value: '1' }, { label: '女', value: '0' }, { label: '保密', value: '2' }
          ]" />
        </n-form-item>
        <n-form-item path="userDesc" label="用户简介">
          <n-input v-model:value="modelRef.userDesc" placeholder="用户简介" type="textarea" show-count :maxlength="255"
            clearable />
        </n-form-item>
        <n-form-item path="userAddr" label="地址">
          <n-input v-model:value="modelRef.userAddr" placeholder="地址" />
        </n-form-item>
        <n-form-item path="userPhone" label="手机号码">
          <n-input v-model:value="modelRef.userPhone" placeholder="手机号码" />
        </n-form-item>
        <n-form-item path="userSchool" label="学校">
          <n-input v-model:value="modelRef.userSchool" placeholder="学校" />
        </n-form-item>
        <n-button class="btn" color="#242528" :loading="loading" type="primary" @click="handleValidateButtonClick">
          确认
        </n-button>
      </n-form>
    </n-scrollbar>
  </div>
</template>
 
<style lang='scss' scoped>
.edit_wrap {
  box-shadow: 1px 1px 6px #dbdbdb;
  width: 100%;
  border-radius: 10px;
  height: 544px;
  position: relative;
  overflow: hidden;

  .edit_form {
    padding: 30px 30px 60px 30px;
  }

  .back {
    width: 100%;
    height: 40px;
    background: #F7F7F8aa;
    position: relative;
    z-index: 10;
    top: 0;
    left: 0;
    right: 0;
    box-shadow: 1px 1px 6px #ededed;
    display: flex;
    align-items: center;
    padding: 0 30px;
    color: #a1a1a1;

    i {
      font-size: 22px;
      cursor: pointer;
    }

    span {
      font-size: 16px;
      letter-spacing: 2px;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>