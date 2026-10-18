<template>
  <a-result
      status="error"
      :title="title"
      :subTitle="subTitle"
  >
    <template #extra>
      <a-button type="primary" @click="onGoToHome">返回首页</a-button>
      <a-button @click="onReload">刷新页面</a-button>
      <a-button @click="onClose">关闭</a-button>
    </template>
  </a-result>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {PageEnum} from "@/enums/pageEnum";

defineProps({
  title: {
    type: String,
    default: '啊哦，页面出错了'
  },
  subTitle: String,
})

function onGoToHome() {
  router.push(PageEnum.BASE_HOME);
}

function onReload() {
  location.reload()
}

function onClose() {
  window.close()
  setTimeout(() => alert('当前页面无法通过按钮关闭，请手动关闭'), 1000)
}

</script>

<style scoped lang="less">

</style>