<template>
  <div>这里是你的登录页面</div>
</template>

<script setup lang="ts">

import {useUserStore} from "@/store/modules/user";

const userStore = useUserStore();

// 获取路由的 r

if (userStore.getToken) {

}

</script>

<style scoped lang="less">

</style>