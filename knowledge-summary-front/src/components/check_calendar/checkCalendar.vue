<script setup lang='ts'>
import { useMessage } from 'naive-ui';
import { reactive } from 'vue';
import { request } from '~/utils/request';
import gsap from 'gsap'
import { getAssetsImages, emitter, checkLogin } from '~/utils/index'

function getDays(year: number, month: number) {
  let days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
  if ((year % 4 === 0) && (year % 100 !== 0 || year % 400 === 0)) {
    days[1] = 29
  }
  return days[month]
}

const message = useMessage()

//是否签到
const isCheck = ref<boolean>(false)

const dateEntity = reactive({
  todayIndex: 0,//周几
  totalDay: 0,//月 总天数
  year: 0,//年
  month: 0,//月 从1开始
  day: 0,//日
  startWeek: 0,//本月1号开始周
  continuesDay: 0//连续打卡天数 todo
})

//当前月
let curMonth = new Date().getMonth() + 1;//索引1开始
const calendarImageTip = ref<Element | null>(null)
const weekList = ['日', '一', '二', '三', '四', '五', '六']
const tipText = ref<string>('')
const speakList: Array<string> = ['哎哟，你干嘛~', '嗨嗨嗨', '点点点点点，啊', '🐓🐓🐓', '师傅，你是做什么工作的？', '我爱学习!!!', '我宣布，我是大帅b']
const dayMap: any = {
  1: '一',
  2: '二',
  3: '三',
  4: '四',
  5: '五',
  6: '六',
  7: '七',
  8: '八',
  9: '九',
  10: '十',
  11: '十一',
  12: '十二',
}
const imgList = [
  'tuzi.png',
  'xiaogou.png',
  'xiaozhu.png',
  'xiaoji.png',
  'zongxiong.png',
  'houzi.png',
  'qingwa.png',
]

function f(date: InstanceType<typeof Date>) {
  let y = date.getFullYear()
  dateEntity.year = y;
  dateEntity.month = date.getMonth() + 1;
  dateEntity.day = date.getDate()
  dateEntity.totalDay = getDays(dateEntity.year, dateEntity.month - 1)
  dateEntity.todayIndex = date.getDay()
  if (date.getMonth() == 0) {
    //上一年
    y--;
  }
  dateEntity.startWeek = new Date(y, dateEntity.month - 1).getDay();
}

async function toggleMonth(count: number) {
  dateEntity.month += count
  if (dateEntity.month == curMonth) {
    f(new Date())
    await getCheckList(new Date())
  } else {
    f(new Date(dateEntity.year, dateEntity.month - 1))
    await getCheckList(new Date(dateEntity.year, dateEntity.month - 1))
  }
}

async function isCheckIn(): Promise<boolean> {
  const res: boolean = await request('/api/check/have', {
    method: 'post'
  })
  return res
}

//月 签到日志 0未签 1已签
const checkList = ref<{
  [propName: string]: Array<number>
}>({
  1: new Array<number>(31), 2: new Array<number>(31), 3: new Array<number>(31), 4: new Array<number>(31),
  5: new Array<number>(31), 6: new Array<number>(31), 7: new Array<number>(31), 8: new Array<number>(31),
  9: new Array<number>(31), 10: new Array<number>(31), 11: new Array<number>(31), 12: new Array<number>(31)
})

const getCheckList = async (date: InstanceType<typeof Date>) => {
  const res: AjaxResult<Array<number>> = await request('/api/check/all', {
    params: {
      year: date.getFullYear(),
      month: date.getMonth() + 1
    }
  })
  if (res) {
    checkList.value[date.getMonth() + 1] = res.data as Array<number>
  }
}

onBeforeMount(async () => {
  const date = new Date();
  f(date)
  isCheck.value = await isCheckIn()
  getCheckList(date)
})

const handleCheckBtn = async () => {
  if (!checkLogin(true)) {
    return
  }
  const res: AjaxResult<null> = await request('/api/check/in', {
    method: 'post'
  })
  if (res.code != 200) {
    message.error(res.msg)
  } else {
    //可爱提示
    tipText.value = '签到成功~'
    gsap.to('#calendar_image_tip', { opacity: 1, direction: 1 })
    setTimeout(() => {
      gsap.to('#calendar_image_tip', { opacity: 0, display: 'none', direction: 1 })
    }, 2500);
    isCheck.value = true
  }
  getCheckList(new Date())
}

//点击图片
let key: NodeJS.Timeout;
const handleClickImage = () => {
  //[0,1) => [0,len)
  clearTimeout(key)
  gsap.to('#calendar_image_tip', { opacity: 0, display: 'inline-block', direction: 0 })
  tipText.value = speakList[Math.floor(Math.random() * (speakList.length))]
  gsap.to('#calendar_image_tip', { opacity: 1, direction: 1 })
  key = setTimeout(() => {
    gsap.to('#calendar_image_tip', { opacity: 0, display: 'none', direction: 1 })
  }, 2500);
}

onMounted(() => {
  emitter.on('loginSuccess', async () => {
    isCheck.value = await isCheckIn()
    getCheckList(new Date())
  })
  emitter.on('logoutSuccess', async () => {
    isCheck.value = false
    checkList.value = {
      1: new Array<number>(31), 2: new Array<number>(31), 3: new Array<number>(31), 4: new Array<number>(31),
      5: new Array<number>(31), 6: new Array<number>(31), 7: new Array<number>(31), 8: new Array<number>(31),
      9: new Array<number>(31), 10: new Array<number>(31), 11: new Array<number>(31), 12: new Array<number>(31)
    }
  })
})
</script>
 
<template>
  <div class="check_calendar_wrap bg-white">
    <div class="decoration_image" @click="handleClickImage">
      <span style="opacity: 0;" id="calendar_image_tip" ref="calendarImageTip">{{ tipText }}</span>
      <img :src="getAssetsImages(imgList[dateEntity.todayIndex % 7])" />
    </div>
    <div class="title_wrap">
      <n-button-group>
        <n-button tertiary circle :disabled="dateEntity.month === 1" @click="toggleMonth(-1)">
          &lt;
        </n-button>
        <n-button tertiary>{{ dayMap[dateEntity.month] + '月' }}</n-button>
        <n-button tertiary circle :disabled="dateEntity.month === 12" @click="toggleMonth(1)">
          &gt;
        </n-button>
      </n-button-group>
    </div>
    <div class="calendar_box">
      <div class="week_tag_item" v-for="(item, index) in  weekList ">
        <span
          :style="index == dateEntity.todayIndex && curMonth === dateEntity.month ? 'background:#18A058;color:#fff;' : ''">{{
            item }}</span>
      </div>
      <div class="week_tag_item date_tag_item" v-for=" _item  in  dateEntity.startWeek ">
        <!-- 占位符 -->
      </div>
      <div class="week_tag_item date_tag_item" v-for="( item, index ) in  dateEntity.totalDay ">
        <p :class="index == dateEntity.day - 1 && curMonth === dateEntity.month && checkList[dateEntity.month][index + 1] == 0 ?
          'active_month' : ''">
          <img v-if="checkList[dateEntity.month][index + 1] == 1" src="../../assets/is_check.png" />
          <span v-else>{{ item }}</span>
        </p>
      </div>
    </div>
    <n-button style="width: 100%;margin-top: 10px;" :type="isCheck ? 'tertiary' : 'success'" :disabled="isCheck"
      @click="handleCheckBtn">
      {{ isCheck ? '今日已签到√' : '签到' }}
    </n-button>
  </div>
</template>
 
<style lang='scss' scoped>
.check_calendar_wrap {
  padding: 10px 10px 20px 10px;
  box-shadow: 1px 1px 6px #e7e7e7;
  height: fit-content;
  border-radius: 10px;
  position: relative;
  width: 300px;

  .decoration_image {
    width: 65px;
    position: absolute;
    top: 0;
    left: 0;
    transform: translateX(-30%) translateY(-30%);
    z-index: 10;
    cursor: pointer;

    span {
      position: absolute;
      background: #fff;
      border-radius: 10px;
      width: 130px;
      box-shadow: 1px 1px 10px #3c3c3c55;
      color: #3c3c3c;
      font-size: 14px;
      display: inline-block;
      letter-spacing: 1px;
      transform: translateY(10px) translateX(70px);
      padding: 5px 10px;
      font-weight: bold;
    }

    img {
      width: 100%;
    }
  }

  .title_wrap {
    display: flex;
    justify-content: center;
    margin-bottom: 0px;
    margin-top: 10px;
  }

  .calendar_box {
    display: flex;
    flex-wrap: wrap;
    margin-top: 10px;

    .week_tag_item {
      width: calc(100% / 7);
      height: 34px;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #a5a5a5;

      span {
        display: inline-block;
        width: 25px;
        height: 25px;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 50%;
        padding: 5px;

        &:hover {
          background: #18A058;
          color: #fff;
        }
      }
    }

    .date_tag_item {
      color: #3c3c3c;
      cursor: pointer;

      img {}

      p {
        display: inline-block;
        width: 25px;
        height: 25px;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 50%;

        span {
          width: 25px;
          height: 25px;
        }
      }

      .active_month {
        background: #18A058;
        color: #fff;
        cursor: pointer;
      }
    }
  }
}
</style>