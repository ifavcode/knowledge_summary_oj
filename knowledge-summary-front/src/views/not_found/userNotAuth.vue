<script setup lang='ts'>

</script>
 
<template>
  <div class="w-full">
    <div class="w-full mt-10 flex flex-col items-center justify-center">
      <img class="w-1/4" src="../../assets/not_found/user_not_auth.svg" alt="">
      <n-button type="primary" size="large" @click="$router.back()">返回</n-button>
      <span class="text-gray-600 text-lg tracking-widest mt-3">你被神秘的结界拦住了！</span>
      <span class="text-gray-600 text-lg tracking-widest mt-3">寻找方式来探索吧~</span>
    </div>
  </div>
</template>
 
<style lang='scss' scoped></style>