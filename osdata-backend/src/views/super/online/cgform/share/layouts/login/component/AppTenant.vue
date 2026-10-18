<template>
  <div class="organ-box">
    <div class="organ-subject" :class="tenantType !== '' ? 'subject-margin' : ''">
      <AppLoginHeader :showLocalePicker="false" />
      <template v-if="organType === 'home'">
        <div class="flex-row align-items-center" style="margin-top: 20px">
          <div class="organ-title"> 创建或者加入组织 </div>
        </div>
        <div class="organ-desc align-items-center">
          <div class="content-box pointer" @click="joinOrganClick">
            <div class="organ-title-desc"> 加入<span style="color: rgb(141, 198, 216)">已有</span>组织 </div>
            <div> 如果被告知要使用，或有同事已经在用，请选择此项。 </div>
          </div>
          <div class="content-box pointer" style="margin-top: 35px" @click="createOrganClick">
            <div class="organ-title-desc"> 创建<span style="color: rgb(74, 79, 175)">新的</span>组织 </div>
            <div> 如果想为企业或者组织创建账号，请选择此项。 </div>
          </div>
        </div>
      </template>
      <template v-else-if="organType === 'join'">
        <div class="margin-top40 pointer" style="display: flex" @click="returnHome" v-if="tenantType === ''">
          <Icon icon="ant-design:arrow-left-outlined" style="color: #757575; margin-top: 2px" />
          <span style="margin-left: 10px">返回</span>
        </div>
        <div class="align-items-center" style="margin-top: 20px">
          <div class="organ-title"> 请填写组织门牌号 </div>
          <div class="organ-join-desc">
            <span style="color: #9e9e9e">组织门牌号可以通过管理员获取</span>
          </div>
          <a-form ref="joinRef" :model="formJoinState" :rules="getFormJoinRules">
            <div class="content-item" :class="getHouseNumberClass" @click="formStateInputClick('houseNumber')">
              <a-form-item name="houseNumber">
                <a-input ref="hoseNumberRef" v-model:value="formJoinState.houseNumber" style="height: 40px" @blur="formStateInputBlur" />
                <div class="form-title" :class="activekey === 'houseNumber' ? 'active-title' : ''"> 示例: J9A2K8R </div>
              </a-form-item>
            </div>
            <div class="tenant-number">
              <a href="http://help.qiaoqiaoyun.com/org/new.html" target="_blank">没有组织门牌号？</a>
            </div>
            <div class="create-btn pointer" @click="joinTenantClick"> 加入 </div>
          </a-form>
        </div>
      </template>
      <template v-else-if="organType === 'create'">
        <div class="margin-top40 pointer" style="display: flex" @click="returnHome" v-if="tenantType === ''">
          <Icon icon="ant-design:arrow-left-outlined" style="color: #757575; margin-top: 2px" />
          <span style="margin-left: 10px">返回</span>
        </div>
        <div class="align-items-center" style="margin-top: 20px">
          <div class="organ-title"> 创建组织 </div>
          <div class="organ-join-desc">
            <span style="color: #9e9e9e">您当前账号默认成为组织的管理员</span>
          </div>

          <a-form ref="createRef" :model="formCreateState" :rules="getFormCreateRules">
            <div class="content-item" :class="getNameClass" @click="formStateInputClick('name')">
              <a-form-item name="name">
                <a-input
                  ref="nameRef"
                  v-model:value="formCreateState.name"
                  style="height: 40px; font-size: 13px; color: rgba(0, 0, 0, 0.65)"
                  @blur="formStateInputBlur"
                />
                <div class="form-title" :class="activekey === 'name' ? 'active-title' : ''"> 组织名称 </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="trade">
                <a-select placeholder="请选择" style="height: 40px" :options="tradeOption" v-model:value="formCreateState.trade" />
                <div class="active-form-title"> 行业 </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="companySize">
                <a-select placeholder="请选择" style="height: 40px" :options="companySizeOption" v-model:value="formCreateState.companySize" />
                <div class="active-form-title"> 规模 </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="position">
                <a-select placeholder="请选择" style="height: 40px" :options="positionOption" v-model:value="formCreateState.position" />
                <div class="active-form-title"> 您的职级 </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="department">
                <a-select placeholder="请选择" style="height: 40px" :options="departmentOption" v-model:value="formCreateState.department" />
                <div class="active-form-title"> 您的部门 </div>
              </a-form-item>
            </div>
            <div class="create-btn pointer" @click="addOrganClick"> 创建 </div>
          </a-form>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup name="app-organization">
  import { computed, onMounted, reactive, ref, unref, watch } from 'vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import AppLoginHeader from './AppLoginHeader.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { saveTenantJoinUser, joinTenantByHouseNumber } from '../AppLogin.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { router } from '/@/router';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useUserStore } from '/@/store/modules/user';

  const props = defineProps({
    tenantType: { type: String, default: '' },
  });

  const { createMessage } = useMessage();
  //创建租户表单
  const createRef = ref();
  //加入租户表单
  const joinRef = ref();
  //租户类别：主页，加入，创建
  const organType = ref<string>('home');
  //租户加入
  const formJoinState = reactive<any>({
    houseNumber: '',
  });
  //租户创建
  const formCreateState = reactive<any>({
    name: '',
    trade: undefined,
    companySize: undefined,
    position: undefined,
    department: undefined,
  });
  //选中值
  const activekey = ref<string>('');
  //获取门牌号样式
  const getHouseNumberClass = computed(() => {
    return formJoinState.houseNumber != '' ? 'current-active' : '' || unref(activekey) === 'houseNumber' ? 'current-active' : '';
  });
  //获取租户名称样式
  const getNameClass = computed(() => {
    return formCreateState.name != '' ? 'current-active' : '' || unref(activekey) === 'name' ? 'current-active' : '';
  });
  const hoseNumberRef = ref();
  const nameRef = ref();
  //加入租户的表单验证
  const getFormJoinRules = {
    houseNumber: [{ required: true, message: '请输入组织门牌号' }],
  };
  const { t } = useI18n();
  const getNameRule = computed(() => createRule('请输入组织名称'));
  //创建租户的表单验证
  const getFormCreateRules = computed(() => {
    return {
      name: unref(getNameRule),
    };
  });
  //所属行业
  const tradeOption = ref<any>([
    { label: '信息传输、软件和信息技术服务业', value: '1' },
    { label: '制造业', value: '2' },
    { label: '租赁和商务服务业', value: '3' },
    { label: '教育', value: '4' },
    { label: '金融业', value: '5' },
    { label: '建筑业', value: '6' },
    { label: '科学研究和技术服务业', value: '7' },
    { label: '批发和零售业', value: '8' },
    { label: '住宿和餐饮业', value: '9' },
    { label: '电子商务', value: '10' },
    { label: '线下零售与服务业', value: '11' },
    { label: '文化、体育和娱乐业', value: '12' },
    { label: '房地产业', value: '13' },
    { label: '交通运输、仓储和邮政业', value: '14' },
    { label: '卫生和社会工作', value: '15' },
    { label: '公共管理、社会保障和社会组织', value: '16' },
    { label: '电力、热力、燃气及水生产和供应业', value: '17' },
    { label: '水利、环境和公共设施管理业', value: '18' },
    { label: '居民服务、修理和其他服务业', value: '19' },
    { label: '政府机构', value: '20' },
    { label: '农、林、牧、渔业', value: '21' },
    { label: '采矿业', value: '22' },
    { label: '国际组织', value: '23' },
    { label: '其他', value: '24' },
  ]);
  //公司规模
  const companySizeOption = ref<any>([
    { label: '20人以下', value: '1' },
    { label: '21-99人', value: '2' },
    { label: '100-499人', value: '3' },
    { label: '500-999人', value: '4' },
    { label: '1000-9999人', value: '5' },
    { label: '10000人以上', value: '6' },
  ]);
  //职位
  const positionOption = ref<any>([
    { label: '总裁/总经理/CEO', value: '1' },
    { label: '副总裁/副总经理/VP', value: '2' },
    { label: '总监/主管/经理', value: '3' },
    { label: '员工/专员/执行', value: '4' },
    { label: '其他', value: '5' },
  ]);
  //职位
  const departmentOption = ref<any>([
    { label: '总经办', value: '1' },
    { label: '技术/IT/研发', value: '2' },
    { label: '产品/设计', value: '3' },
    { label: '销售/市场/运营', value: '4' },
    { label: '人事/财务/行政', value: '5' },
    { label: '资源/仓储/采购', value: '6' },
    { label: '其他', value: '7' },
  ]);
  const emit = defineEmits(['success']);
  const userStore = useUserStore();

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
   * 加入租户
   */
  function joinOrganClick() {
    organType.value = 'join';
  }

  /**
   * 创建租户
   */
  function createOrganClick() {
    organType.value = 'create';
  }

  /**
   * 表单组件点击事件
   */
  function formStateInputClick(val) {
    activekey.value = val;
    if (val === 'houseNumber') {
      hoseNumberRef.value.focus();
    } else {
      nameRef.value.focus();
    }
  }

  /**
   * 清除选中值
   */
  function formStateInputBlur() {
    activekey.value = '';
  }

  /**
   * 创建租户
   */
  async function addOrganClick() {
    createRef.value.validateFields().then(async (values) => {
      values.status = '1';
      await saveTenantJoinUser(values).then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          userStore.setTenant(res.result);
          router.replace((userStore.getUserInfo && userStore.getUserInfo.homePath) || PageEnum.BASE_HOME);
          emit('success');
        } else {
          createMessage.warning(res.message);
        }
      });
    });
  }

  /**
   * 加入租户
   */
  function joinTenantClick() {
    joinRef.value.validateFields().then(async (values) => {
      await joinTenantByHouseNumber(values).then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          //userStore.setTenant(res.result);
          if (!props.tenantType) {
            router.replace((userStore.getUserInfo && userStore.getUserInfo.homePath) || PageEnum.BASE_HOME);
          }
          emit('success');
        } else {
          createMessage.warning(res.message);
        }
      }).catch((e)=>{
        createMessage.warning(e.message);
      });
    });
  }

  /**
   * 返回选择页面
   */
  function returnHome() {
    organType.value = 'home';
  }

  watch(
    () => props.tenantType,
    (val) => {
      if (val) {
        organType.value = val;
      }
    },
    { immediate: true }
  );
</script>

<style lang="less" scoped>
  .organ-box {
    display: block;
  }
  .organ-subject {
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

  .organ-title {
    text-align: left;
    font-size: 20px;
    color: #333333;
  }
  .organ-desc {
    margin-top: 20px;
  }
  .content-box {
    width: 100%;
    height: 150px;
    background: #ffffff;
    box-shadow: rgba(0, 0, 0, 0.24) 0 0 3px;
    padding: 40px 20px;
    border-radius: 6px;
  }
  .organ-title-desc {
    font-size: 18px;
    font-weight: 500;
  }
  .pointer {
    cursor: pointer;
  }
  .organ-join-desc {
    margin-top: 10px;
    align-items: center;
    font-size: 15px;
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
  .content-item {
    position: relative;
    margin-top: 30px;
    background: #ffffff;
    height: 40px;
    box-sizing: border-box;
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
  .content-item :deep(.ant-select-selector) {
    height: 40px;
  }
  .content-item :deep(.ant-select-single.ant-select-show-arrow .ant-select-selection-placeholder) {
    line-height: 40px !important;
  }
  .content-item :deep(.ant-select-single.ant-select-show-arrow .ant-select-selection-item) {
    line-height: 40px !important;
  }
  .create-btn {
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

  :deep(.ant-input-number-input) {
    font-size: 14px;
  }

  .subject-margin {
    margin: 15px auto 15px !important;
  }

  .tenant-number {
    align-items: center;
    color: #1e88e5;
    margin-top: 16px;
    vertical-align: middle;
  }

  @media screen and (max-width: 414px) {
    .organ-box {
      padding: 46px 16px;
    }
    .organ-subject {
      margin: 0;
      padding: 48px 24px 48px;
    }
  }
</style>
