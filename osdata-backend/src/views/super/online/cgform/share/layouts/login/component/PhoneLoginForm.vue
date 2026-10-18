<template>
  <a-form ref="loginPhoneRef" :model="formData" :rules="getFormRules" @keyup.enter.native="phoneLoginClick">
    <div class="content-item" :class="geMobileClass" @click="formDataInputClick('mobile')">
      <a-form-item name="mobile">
        <a-input ref="mobileRef" v-model:value="formData.mobile" style="height: 40px" @blur="formDataInputBlur" />
        <div class="form-title" :class="activekey === 'mobile' ? 'active-title' : ''">
          {{ t('sys.login.mobile') }}
        </div>
      </a-form-item>
    </div>
    <div class="content-item" :class="getSmsCodeClass">
      <a-form-item name="sms" @click="formDataInputClick('sms')">
        <a-input ref="smsCodeRef" :maxLength="6" v-model:value="formData.sms" style="height: 40px" @blur="formDataInputBlur" />
        <div class="form-title" :class="activekey === 'sms' ? 'active-title' : ''">
          {{ t('sys.login.smsCode') }}
        </div>
      </a-form-item>
      <a-input type="button" v-if="showInterval" class="aui-code-line pointer" :bordered="false" @click="getLoginCode" v-model:value="loginCode" />
      <a-input type="button" v-else class="aui-code-line disabled-btn" :bordered="false" v-model:value="loginDisabledCode" />
    </div>
    <div>
      <a-button type="primary" @click="phoneLoginClick" :loading="loginLoading" class="login-btn">{{ t('sys.login.loginButton') }}</a-button>
    </div>
    <div v-if="!props.bindThirdAccount" class="phone-login-btn pointer" @click="accountHandleLogin">{{ t('sys.login.backSignIn') }}</div>
  </a-form>
  <!-- 图片验证码弹窗 -->
  <CaptchaModal @register="captchaRegisterModal" @ok="getLoginCode" />
</template>

<script lang="ts" setup name="phone-login-form">
  //获取手机号的样式
  import { computed, onMounted, reactive, ref, toRaw, unref } from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { SmsEnum, useFormRules, useFormValid } from '/@/views/sys/login/useLogin';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getCaptcha, getCodeInfo } from '/@/api/sys/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import CaptchaModal from '@/components/jeecg/captcha/CaptchaModal.vue';
  import { useModal } from "@/components/Modal";
  import { ExceptionEnum } from "@/enums/exceptionEnum";

  const { createMessage } = useMessage();

  const { t } = useI18n();
  //选中值
  const activekey = ref<string>('');
  //登录加载
  const loginLoading = ref<boolean>(false);
  //登录表单
  const loginPhoneRef = ref();
  const randCodeData = reactive({
    randCodeImage: '',
    requestCodeSuccess: false,
    checkKey: -1,
  });
  //手机号表单
  const formData = reactive({
    mobile: '',
    sms: '',
  });
  const userStore = useUserStore();
  const emit = defineEmits(['login', 'login-success','bind-third-phone']);
  //记住我
  const rememberMeCheck = ref<boolean>(false);
  //获取手机号的样式
  const geMobileClass = computed(() => {
    return formData.mobile != '' ? 'current-active' : '' || unref(activekey) === 'mobile' ? 'current-active' : '';
  });
  //获取手机验证码的样式
  const getSmsCodeClass = computed(() => {
    return formData.sms != '' ? 'current-active' : '' || unref(activekey) === 'sms' ? 'current-active' : '';
  });
  const { notification, createErrorModal } = useMessage();
  //是否显示获取验证码
  const showInterval = ref<boolean>(true);
  //60s
  const timeRuning = ref<number>(60);
  //定时器
  const timer = ref<any>(null);
  const smsCodeRef = ref();
  const mobileRef = ref();
  const getAccountFormRule = computed(() => createRule(t('sys.login.accountPlaceholder')));
  const getSmsFormRule = computed(() => createRule(t('sys.login.smsPlaceholder')));
  //登录规则
  const getFormRules = computed(() => {
    return {
      mobile: unref(getAccountFormRule),
      sms: unref(getSmsFormRule),
    };
  });
  const loginCode = computed(() => t('component.countdown.normalText'));
  const loginDisabledCode = computed(() => t('component.countdown.sendText', [unref(timeRuning)]));
  const props = defineProps({ bindThirdAccount: { type: Boolean, default: false } });
  const [captchaRegisterModal, { openModal: openCaptchaModal }] = useModal();

  /**
   * 创建规则
   * @param message
   */
  function createRule(message: string) {
    return [
      {
        required: true,
        message,
        trigger: 'change',
      },
    ];
  }

  /**
   * 表单组件点击事件
   */
  function formDataInputClick(val) {
    activekey.value = val;
    if (val === 'sms') {
      smsCodeRef.value.focus();
    } else {
      mobileRef.value.focus();
    }
  }

  /**
   * 表单组件失去焦点
   */
  function formDataInputBlur() {
    activekey.value = '';
  }

  /**
   * 登录点击事件
   */
  async function phoneLoginClick() {
    loginPhoneRef.value.validateFields().then(async (values) => {
      //当前是否为第三方绑定手机号,如果是那么就走绑定手机号的逻辑，否则直接登录
      if(!props.bindThirdAccount){
        try {
          loginLoading.value = true;
          const { userInfo } = await userStore.phoneLogin(
            toRaw({
              mobile: values.mobile,
              captcha: values.sms,
              mode: 'none', //不要默认的错误提示
              goHome: false,
            })
          );
          if (userInfo) {
            emit('login-success', userInfo.realname);
          }
        } catch (error) {
          notification.error({
            message: t('sys.api.errorTip'),
            description: error.message || t('sys.api.networkExceptionMsg'),
            duration: 3,
          });
        } finally {
          loginLoading.value = false;
        }
      }else{
        emit('bind-third-phone',values)
      }
    });
  }

  /**
   * 获取验证码
   */
  async function getLoginCode() {
    if (!formData.mobile) {
      createMessage.warn(t('sys.login.mobilePlaceholder'));
      return;
    }
    //update-begin---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】同一个IP，1分钟超过5次短信，则提示需要验证码---
    //update-begin---author:wangshuai---date:2025-07-15---for:【issues/8567】严重：修改密码存在水平越权问题：登录应该用登录模板不应该用忘记密码的模板---
    const result = await getCaptcha({ mobile: formData.mobile, smsmode: SmsEnum.LOGIN }).catch((res) =>{
    //update-end---author:wangshuai---date:2025-07-15---for:【issues/8567】严重：修改密码存在水平越权问题：登录应该用登录模板不应该用忘记密码的模板---
      if(res.code === ExceptionEnum.PHONE_SMS_FAIL_CODE){
        openCaptchaModal(true, {});
      }
    });
    //update-end---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】同一个IP，1分钟超过5次短信，则提示需要验证码---
    if (result) {
      const TIME_COUNT = 60;
      if (!unref(timer)) {
        timeRuning.value = TIME_COUNT;
        showInterval.value = false;
        timer.value = setInterval(() => {
          if (unref(timeRuning) > 0 && unref(timeRuning) <= TIME_COUNT) {
            timeRuning.value = timeRuning.value - 1;
          } else {
            showInterval.value = true;
            clearInterval(unref(timer));
            timer.value = null;
          }
        }, 1000);
      }
    }
  }

  /**
   * 手机号登录
   */
  function accountHandleLogin() {
    emit('login', 'accountLogin');
  }
</script>

<style lang="less" scoped>
  .content-item {
    position: relative;
    margin-top: 30px;
    background: #ffffff;
    height: 40px;
    box-sizing: border-box;
  }
  .form-title {
    position: absolute;
    left: 12px;
    top: 13px;
    color: #9e9e9e;
    font-size: 14px;
    z-index: 0;
    line-height: 1;
    transition: all 0.3s;
  }
  .current-active {
    .form-title {
      font-size: 12px;
      color: #757575;
      top: -9px;
      z-index: 1;
      background: #fff;
      padding: 0 4px;
    }
    .active-title {
      color: #2196f3;
    }
  }

  .pointer {
    cursor: pointer;
  }

  .login-btn {
    width: 100%;
    height: 40px;
    display: block;
    background: #2296f3;
    border-radius: 4px;
    font-size: 14px;
    color: #fff;
    margin-top: 40px;
    text-align: center;
  }
  .phone-login-btn {
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: block;
    background: #ffffff;
    border-radius: 4px;
    font-size: 14px;
    color: #333333;
    margin-top: 20px;
    text-align: center;
    border: 1px solid #d9d9d9;
  }

  .aui-code-line {
    background-color: #42a5f5;
    display: inline-block;
    width: 110px;
    height: 32px;
    border-radius: 4px;
    position: absolute;
    right: 4px;
    top: 4px;
    text-align: center;
    font-size: 13px;
    color: #fff;
  }

  .disabled-btn {
    background-color: #ccc;
  }
</style>
