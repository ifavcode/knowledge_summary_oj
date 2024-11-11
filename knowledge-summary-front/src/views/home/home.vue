<script setup lang='ts'>

</script>
 
<template>
  <div class="home_wrap sm:mt-16 max-sm:mt-14 ml-auto mr-auto">
    <div class="home_left">
      <recommend />
      <tag-group />
    </div>
    <div class="home_right">
      <check-calendar />
      <subscribe-some-thing />
    </div>
  </div>
</template>
 
<style lang='scss' scoped>
.home_wrap {
  max-width: 1200px;
  display: flex;
  width: 100%;
  min-width: 300px;

  @media screen and (min-width: 700px) and (max-width: 1200px) {
    padding-left: 60px;
    padding-right: 60px;
  }

  @media screen and (max-width: 700px) and (min-width: 480px) {
    padding-left: 60px;
    padding-right: 60px;
  }

  @media screen and (max-width:480px) {}

  .home_left {
    flex: 1;

    @media screen and (max-width:480px) {
      width: 100%;
    }
  }

  .home_right {
    min-width: 300px;
    width: 310px;
    margin-left: 30px;
    height: fit-content;
    margin-top: 10px;

    @media screen and (max-width: 800px) {
      display: none;
    }
  }
}
</style>