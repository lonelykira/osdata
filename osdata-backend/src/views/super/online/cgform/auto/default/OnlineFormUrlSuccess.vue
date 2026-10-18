<template>
  <div :class="['p-2']" class="online-wrap">
    <div class="contentArea">
      <svg t="1709697840296" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1485">
        <path
          d="M874.119618 149.859922A510.816461 510.816461 0 0 0 511.997 0.00208a509.910462 509.910462 0 0 0-362.119618 149.857842c-199.817789 199.679789-199.817789 524.581447 0 724.260236a509.969462 509.969462 0 0 0 362.119618 149.857842A508.872463 508.872463 0 0 0 874.119618 874.120158c199.836789-199.679789 199.836789-524.581447 0-724.260236zM814.94268 378.210681L470.999043 744.132295a15.359984 15.359984 0 0 1-5.887994 4.095996c-1.751998 1.180999-2.913997 2.362998-5.276994 2.913997a34.499964 34.499964 0 0 1-13.469986 2.914997 45.547952 45.547952 0 0 1-12.897986-2.303998l-4.095996-2.363997a45.291952 45.291952 0 0 1-7.009992-4.095996l-196.902793-193.789796a34.126964 34.126964 0 0 1-10.555989-25.186973c0-9.37399 3.583996-18.74698 9.98399-25.186974a36.429962 36.429962 0 0 1 50.372947 0l169.98382 167.423824L763.389735 330.220732a37.059961 37.059961 0 0 1 50.371947-1.732998 33.647965 33.647965 0 0 1 11.165988 25.186973 35.544963 35.544963 0 0 1-9.98399 24.575974v-0.04z m0 0"
          fill="currentColor"
          p-id="1486"
        ></path>
      </svg>
      <h1>提交成功</h1>
    </div>
    <div class="btnArea">
      <a-button @click="getBack()">返回</a-button>
      <a-button @click="getBack()">关闭</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  const router = useRouter();
  const getBack = () => {
    router.go(-1);
  };
</script>

<style lang="less" scoped>
  .online-wrap {
    height: 100%;
    // position: relative;
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 1000;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    .contentArea {
      text-align: center;
      padding-top: 10%;
      margin-bottom: 24px;
      svg {
        width: 80px;
        color: #40c409;
        margin-bottom: 24px;
      }
      h1 {
        font-size: 24px;
        line-height: 1;
      }
    }
    .btnArea {
      width: 150px;
      display: flex;
      justify-content: space-between;
    }
  }
</style>
