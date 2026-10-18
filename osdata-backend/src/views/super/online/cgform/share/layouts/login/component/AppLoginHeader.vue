<template>
  <div class="login-header">
    <img :src="loginLogo" alt="敲敲云" class="login-image" @click="logoImgClick"/>
    <span class="login-header-name"> </span>
  </div>
</template>

<script lang="ts" setup name="app-login-header">
  import loginLogo from '/@/assets/images/logo.png';

  /**
   * logo图片点击之后跳转敲敲云官网
   */
  function logoImgClick() {
    window.open("https://www.qiaoqiaoyun.com/","_target")
  }
  
</script>

<style lang="less" scoped>
  .login-header {
    height: 60px;
    display: flex;
    vertical-align: middle;
  }
  .login-image {
    height: 40px;
    cursor: pointer;
  }
  .login-header-name {
    font-size: 24px;
    font-weight: 700;
    line-height: 60px;
  }
</style>
