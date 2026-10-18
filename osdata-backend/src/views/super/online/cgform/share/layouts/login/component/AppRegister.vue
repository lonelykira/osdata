<template>
  <div class="register-box" v-show="showType==='register'">
    <div class="register-subject">
      <AppLoginHeader />
      <div class="flex-row align-items-center margin-top40">
        <div class="register-title">
          {{ t('sys.login.signUpFormTitle') }}
        </div>
      </div>
      <div class="register-content">
        <a-form ref="registerRef" :model="formData" :rules="getFormRules">
          <div class="content-item" :class="getPhoneClass" @click="formDataInputClick('mobile')">
            <a-form-item name="mobile">
              <a-input ref="phoneRef" v-model:value="formData.mobile" style="height: 40px" @blur="formDataInputBlur" />
              <div class="form-title" :class="activekey === 'mobile' ? 'active-title' : ''">
                {{ t('sys.login.mobile') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getSmsCodeClass">
            <a-form-item name="sms" @click="formDataInputClick('sms')">
              <a-input ref="smscodeRef" :maxLength="6" v-model:value="formData.sms" style="height: 40px" @blur="formDataInputBlur" />
              <div class="form-title" :class="activekey === 'sms' ? 'active-title' : ''">
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
          <div class="content-item" :class="getPasswordClass" @click="formDataInputClick('regPassword')">
            <a-form-item name="regPassword">
              <a-input ref="pwdRef" type="password" v-model:value="formData.regPassword" style="height: 40px" @blur="formDataInputBlur" autocomplete="new-password"/>
              <div class="form-title" :class="activekey === 'regPassword' ? 'active-title' : ''">
                8-20位，需包含字母和数字
              </div>
            </a-form-item>
          </div>
          <p class="register-rule">
            <a-form-item name="policy">
              <a-checkbox v-model:checked="formData.policy">{{ t('sys.login.policy') }}</a-checkbox>
            </a-form-item>
          </p>
          <div>
            <a-button type="primary" :loading="registerLoading" class="registr-btn pointer" @click="registerClick">{{ t('sys.login.nextStep') }}</a-button>
          </div>
          <div class="line" v-if="!props.bindThirdAccount"></div>
          <span v-if="!props.bindThirdAccount" class="to-login pointer" @click="toLoginClick">{{ t('sys.exception.backLogin') }}</span>
        </a-form>
      </div>
    </div>
  </div>
  <div v-show="showType === 'email'">
    <AppNameEmail ref="emailRef" @login-account="loginAccount" @bind-third-account="bindThirdAccount"/>
  </div>
  <!-- 图片验证码弹窗 -->
  <CaptchaModal @register="captchaRegisterModal" @ok="getLoginCode" />
</template>

<script lang="ts" setup name="app-register">
  import AppLoginHeader from './AppLoginHeader.vue';
  import { computed, reactive, ref, toRaw, unref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Rule } from '/@/components/Form';
  import {getCaptcha, phoneVerify} from '/@/api/sys/user';
  import { SmsEnum, useFormRules, useFormValid } from '/@/views/sys/login/useLogin';
  import AppNameEmail  from './AppNameEmail.vue';
  import CaptchaModal from '@/components/jeecg/captcha/CaptchaModal.vue';
  import { useModal } from "@/components/Modal";
  import { ExceptionEnum } from "@/enums/exceptionEnum";

  const { createMessage } = useMessage();
  const { t } = useI18n();
  //手机号表单
  const formData = reactive<any>({
    mobile: '',
    sms: '',
    regPassword: '',
    policy: true,
  });
  //input ref
  const phoneRef = ref();
  const pwdRef = ref();
  const smscodeRef = ref();
  const usernameRef = ref();
  //加载按钮
  const registerLoading = ref<boolean>(false);
  //选中值
  const activekey = ref<string>('');

  //获取手机号的样式
  const getPhoneClass = computed(() => {
    return formData.mobile != '' ? 'current-active' : '' || unref(activekey) === 'mobile' ? 'current-active' : '';
  });

  //获取用户名的样式
  const getUsernameClass = computed(() => {
    return formData.username != '' ? 'current-active' : '' || unref(activekey) === 'username' ? 'current-active' : '';
  });

  //获取手机号的样式
  const getSmsCodeClass = computed(() => {
    return formData.sms != '' ? 'current-active' : '' || unref(activekey) === 'sms' ? 'current-active' : '';
  });

  //获取密码的样式
  const getPasswordClass = computed(() => {
    return formData.regPassword != '' ? 'current-active' : '' || unref(activekey) === 'regPassword' ? 'current-active' : '';
  });
  const emit = defineEmits(['return-login', 'login-account', 'bind-third-account']);
  //是否显示获取验证码
  const showInterval = ref<boolean>(true);
  //60s
  const timeRuning = ref<number>(60);
  //定时器
  const timer = ref<any>(null);
  //注册表单
  const registerRef = ref();
  const getMobileFormRule = computed(() => createPhoneRule(t('sys.login.mobilePlaceholder')));
  const getSmsFormRule = computed(() => createRule(t('sys.login.smsPlaceholder')));
  const getPasswordFormRule = computed(() => [{ required: true, validator:checkPassword},{ pattern:/^(?=.*[0-9])(?=.*[a-zA-Z])(.{8,20})$/,message:'8-20位，需包含字母和数字'}]);
  //登录规则
  const getFormRules = computed(() => {
    return {
      mobile: unref(getMobileFormRule),
      sms: unref(getSmsFormRule),
      regPassword: unref(getPasswordFormRule),
    };
  });
  const { notification, createErrorModal } = useMessage();
  const loginCode = computed(() => t('component.countdown.normalText'));
  const loginDisabledCode = computed(() => t('component.countdown.sendText', [unref(timeRuning)]));
  const showType = ref<string>('register')
  const emailRef = ref();
  const props = defineProps({ 
      bindThirdAccount: { type: Boolean,default: false },
    });
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
   * 创建手机号规则
   * @param message
   */
  function createPhoneRule(message: string) {
    return [
      {
        required: true,
        message,
        trigger: 'change',
      },
      {
        pattern: /^1[3456789]\d{9}$/,
        message: t('sys.login.mobileCorrectPlaceholder')
      },
    ];
  }
  
  /**
   * 表单组件点击事件
   */
  function formDataInputClick(val) {
    activekey.value = val;
    if (val === 'mobile') {
      phoneRef.value.focus();
    } else if (val === 'sms') {
      smscodeRef.value.focus();
    } else if (val === 'username') {
      usernameRef.value.focus();
    } else {
      pwdRef.value.focus();
    }
  }

  /**
   * 表单组件失去焦点
   */
  function formDataInputBlur() {
    activekey.value = '';
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
    const result = await getCaptcha({ mobile: formData.mobile, smsmode: SmsEnum.REGISTER }).catch((res) =>{
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
   * 验证新密码是否为空
   */
  function checkPassword(_rule: Rule, value: string) {
    if (value === '') {
      return Promise.reject(t('sys.login.passwordPlaceholder'));
    }
    return Promise.resolve();
  }

  /**
   * 返回登录
   */
  function toLoginClick() {
    emit('return-login');
  }

  /**
   * 注册
   */
  async function registerClick() {
    registerRef.value.validateFields().then(async (values) => {
      registerLoading.value = true;
      await phoneVerify({phone:values.mobile,smscode:values.sms}).then((res) =>{
        if(res.success){
          if(res.result && res.result.username){
            notification.warning({
              message: t('sys.api.errorTip'),
              description: '手机号已注册' || t('sys.api.networkExceptionMsg'),
              duration: 3,
            });
          }
        }else{
          if(res.message === '用户信息不存在'){
            let params = { password: values.regPassword,phone: values.mobile,smscode: values.sms, bindThirdAccount: false, thirdUserUuid: '', thirdType: '' }
            showType.value = 'email';
            setTimeout(()=>{
              params.bindThirdAccount = props.bindThirdAccount
              emailRef.value.setRegisterData(params);
            },300)
          }else{
            notification.warning({
              message: t('sys.api.errorTip'),
              description: res.message || t('sys.api.networkExceptionMsg'),
              duration: 3,
            });
          }
        }
      }).finally(()=>{
        registerLoading.value = false;
      })
    });
  }

  /**
   * 清除表单验证
   */
  function clearValidate() {
    showType.value = 'register';
    Object.assign(formData, { mobile: '', sms: '', regPassword: '', policy: true});
    registerRef.value.clearValidate();
  }

  /**
   * 注册成功返回结果
   * @param values
   */
  function loginAccount(values){
    emit('login-account', values);
  }
  
  /**
   * 绑定第三方账号
   */
  function bindThirdAccount(values) {
    emit('bind-third-account',values)
  }

  defineExpose({
    clearValidate,
  });
</script>

<style lang="less" scoped>
  .register-box {
    display: block;
  }
  .register-subject {
    max-width: 420px;
    padding: 48px 48px 23px;
    box-sizing: border-box;
    background: #ffffff;
    border-radius: 4px;
    margin: 80px auto 15px;
    min-height: 578px;
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
  .registr-btn {
    font-weight: 600;
    width: 100%;
    height: 40px;
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
    margin-top: 48px;
  }
  .to-login {
    font-size: 14px;
    color: #2196f3;
    display: block;
    margin: 20px auto 0;
    text-align: center;
  }

  .disabled-btn {
    background-color: #ccc;
  }

  @media screen and (max-width: 414px) {
    .register-box {
      padding: 46px 16px;
    }
    .register-subject {
      margin: 0;
      padding: 48px 24px 23px;
    }
  }
</style>
