<script setup lang='ts'>
</script>
 
<template>
  <div class="w-full">
    <div class="w-full mt-10 flex flex-col items-center justify-center">
      <img class="w-1/4" src="../../assets/not_found/user_404.svg" alt="">
      <n-button type="primary" size="large" @click="$router.back()">返回</n-button>
      <span class="text-gray-600 text-lg tracking-widest mt-3">糟糕，用户缺失了！</span>
    </div>
  </div>
</template>
 
<style lang='scss' scoped></style>