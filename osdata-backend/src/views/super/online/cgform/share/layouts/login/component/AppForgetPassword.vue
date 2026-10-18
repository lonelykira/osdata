<template>
  <div class="forget-pwd-box">
    <div class="forget-pwd-subject">
      <AppLoginHeader />
      <div class="flex-row align-items-center margin-top40">
        <div class="register-title">
          {{ t('sys.login.forgetFormTitle') }}
        </div>
      </div>
      <div class="register-content">
        <a-form ref="phoneUpdateRef" :model="formState" :rules="getFormRules" v-show="activeUpdateKey === 'vailPhone'">
          <div class="content-item" :class="getPhoneClass" @click="formStateInputClick('phone')">
            <a-form-item name="phone">
              <a-input ref="phoneRef" v-model:value="formState.phone" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'phone' ? 'active-title' : ''">
                {{ t('sys.login.mobile') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getSmsCodeClass">
            <a-form-item name="smscode" @click="formStateInputClick('smscode')">
              <a-input ref="smscodeRef" :maxLength="6" v-model:value="formState.smscode" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'smscode' ? 'active-title' : ''">
                {{ t('sys.login.smsCode') }}
              </div>
            </a-form-item>
            <a-input
              type="button"
              v-if="showInterval"
              class="aui-code-line pointer"
              :bordered="false"
              @click="getLoginCode"
              v-model:value="loginCode"
            />
            <a-input type="button" v-else class="aui-code-line disabled-btn" :bordered="false" v-model:value="loginDisabledCode" />
          </div>
          <div class="forget-btn pointer" @click="forgetPwdClick">
            <span>{{ t('sys.login.nextStep') }}</span>
          </div>
          <div class="line"></div>
        </a-form>
        <a-form ref="pwdUpdateRef" :model="accountData" :rules="getFormPwdRules" v-show="activeUpdateKey === 'vailPwd'">
          <div class="content-item" :class="getPhoneClass">
            <a-form-item name="username">
              <a-input ref="phoneRef" v-model:value="accountData.username" style="height: 40px" disabled />
              <div class="active-form-title">
                {{ t('sys.login.userName') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getPasswordClass" @click="formStateInputClick('forgetPassword')">
            <a-form-item name="forgetPassword">
              <a-input ref="pwdRef" type="password" v-model:value="accountData.forgetPassword" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'password' ? 'active-title' : ''">
                {{ t('sys.login.password') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getConfirmPasswordClass" @click="formStateInputClick('confirmPassword')">
            <a-form-item name="confirmPassword">
              <a-input ref="conPwdRef" type="password" v-model:value="accountData.confirmPassword" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'password' ? 'active-title' : ''">
                {{ t('sys.login.confirmPassword') }}
              </div>
            </a-form-item>
          </div>
          <div class="forget-btn pointer" @click="updatePwdClick">
            <span>完成</span>
          </div>
          <div class="line"></div>
        </a-form>
        <span class="to-login pointer" @click="toLoginClick">{{ t('sys.exception.backLogin') }}</span>
      </div>
    </div>
  </div>
  <!-- 图片验证码弹窗 -->
  <CaptchaModal @register="captchaRegisterModal" @ok="getLoginCode" />
</template>

<script lang="ts" setup name="app-forget-password">
  import AppLoginHeader from './AppLoginHeader.vue';
  import { computed, reactive, ref, toRaw, unref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Rule } from '/@/components/Form';
  import { getCaptcha, passwordChange, phoneVerify } from '/@/api/sys/user';
  import { SmsEnum } from '/@/views/sys/login/useLogin';
  import { RuleObject } from 'ant-design-vue/lib/form/interface';

  import CaptchaModal from '@/components/jeecg/captcha/CaptchaModal.vue';
  import { useModal } from "@/components/Modal";
  import { ExceptionEnum } from "@/enums/exceptionEnum";
  const [captchaRegisterModal, { openModal: openCaptchaModal }] = useModal();
  
  const { createMessage } = useMessage();
  const { t } = useI18n();
  //手机号表单
  const formState = reactive<any>({
    phone: '',
    smscode: '',
    forgetPassword: '',
    policy: true,
  });
  //input ref
  const phoneRef = ref();
  const pwdRef = ref();
  const smscodeRef = ref();
  const pwdUpdateRef = ref();
  const conPwdRef = ref();
  //修改密码值
  const activeUpdateKey = ref<string>('vailPhone');
  //选中值
  const activekey = ref<string>('');

  const getMobileFormRule = computed(() => createRule(t('sys.login.mobilePlaceholder')));
  const getSmsFormRule = computed(() => createRule(t('sys.login.smsPlaceholder')));
  const getPasswordFormRule = computed(() => createRule(t('sys.login.passwordPlaceholder')));
  //忘记密码规则
  const getFormRules = computed(() => {
    return {
      phone: unref(getMobileFormRule),
      smscode: unref(getSmsFormRule),
    };
  });

  const getFormPwdRules = computed(() => {
    return {
      forgetPassword: unref(getPasswordFormRule),
      confirmPassword: [{ validator: validateConfirmPassword(unref(accountData).forgetPassword), trigger: 'change' }],
    };
  });

  const { notification, createErrorModal } = useMessage();
  const loginCode = computed(() => t('component.countdown.normalText'));
  const loginDisabledCode = computed(() => t('component.countdown.sendText', [unref(timeRuning)]));

  /**
   * 验证二次密码
   * @param password
   */
  const validateConfirmPassword = (password: string) => {
    return async (_: RuleObject, value: string) => {
      if (!value) {
        return Promise.reject(t('sys.login.passwordPlaceholder'));
      }
      if (value !== password) {
        return Promise.reject(t('sys.login.diffPwd'));
      }
      return Promise.resolve();
    };
  };

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

  //获取手机号的样式
  const getPhoneClass = computed(() => {
    return formState.phone != '' ? 'current-active' : '' || unref(activekey) === 'phone' ? 'current-active' : '';
  });

  //获取手机号的样式
  const getSmsCodeClass = computed(() => {
    return formState.smscode != '' ? 'current-active' : '' || unref(activekey) === 'smscode' ? 'current-active' : '';
  });

  //获取密码的样式
  const getPasswordClass = computed(() => {
    return unref(accountData).forgetPassword != '' ? 'current-active' : '' || unref(activekey) === 'forgetPassword' ? 'current-active' : '';
  });
  //获取确认密码的样式
  const getConfirmPasswordClass = computed(() => {
    return unref(accountData).confirmPassword != '' ? 'current-active' : '' || unref(activekey) === 'confirmPassword' ? 'current-active' : '';
  });

  const emit = defineEmits(['return-login', 'login-account']);
  //是否显示获取验证码
  const showInterval = ref<boolean>(true);
  //60s
  const timeRuning = ref<number>(60);
  //定时器
  const timer = ref<any>(null);
  //验证手机号表单
  const phoneUpdateRef = ref();
  //用户数据
  const accountData = ref<any>([]);

  /**
   * 表单组件点击事件
   */
  function formStateInputClick(val) {
    activekey.value = val;
    if (val === 'phone') {
      phoneRef.value.focus();
    } else if (val === 'smscode') {
      smscodeRef.value.focus();
    } else if (val === 'confirmPassword') {
      conPwdRef.value.focus();
    } else {
      pwdRef.value.focus();
    }
  }

  /**
   * 表单组件失去焦点
   */
  function formStateInputBlur() {
    activekey.value = '';
  }

  /**
   * 获取验证码
   */
  async function getLoginCode() {
    if (!formState.phone) {
      createMessage.warn(t('sys.login.mobilePlaceholder'));
      return;
    }
    //update-begin---author:wangshuai ---date:20230620  for：【QQYUN-5590】忘记密码不好使------------
    //update-begin---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】同一个IP，1分钟超过5次短信，则提示需要验证码---
    const result = await getCaptcha({ mobile: formState.phone, smsmode: SmsEnum.FORGET_PASSWORD }).catch((res) =>{
      if(res.code === ExceptionEnum.PHONE_SMS_FAIL_CODE){
        openCaptchaModal(true, {});
      }
    });
    //update-end---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】同一个IP，1分钟超过5次短信，则提示需要验证码---
    //update-end---author:wangshuai ---date:20230620  for：【QQYUN-5590】忘记密码不好使------------
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
   * 忘记密码
   */
  function forgetPwdClick() {
    phoneUpdateRef.value.validateFields().then(async (values) => {
      const resultInfo = await phoneVerify(
        toRaw({
          phone: values.phone,
          smscode: values.smscode,
        })
      );
      if (resultInfo.success) {
        console.log(resultInfo);
        let accountInfo = {
          username: resultInfo.result.username,
          phone: values.phone,
          smscode: resultInfo.result.smscode,
        };
        accountData.value = accountInfo;
        activeUpdateKey.value = 'vailPwd';
      } else {
        notification.error({
          message: t('sys.api.errorTip'),
          description: resultInfo.message || t('sys.api.networkExceptionMsg'),
          duration: 3,
        });
      }
    });
  }

  /**
   * 完成
   */
  async function updatePwdClick() {
    pwdUpdateRef.value.validateFields().then(async (values) => {
      const resultInfo = await passwordChange(
        toRaw({
          username: values.username,
          password: values.forgetPassword,
          smscode: unref(accountData).smscode,
          phone: unref(accountData).phone,
        })
      );
      if (resultInfo.success) {
        //修改密码
        emit('login-account', { account: values.username, password: values.forgetPassword });
        phoneUpdateRef.value.resetFields();
        accountData.value = {};
        activeUpdateKey.value = 'vailPhone';
      } else {
        //错误提示
        createErrorModal({
          title: t('sys.api.errorTip'),
          content: resultInfo.message || t('sys.api.networkExceptionMsg'),
        });
      }
    });
  }

  /**
   * 返回登录
   */
  function toLoginClick() {
    emit('return-login');
  }
</script>

<style lang="less" scoped>
  .forget-pwd-box {
    display: block;
  }

  .forget-pwd-subject {
    max-width: 420px;
    padding: 48px 48px 23px;
    box-sizing: border-box;
    background: #ffffff;
    border-radius: 4px;
    margin: 80px auto 15px;
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

  .register-title {
    text-align: left;
    font-size: 20px;
    color: #333333;
  }

  .register-content {
    margin-top: 5px;
  }

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

  .register-rule {
    margin-top: 13px;
    font-size: 14px;
    padding: 0;
    line-height: 1.5;
    color: #757575;
  }

  .forget-btn {
    font-weight: 600;
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: block;
    background: #2296f3;
    border-radius: 4px;
    font-size: 14px;
    color: #fff;
    margin-top: 32px;
    text-align: center;
  }

  .pointer {
    cursor: pointer;
  }

  .line {
    width: 100%;
    height: 0;
    border-top: 1px solid #f0f0f0;
    opacity: 1;
    display: block;
    margin-top: 84px;
  }

  .to-login {
    font-size: 14px;
    color: #2196f3;
    display: block;
    margin: 20px auto 0;
    text-align: center;
  }

  .active-form-title {
    position: absolute;
    left: 12px;
    top: -9px;
    color: #757575;
    font-size: 12px;
    z-index: 0;
    line-height: 1;
    background: #fff;
  }

  .disabled-btn {
    background-color: #ccc;
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
