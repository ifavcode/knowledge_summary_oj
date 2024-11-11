<script setup lang='ts'>
const szTotalEle = ref()
const szContentScrollDown = () => {
  window.scrollTo({
    top: szTotalEle.value.offsetHeight,
    behavior: 'smooth'
  })
}
</script>
 
<template>
  <div class="sm:mt-20 max-sm:pt-12 ml-auto mr-auto sm:w-4/5 max-sm:w-full" ref="szTotalEle">
    <div class="custom_height bg-gradient-to-r bg-gray-100 sm:flex rounded-md overflow-hidden">
      <XzLeftBar @szContentScrollDown="szContentScrollDown" />
      <XzRightContent />
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.custom_height {
  height: calc(100vh - 100px);

  @media screen and (max-width:480px) {
    height: fit-content;
    min-height: 90vh;
  }
}
</style>