<script setup lang='ts'>
const props = withDefaults(defineProps<{
  fontSize?: string
  marginRight?: string
  width?: string
}>(), {
  fontSize: '30px',
  marginRight: '0px',
  width: '36px'
})

</script>
 
<template>
  <span class="logo" :style="props">
    <img src="../../assets/logo.svg" class="w-full" alt="主页">
  </span>
</template>
 
<style lang='scss' scoped>
.logo {
  width: fit-content;
  display: flex;
  align-items: center;
}
</style>