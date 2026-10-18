<template>
  <ShareView :isUpdate="true" :isDetail="isDetail"/>
</template>

<script setup lang="ts">
import {computed} from 'vue';
import {router} from "@/router";
import {SHARE_DETAIL_ROUTER_NAME} from "../../route";
import ShareView from "../ShareView.vue";

const isDetail = computed(() => router.currentRoute.value.name === SHARE_DETAIL_ROUTER_NAME);

</script>

<style scoped lang="less">

</style>
