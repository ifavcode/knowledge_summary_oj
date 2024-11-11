<script setup lang='ts'>
import { FormInst, useMessage } from 'naive-ui';
import { FormRules } from 'naive-ui/lib';
import { request } from '~/utils/request';
import { emitter } from '~/utils/index'
import moment from 'moment'
import 'moment/dist/locale/zh-cn'
moment.locale('zh-cn')
const subList = ref<GetSubIem[]>([])
const overTimeSubList = ref<GetSubIem[]>([])
const isSubPopupShow = ref<boolean>(false)
const subBtnLoading = ref<boolean>(false)
const message = useMessage()
const popupWrap = ref()

const getSubList = async () => {
  const res: AjaxResult<GetSubInfoRes> = await request('/api/subscribe/info')
  let arr: GetSubInfoRes = res?.data as GetSubInfoRes
  //按照开始日期升序排序
  if (arr) {
    subList.value = arr.notOverTime as GetSubIem[];
    overTimeSubList.value = arr.overTime as GetSubIem[];
  }
}

const formRef = ref<FormInst | null>(null)
const chooseDate1 = ref()
const chooseDate2 = ref()
const modelRef = ref<GetSubIem>({
  startTime: '',
  endTime: '',
  todoThing: '',
  todoLog: '',
  stime: new Date().getTime(),
  etime: new Date().getTime(),
  shour: new Date().getHours(),
  ehour: new Date().getHours()
})
const rules: FormRules = {
  stime: [
    {
      required: true,
      message: '请输入开始日期'
    }
  ],
  etime: [
    {
      required: true,
      message: '请输入结束日期'
    }
  ],
}

onClickOutside(popupWrap, () => {
  // isSubPopupShow.value = false
}, {
  ignore: [chooseDate1, chooseDate2]
})

function handleCheckBtn(e: MouseEvent) {
  e.preventDefault()
  subBtnLoading.value = true
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      if ((modelRef.value.stime == modelRef.value.etime) && (modelRef.value.shour as number) > (modelRef.value.ehour as number)) {
        message.error('开始日期不能晚于结束日期!')
        return
      }
      const res: AjaxResult<GetSubInfoRes> = await request('/api/subscribe/date', {
        method: 'post',
        data: {
          startTime: moment(modelRef.value.stime).format('L') + " " + hourFormat(modelRef.value.shour as number),
          endTime: moment(modelRef.value.etime).format('L') + " " + hourFormat(modelRef.value.ehour as number),
          todoThing: modelRef.value.todoThing,
          todoLog: modelRef.value.todoLog
        }
      })
      if (res.code == 200) {
        message.success(res.msg)
        isSubPopupShow.value = false
        getSubList()
      } else {
        message.error(res.msg)
      }
    } else {
    }
    subBtnLoading.value = false
  })
}

const hourFormat = (hour: number) => {
  if (hour < 10) {
    return "0" + hour;
  } else {
    return hour
  }
}

function startTimeDisabled(ts: number) {
  return ts <= Date.now() - 1000 * 60 * 60 * 24
}

function endTimeDisabled(ts: number) {
  return ts <= modelRef.value.stime - 1000 * 60 * 60
}

function ehourValidator(x: number) {
  let up: number = 0;
  if (modelRef.value.stime == modelRef.value.etime) {
    up = modelRef.value.shour as number
  }
  return x >= up && x <= 23
}

//计算一共的时间
const remainTime = (startTime: string, endTime: string) => {
  let day = parseInt((
    (new Date(endTime as string + ":00:00").getTime() - new
      Date(startTime as string + ":00:00").getTime()) / 1000 / 60 / 60 / 24
  ).toString())
  let hour = (new Date(endTime as string + ":00:00").getTime() - new Date(startTime as string +
    ":00:00").getTime()) / 1000 / 60 / 60 % 24
  return [day, hour + 1]
}

onBeforeMount(() => {
  getSubList()
})

onMounted(() => {
  emitter.on('loginSuccess', () => {
    getSubList()
  })
  emitter.on('logoutSuccess', () => {
    subList.value = []
  })
})
</script>
 
<template>
  <div class="subscribe_something_wrap">
    <div class="icon">
      <i class="iconfont" style="fontSize: 30px;color: #a5a5a5;">&#xe60d;</i>
      <p>
        <span>预约事项</span>
        <n-popover trigger="hover">
          <template #trigger>
            <i class="iconfont" style="font-size: 19px; color: #808080;margin-left: 10px;cursor: pointer;">&#xe600;</i>
          </template>
          <span>设定最长预约时间7天，重复时间段最多占10个用户</span>
        </n-popover>
      </p>
    </div>
    <div class="sub_wrap">
      <!-- 未过期 -->
      <p style="color: #313131; font-weight: bold;">进行中</p>
      <n-empty size="small" description="这里空空的" v-show="subList.length == 0">
      </n-empty>
      <div class="sub_item not_overtime_item" v-for="(item, index) in subList" :key="index">
        <span>{{ item.todoThing || '未命名预定' }}</span>
        <p style="font-weight: normal;">共{{ remainTime(item.startTime as string, item.endTime as string)[0] }}天，{{
          remainTime(item.startTime as string, item.endTime as string)[1] }}时
        </p>
        <span class="sub_time">{{ item.startTime }}时 ~ {{ item.endTime }}时</span>
      </div>
      <!-- 过期 -->
      <p style="margin-top: 10px;color: #313131;">过去的预约</p>
      <n-empty size="small" description="这里空空的" v-show="overTimeSubList.length == 0"></n-empty>
      <div class="sub_item" v-for="(item, index) in overTimeSubList" :key="index">
        <span>{{ item.todoThing || '未命名预定' }}</span>
        <p style="font-weight: normal;">共{{ remainTime(item.startTime as string, item.endTime as string)[0] }}天，{{
          remainTime(item.startTime as string, item.endTime as string)[1] }}时
        </p>
        <span class="sub_time">{{ item.startTime }}时 ~ {{ item.endTime }}时</span>
      </div>
    </div>
    <div class="add_btn">
      <n-popover trigger="hover">
        <template #trigger>
          <i @click="isSubPopupShow = !isSubPopupShow" class="iconfont"
            style="color: #808080;font-size: 26px;cursor: pointer;">&#xe606;</i>
        </template>
        <span>新增预定</span>
      </n-popover>
    </div>
    <transition name="popup" appear>
      <div class="popup_wrap" v-show="isSubPopupShow" ref="popupWrap">
        <!-- 弹出层 -->
        <h2>预定信息</h2>
        <n-form ref="formRef" :model="modelRef" :rules="rules" label-placement="left">
          <n-form-item path="stime" label="开始日期">
            <n-date-picker ref="chooseDate1" v-model:value="modelRef.stime" :is-date-disabled="startTimeDisabled"
              type="date" clearable />
            <n-input-number style="width: 80px;" v-model:value="modelRef.shour"
              :validator="(x: number) => x >= 0 && x <= 23" />
            <span style="font-size: 16px;margin-left:10px;">时</span>
          </n-form-item>
          <n-form-item path="etime" label="结束日期">
            <n-date-picker ref="chooseDate2" v-model:value="modelRef.etime" :is-date-disabled="endTimeDisabled"
              type="date" clearable />
            <n-input-number style="width: 80px;" v-model:value="modelRef.ehour" :validator="ehourValidator" />
            <span style="font-size: 16px;margin-left:10px;">时</span>
          </n-form-item>
          <n-form-item path="todoThing" label="预定事项">
            <n-input v-model:value="modelRef.todoThing" />
          </n-form-item>
          <n-form-item path="todoLog" label="预定备注">
            <n-input v-model:value="modelRef.todoLog" />
          </n-form-item>
          <n-row>
            <n-col :span="24">
              <div style="display: flex; justify-content: flex-end">
                <n-button color="#18A058" @click="handleCheckBtn" :loading="subBtnLoading">
                  预定
                </n-button>
              </div>
            </n-col>
          </n-row>
        </n-form>
      </div>
    </transition>
  </div>
</template>
 
<style lang='scss' scoped>
.subscribe_something_wrap {
  margin-top: 30px;
  background: #fff;
  padding: 10px 20px;
  box-shadow: 1px 1px 6px #e7e7e7;
  border-radius: 10px;
  position: relative;

  .icon {
    display: flex;
    align-items: center;

    p {
      margin-left: 10px;
      font-size: 16px;
      color: #363636;
    }
  }

  .sub_wrap {
    .sub_item {
      width: 100%;
      height: fit-content;
      color: rgb(36, 36, 36);
      border-radius: 10px;
      cursor: pointer;
      display: flex;
      justify-content: center;
      padding: 6px 10px;
      background: #e4e4e4;
      letter-spacing: 1px;
      flex-direction: column;
      transition: all .4s;

      &:hover {
        transform: scale(0.95);
      }

      &:nth-child(n + 2) {
        margin-top: 6px;
      }

      .sub_time {
        font-weight: normal;
        font-size: 12px;
      }
    }

    .not_overtime_item {
      background: #2aca75;
      color: #fff;
      font-weight: 600;
    }
  }

  .add_btn {}

  .popup_wrap {
    box-shadow: 1px 1px 6px #e7e7e7;
    width: 420px;
    height: fit-content;
    position: absolute;
    bottom: 0;
    left: 0;
    border-radius: 10px;
    transform: translateX(-102%);
    padding: 15px 20px;
    background: #fff;

    h2 {
      font-weight: normal;
      font-size: 20px;
      margin-bottom: 10px;
    }
  }
}
</style>  

<style lang='scss' scoped>
.popup-enter-active {
  animation: fadeRightIn .3s forwards;
}

.popup-leave-active {
  animation: fadeRightIn .2s reverse forwards;
}

@keyframes fadeRightIn {
  0% {
    opacity: 0;
    transform: translateX(-90%);
  }

  100% {
    opacity: 1;
    transform: translateX(-102%);
  }
}
</style>