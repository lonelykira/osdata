<template>
  <div class="login-box" v-if="type === 'login'">
    <div class="login-subject">
      <AppLoginHeader />
      <div class="flex-row align-items-center margin-top10">
        <div class="login-title">
          {{ loginType === 'accountLogin' ? t('sys.login.signInFormTitle') : t('sys.login.mobileSignInFormTitle') }}
        </div>
      </div>
      <div class="content-box">
        <div v-show="loginType === 'accountLogin'">
          <AccountLoginForm ref="accountLoginRef" @login="loginHandleType" @forget-pwd="forgetPwdClick" @login-success="loginSuccess" />
        </div>
        <div v-show="loginType === 'phoneLogin'">
          <PhoneLoginForm
            @login="loginHandleType"
            @login-success="loginSuccess"
            :bindThirdAccount="bindThirdAccount"
            @bind-third-phone="bindThirdPhone"
          />
        </div>
        <div class="text-center" v-if="!bindThirdAccount">
          <div class="login-other">
            {{ t('sys.login.otherSignIn') }}
          </div>
          <div style="width: 100%; display: flex">
            <div class="aui-third-login">
              <a title="微信" @click="onThirdLogin('wechat_open')">
                <WechatFilled style="color: rgb(75, 176, 79)" />
              </a>
            </div>
          </div>
          <div class="line"></div>
          <div class="register-account pointer" @click="registerHandleClick">{{ t('sys.login.registerButton') }}</div>
        </div>
      </div>
    </div>
  </div>
  <div v-show="type === 'register'">
    <a-spin :spinning="spinningLogin">
      <AppRegister
        ref="appRegisterRef"
        @return-login="returnLogin"
        :bindThirdAccount="bindThirdAccount"
        @login-account="loginAccount"
        @bind-third-account = "bindAccountThird"
      />
    </a-spin>
  </div>
  <div v-show="type === 'forgetPwd'">
    <AppForgetPassword ref="appForgetPwdRef" @return-login="returnLogin" @login-account="loginAccount" />
  </div>
  <div v-show="type === 'tenant'">
    <AppTenant @success="windowReload"/>
  </div>
  <div class="login-language" v-show="type !== 'thirdLogin'">
    <Icon icon="ant-design:global-outlined" style="font-size: 13px; color: #9e9e9e" />
    <a-dropdown :trigger="['click']" placement="top">
      <span class="language-drop pointer" @click.prevent>
        {{ languageValue === 'zh_CN' ? 'CN' : 'EN' }}
        <Icon icon="ant-design:down-outlined" style="color: #9e9e9e; font-size: 13px" />
      </span>
      <template #overlay>
        <a-menu v-model:value="languageValue" @click="handleMenuEvent">
          <a-menu-item key="zh_CN">
            <span>简体中文</span>
          </a-menu-item>
          <a-menu-item key="en">
            <span>English</span>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
  <!-- 第三方登录相关弹框 -->
  <AppThirdForm ref="thirdModalRef" @type="thirdLoginType" @login-success="loginSuccess" />
</template>

<script lang="ts" setup name="myapps-applogin">
  import { ref, reactive, unref, computed, onMounted, toRaw, nextTick } from 'vue';
  import AppLoginHeader from './component/AppLoginHeader.vue';
  import AppRegister from './component/AppRegister.vue';
  import AccountLoginForm from './component/AccountLoginForm.vue';
  import PhoneLoginForm from './component/PhoneLoginForm.vue';
  import AppForgetPassword from './component/AppForgetPassword.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { GithubFilled, WechatFilled, DingtalkCircleFilled } from '@ant-design/icons-vue';
  import { IconFont } from '/@/utils/iconfont2';
  import { Rule } from '/@/components/Form';
  import { useFormRules } from '/@/views/sys/login/useLogin';
  import { getCodeInfo } from '/@/api/sys/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import AppTenant from './component/AppTenant.vue';
  import { router } from '/@/router';
  import AppThirdForm from './component/AppThirdForm.vue';
  import { LocaleType } from '/#/config';
  import { DropMenu } from '/@/components/Dropdown';
  import { useLocale } from '/@/locales/useLocale';
  import { PageEnum } from '/@/enums/pageEnum';

  const { changeLocale, getLocale } = useLocale();
  const thirdModalRef = ref();
  const userStore = useUserStore();
  //登录方式：手机号/账号
  const loginType = ref<string>('accountLogin');
  //注册页面
  const appRegisterRef = ref();
  //登录页面
  const accountLoginRef = ref();
  //忘记密码页面
  const appForgetPwdRef = ref();
  const { t } = useI18n();

  //方式 判端登录、注册、忘记密码等
  const type = ref<string>('login');
  //手机号登录表单
  const loginPhoneRef = ref();
  const formPhoneState = reactive<any>({
    phone: '',
    smscode: '',
  });
  const { notification, createErrorModal } = useMessage();
  //中文还是英文
  const languageValue = ref<string>('zh_CN');
  //当前是否为绑定账号
  const bindThirdAccount = ref<boolean>(false);
  const spinningLogin = ref<boolean>(false);
  /**
   * 注册
   */
  function registerHandleClick() {
    type.value = 'register';
    setTimeout(() => {
      appRegisterRef.value.clearValidate();
    }, 300);
  }

  /**
   * 返回登录页面
   */
  function returnLogin() {
    type.value = 'login';
    loginType.value = 'accountLogin';
  }

  /**
   * 返回登录页面并赋值
   */
  function loginAccount(values) {
    type.value = 'login';
    loginType.value = 'accountLogin';
    setTimeout(() => {
      accountLoginRef.value.setAccountData(values);
    }, 300);
  }

  /**
   * 显示手机号登录
   * @param type
   */
  function loginHandleType(type) {
    loginType.value = type;
  }

  /**
   * 忘记密码
   */
  function forgetPwdClick() {
    type.value = 'forgetPwd';
  }

  /**
   * 登陆成功
   */
  async function loginSuccess(value) {
    //登录成功之后需要查询租户
    const loginInfo = toRaw(userStore.getLoginInfo);
    const { tenantList } = loginInfo;
    if (!tenantList || tenantList.length === 0) {
      type.value = 'tenant';
    } else {
      notification.success({
        message: t('sys.login.loginSuccessTitle'),
        description: `${t('sys.login.loginSuccessDesc')}: ${value}`,
        duration: 3,
      });
      //update-begin---author:wangshuai ---date:20230424  for：【QQYUN-5195】登录之后直接刷新页面导致没有进入创建组织页面------------
      let redirect = router.currentRoute.value?.query?.redirect as string;
      if (redirect) {
        // 当前页面打开
        window.open(decodeURIComponent(redirect), '_self')
        return;
      }
      //update-end---author:wangshuai ---date:20230424  for：【QQYUN-5195】登录之后直接刷新页面导致没有进入创建组织页面------------
      await router.replace((userStore.getUserInfo && userStore.getUserInfo.homePath) || PageEnum.BASE_HOME);
      windowReload();
    }
  }

  /**
   * 浏览器刷新
   */
  function windowReload() {
    window.location.reload();
  }

  /**
   * 第三方登录
   * @param type
   */
  function onThirdLogin(type) {
    thirdModalRef.value.onThirdLogin(type);
  }

  async function toggleLocale(lang: LocaleType | string) {
    await changeLocale(lang as LocaleType);
  }

  function handleMenuEvent({ key }) {
    languageValue.value = key;
    if (unref(getLocale) === key) {
      return;
    }
    toggleLocale(key);
  }

  //==================================绑定第三方登录账号=============================================
  /**
   * 第三方登录回调事件
   * @param values
   */
  function thirdLoginType(values) {
    nextTick(() => {
      if (values.loginType != 'thirdLogin') {
        bindThirdAccount.value = true;
        thirdModalRef.value.hideBindThirdAccount();
      }
      if (values.loginType === 'login') {
        loginType.value = 'phoneLogin';
      }
      type.value = values.loginType;
    });
  }

  /**
   * 绑定手机号
   */
  async function bindThirdPhone(values) {
    spinningLogin.value = true;
    await thirdModalRef.value.bindThirdAccount(values);
    spinningLogin.value = false;
  }

  /**
   * 绑定第三方账号
   * @param values
   */
  async function bindAccountThird(values) {
    spinningLogin.value = true;
    await thirdModalRef.value.createAccountBindThird(values);
    spinningLogin.value = false;
  }
  //==================================绑定第三方登录账号=============================================

  onMounted(() => {
    let app = document.getElementById('app');
    app.style.height = 'auto';
    let body = document.getElementsByTagName('body')[0].style;
    body.backgroundColor = '#f2f5f7';
  });
</script>

<style lang="less" scoped>
  .login-box {
    display: block;
  }

  .login-subject {
    max-width: 420px;
    padding: 48px 48px 23px;
    box-sizing: border-box;
    margin: 80px auto 0;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 15px;
    position: relative;
  }

  .flex-row {
    display: flex;
    min-width: 0;
  }

  .align-items-center {
    align-items: center;
  }

  .margin-top40 {
    margin-top: 40px;
  }

  .login-title {
    text-align: left;
    font-size: 20px;
    color: #333333;
  }

  .webkit-flex {
    -webkit-flex: 1;
    flex: 1;
    -ms-flex: 1;
  }

  .content-box {
    margin-top: 5px;
  }

  .pointer {
    cursor: pointer;
  }

  .text-center {
    text-align: center;
  }

  .login-other {
    text-align: center;
    color: #bbb;
    margin: 15px auto;
  }

  .aui-third-login {
    width: 30px;
    height: 30px;
    margin: 0 auto;
    font-size: 24px;

    a {
      color: #888;
    }
  }

  .line {
    width: 100%;
    height: 0;
    border-top: 1px solid #f0f0f0;
    opacity: 1;
    display: block;
    margin-top: 20px;
  }

  .register-account {
    font-size: 14px;
    color: #2196f3;
    display: block;
    margin: 20px auto 0;
    text-align: center;
  }

  .login-class {
    color: 757575;
    margin-right: 20px;
  }

  :deep(.ant-spin-dot-item) {
    background: #757575;
  }

  .login-language {
    justify-content: center;
    align-items: center;
    display: flex;
    min-width: 0;
  }

  .language-drop {
    padding: 5px 5px 5px 12px;
    color: #757575;
  }

  .margin-top10{
    margin-top: 10px;
  }

  @media screen and (max-width: 414px) {
    .login-box {
      padding: 46px 16px;
    }

    .login-subject {
      margin: 0;
      padding: 48px 24px 23px;
    }
  }
</style>
