<script setup lang='ts'>
import { FormInst, FormItemRule, useMessage, FormRules } from 'naive-ui'
import { ref } from 'vue';
import { useGlobalStore } from '../../store/globalStore'
import { requestAnonymous } from '~/utils/request';
//@ts-ignore
import Cookies from 'js-cookie'
import { emitter } from '~/utils/index'
interface ModelType {
  userName: string | null
  password: string | null
  isAccept?: boolean | null,
  verifyPassword?: string | null
}
const globalStore = useGlobalStore()
//登录form
const formRef = ref<FormInst | null>(null)
//注册form
const formRef2 = ref<FormInst | null>(null)
const loading = ref<boolean>(false)
const message = useMessage()
//todo 删除
const modelRef = ref<ModelType>({
  userName: '1308003218',
  password: '123456',
  isAccept: true
})
const modelRef2 = ref<ModelType>({
  userName: '',
  password: '',
  verifyPassword: ''
})
const rules: FormRules = {
  userName: [
    {
      required: true,
      validator(_rule: FormItemRule, value: string) {
        return /[0-9A-Za-z]{6,15}/.test(value)
      },
      message: '账号长度为6-15位'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      validator(_rule: FormItemRule, value: string) {
        return value != "" && value != null
      },
    }
  ],
  isAccept: [
    {
      required: true,
      message: '请先同意此协议',
      validator(_rule: FormItemRule, value: boolean) {
        return value
      },
    }
  ]
}
const rules2: FormRules = {
  userName: [
    {
      required: true,
      validator(_rule: FormItemRule, value: string) {
        return /[0-9A-Za-z]{6,15}/.test(value)
      },
      message: '账号长度为6-15位'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      validator(_rule: FormItemRule, value: string) {
        return value != "" && value != null
      },
    }
  ],
  verifyPassword: [
    {
      required: true,
      message: '密码不一致',
      validator(_rule: FormItemRule, value: string) {
        return value == modelRef2.value.password
      },
    }
  ]
}

//登录
function handleValidateButtonClick(e: Event) {
  e.preventDefault()
  formRef.value?.validate((errors) => {
    if (!errors) {
      handleLogin()
    } else {
    }
  })
}
//注册
function handleValidateButtonClick2(e: Event) {
  e.preventDefault()
  formRef2.value?.validate((errors) => {
    if (!errors) {
      handleRegister()
    } else {
    }
  })
}

async function handleRegister() {
  loading.value = true
  const res: any = await requestAnonymous('/api/user/register', {
    method: 'post',
    data: {
      password: modelRef2.value.password,
      userName: modelRef2.value.userName,
      userDesc: "这个用户很懒，没有填什么信息",
      nickName: modelRef2.value.userName
    }
  })
  if (res && res.code == 200) {
    message.success('注册成功，快去登录吧~')
    isRegister.value = false
    modelRef.value.userName = modelRef2.value.userName
    modelRef.value.password = modelRef2.value.password
  } else {
    message.error(res.msg)
  }
  loading.value = false
}

async function handleLogin() {
  loading.value = true
  const { userName, password } = modelRef.value
  const res: AjaxResult<LoginBack> | undefined = await requestAnonymous('/api/user/login', {
    method: 'post',
    data: {
      userName,
      password
    }
  })
  if (res?.code == 200) {
    message.success('登陆成功')
    //@todo 存储加密
    Cookies.set('authorization', res.data?.authorization as string, { expires: res.data?.expirationTime })
    globalStore.userInfo = res.data?.user
    globalStore.isLoginElementShow = false
    globalStore.authorization = res.data?.authorization as string
    res.data?.user.role.forEach(v => {
      globalStore.userAuth.push(v.roleAuth)
      if (v.roleAuth == '*') globalStore.isAdmin = true
    })
    emitter.emit('loginSuccess')
  } else {
    message.error('账号或者密码错误')
  }
  loading.value = false
}

const isRegister = ref<boolean>(false)

const handleRegisterBtn = () => {
  isRegister.value = true
}
</script>

<template>
  <div class="sceen_wrap">
    <div class="box">
      <div class="close_btn" @click="globalStore.isLoginElementShow = false">
        <i class="iconfont" style="font-size: 30px;">&#xe724;</i>
      </div>
      <div class="login_box">
        <transition name="reg">
          <div class="register_box" v-show="isRegister">
            <h1>
              <i class="iconfont" @click="isRegister = false"
                style="font-size: 24px;position: relative;right: calc(50% - 30px);color: #333;cursor: pointer;">&#xe605;</i>
              <logo fontSize="24px" />
            </h1>
            <h2>账号注册</h2>

            <!-- 注册form？ -->
            <n-form class="register_form login_form" ref="formRef2" :model="modelRef2" :rules="rules2"
              label-placement="left">
              <n-form-item path="userName">
                <n-input v-model:value="modelRef2.userName" placeholder="账号" />
              </n-form-item>
              <n-form-item path="password">
                <n-input v-model:value="modelRef2.password" type="password" placeholder="输入密码"
                  :input-props="{ autocomplete: false }" />
              </n-form-item>
              <n-form-item path="verifyPassword">
                <n-input v-model:value="modelRef2.verifyPassword" type="password" placeholder="输入确认密码"
                  :input-props="{ autocomplete: false }" @keydown.enter="handleValidateButtonClick2" />
              </n-form-item>
              <n-button class="btn" color="#242528" :loading="loading" type="primary"
                @click="handleValidateButtonClick2">
                注册
              </n-button>
            </n-form>
          </div>
        </transition>
        <h1>
          <logo fontSize="24px" />
        </h1>
        <h2>账号密码登录</h2>

        <n-form class="login_form" ref="formRef" :model="modelRef" :rules="rules" label-placement="left">
          <n-form-item path="userName">
            <n-input v-model:value="modelRef.userName" placeholder="账号" />
          </n-form-item>
          <n-form-item path="password">
            <n-input v-model:value="modelRef.password" type="password" placeholder="输入密码"
              @keydown.enter="handleValidateButtonClick" />
          </n-form-item>
          <n-button class="btn" color="#242528" :loading="loading" type="primary" @click="handleValidateButtonClick">
            登录
          </n-button>

          <div style="display: flex; justify-content: space-between;margin-top: 10px;">
            <p style="cursor: pointer;" @click="handleRegisterBtn">注册</p>
            <p style="font-size: 15px;color: rgb(88, 88, 88);cursor: pointer;">忘记密码</p>
          </div>

          <div class="quick_login">
            <i class="iconfont" style="font-size: 34px;color: #37BD67;">&#xe63f;</i>
            <i class="iconfont" style="font-size: 34px;color: #48A4DC;">&#xe73e;</i>
          </div>
          <div style="marginTop: 20px;">
            <n-form-item path="isAccept">
              <n-checkbox v-model:checked="(modelRef.isAccept as boolean)" style="marginRight:10px;" />
              同意<a href="#">《用户协议》</a>和<a href="#">《隐私协议》</a>
            </n-form-item>
          </div>
        </n-form>
      </div>
    </div>
  </div>
</template>

<style lang='scss' scoped>
//todo 弹出加动画
.sceen_wrap {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  background: #00000044;
  display: flex;
  justify-content: center;
  align-items: center;

  .box {
    width: 384px;
    height: 496px;
    position: relative;

    .close_btn {
      position: absolute;
      top: 0;
      right: 0;
      transform: translateX(50%) translateY(-50%);
      cursor: pointer;
      z-index: 20;

      i {
        &:hover {
          color: #4e4e4e;
        }
      }
    }

    .login_box {
      background: #fff;
      border-radius: 15px;
      padding: 20px;
      width: 384px;
      height: 496px;
      position: relative;

      .register_box {
        background: #fff;
        border-radius: 15px;
        padding: 20px;
        width: 100%;
        height: 100%;
        position: absolute;
        top: 0;
        left: 100%;
        transform: translateX(-100%);
        width: 384px;
        height: 496px;
        z-index: 10;

        h1 {
          display: flex;
          justify-content: center;
        }

        h2 {
          font-weight: normal;
          margin-top: 20px;
        }
      }

      h1 {
        display: flex;
        justify-content: center;
      }

      h2 {
        font-weight: normal;
        margin-top: 20px;
      }

      .login_form {
        margin-top: 20px;

        .btn {
          width: 100%;
          margin-top: 20px;
        }
      }

      .quick_login {
        text-align: center;
        margin-top: 20px;

        i {
          padding: 0 30px;
        }
      }
    }
  }

}
</style>

<style lang='scss' scoped>
.reg-enter-active {
  animation: fadeRightIn .3s forwards;
}

.reg-leave-active {
  animation: fadeRightIn .2s reverse forwards;
}

@keyframes fadeRightIn {
  0% {
    opacity: 0;
    transform: translateX(-70%);
  }

  100% {
    opacity: 1;
    transform: translateX(-100%);
  }
}
</style>