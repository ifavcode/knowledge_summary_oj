<script setup lang='ts'>
const props = defineProps<{
  componentAddr: string
}>()
import checkCalendar from '~/components/check_calendar/checkCalendar.vue'
const map: any = {
  checkCalendar
}
</script>
 
<template>
  <div class="w-full">
    <h2 class="font-light mt-2 mb-4">demo案例</h2>
    <div class="w-full max-sm:flex max-sm:justify-center">
      <component :is="map[props.componentAddr]" />
    </div>
  </div>
</template>
 
<style lang='scss' scoped></style>