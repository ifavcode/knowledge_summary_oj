<script setup lang='ts'>
import moment from 'moment'
import 'moment/dist/locale/zh-cn'
import { request } from '~/utils/request';

moment.locale('zh-cn')

const route = useRoute()
const scbar = ref()

const ansRes: any = []
const date = new Date()
const today = {
  year: date.getFullYear(),
  month: date.getMonth(),
  day: date.getDate()
}
let endTime = moment(date, 'YYYY/MM/DD').format('L')
let startTime = moment((today.year - 1) + '/' + (today.month + 1) + '/' + today.day, 'YYYY/MM/DD').format('L')
function init() {
  let days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
  //上一年开始遍历
  //下标从0开始
  for (let month = today.month; month <= 12 + today.month; month++) {
    let year = Math.floor(month / 12); //0->前一年 1->今年
    let m = month % 12;
    let remainDay
    let week
    if (month != today.month && month != 12 + today.month) {
      remainDay = days[m]
      week = new Date(today.year - (year == 0 ? 1 : 0) + '/' + (m + 1) + '/1').getDay()
    } else {
      if (month == today.month) {
        remainDay = days[m] - today.day
        week = new Date(today.year - (year == 0 ? 1 : 0) + '/' + (m + 1) + '/' + today.day).getDay()
      } else {
        remainDay = today.day
        week = new Date(today.year - (year == 0 ? 1 : 0) + '/' + (m + 1) + '/1').getDay()
      }
    }

    ansRes.push({
      year,
      month: m,
      remainDay,
      week
    })
  }

}

const viewsList = ref<ViewsInfo>()
const getViewsList = async () => {
  const res: AjaxResult<ViewsInfo> = await request('/api/user/views/statistics', {
    method: 'post',
    data: {
      userId: route.params.userId,
      startTime,
      endTime
    }
  })
  viewsList.value = res.data as ViewsInfo
}

const formatDate = (year: number, month: number, day: number) => {
  return moment(today.year + (year == 0 ? -1 : 0) + '/' + (month + 1) + '/' + (day + 1), 'YYYY/MM/DD').format('L')
}

watch(route, () => {
  getViewsList()
})

init()
getViewsList()

onMounted(() => {
  //移动端滚到末尾
  nextTick(() => {
    if (window.innerWidth <= 480) {
      scbar.value.scrollTo({
        left: 680
      })
    }
  })
})

</script>
 
<template>
  <div class="calander_box">
    <p class="view_title">近一年共浏览<span style="font-weight: bold;padding: 0 5px;">{{ viewsList?.totalCnt != null ?
      viewsList?.totalCnt : '...' }}</span>次
    </p>
    <n-scrollbar x-scrollable ref="scbar">
      <div class="mobile_wrap">
        <div class="calander_view_g_wrap">
          <div class="views_wrap" v-for="month in ansRes" v-show="month.remainDay > 0">
            <div class="views_month">
              <div class="views_day" v-for="_offset in month.week" style="background: transparent;">
              </div>
              <div v-for="(_day, index) in month.remainDay">
                <n-popover trigger="hover" style="background: #2A2A2A;color: #fff;" :arrow-style="{background:'#2A2A2A'}">
                  <template #trigger>
                    <div class="views_day" :style="{
                      background: viewsList?.colors[formatDate(month.year, month.month, index)]
                    }">
                    </div>
                  </template>
                  <span><span style="font-weight: bold;padding: 0 5px;">{{
                    viewsList?.views[formatDate(month.year, month.month, index)] || 0 }}
                    </span>次浏览 {{ formatDate(month.year, month.month, index) }}</span>
                </n-popover>
              </div>
            </div>
            <p style="color: #a2a2a2;">{{ month.month + 1 + '月' }}</p>
          </div>
        </div>
      </div>
    </n-scrollbar>
  </div>
</template>
 
<style lang='scss' scoped>
.calander_box {
  width: 100%;
  padding: 20px;

  .view_title {
    font-size: 18px;
    padding-left: 10px;
    margin-bottom: 20px;
  }

  .mobile_wrap {

    @media screen and (max-width:480px) {
      width: 630px;
      white-space: nowrap;
      overflow-anchor: auto;
    }

    .calander_view_g_wrap {
      display: flex;
      justify-content: space-between;

      .views_wrap {
        width: 100%;
        margin-right: 8px;

        p {
          text-align: center;
          margin-top: 10px;
        }

        .views_month {
          width: calc(100% / 12);
          height: 78px;
          display: flex;
          flex-direction: column;
          flex-wrap: wrap;

          @media screen and (max-width:1200px) {
            height: 50px;
          }

          .views_day {
            width: 8px;
            height: 8px;
            margin: 0 2px 2px 0;
            border-radius: 2px;
            background: #F7F7F8;
            cursor: pointer;

            @media screen and (max-width:1200px) {
              width: 5px;
              height: 5px;
            }
          }
        }
      }
    }
  }

}
</style>