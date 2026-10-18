<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="title"
    width="600px"
    destroyOnClose
    @ok="handleSubmit"
    :showFooter="showFooter"
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { authFormSchema } from '../OpenApiAuth.data';
  import { saveOrUpdate, getGenAKSK } from '../OpenApiAuth.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { USER_INFO_KEY } from '/@/enums/cacheEnum';
  import { getAuthCache } from '/@/utils/auth';

  const emit = defineEmits(['register', 'success']);
  const { createMessage } = useMessage();
  const isUpdate = ref(false);
  const formDisabled = ref(false);
  const showFooter = ref(true);

  const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
    labelWidth: 100,
    schemas: authFormSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    showFooter.value = !!data?.showFooter;
    setDrawerProps({ confirmLoading: false, showFooter: showFooter.value });
    isUpdate.value = !!data?.isUpdate;
    formDisabled.value = !data?.showFooter;

    if (unref(isUpdate)) {
      await setFieldsValue({ ...data.record });
    } else {
      // New record: set current user and auto-generate AK/SK
      const userData = getAuthCache(USER_INFO_KEY) as any;
      const akskArr = await getGenAKSK({});
      await setFieldsValue({
        systemUserId: userData?.id || '',
        ak: akskArr[0],
        sk: akskArr[1],
      });
    }
    setProps({ disabled: !data?.showFooter });
  });

  const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(formDisabled) ? '编辑' : '详情'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });
      const res = await saveOrUpdate(values, isUpdate.value);
      if (res.success) {
        createMessage.success(res.message);
        closeDrawer();
        emit('success');
      } else {
        createMessage.warning(res.message);
      }
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
