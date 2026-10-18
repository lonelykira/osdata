<template>
  <div style="margin: 50px auto; width: 800px">
    <BasicForm @register="registerForm" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import { pick } from 'lodash-es';

  const getSchemas = (): FormSchema[] => {
    return [
      {
        field: 'name',
        component: 'Input',
        label: '名称',
      },
      {
        field: 'age',
        component: 'InputNumber',
        label: '年龄',
        componentProps: {
          style: {
            width: '100%',
          },
        },
      },
      {
        field: 'sex',
        label: '性别',
        component: 'JDictSelectTag',
        componentProps: {
          dictCode: 'sex',
        },
      },
      {
        field: 'birthday',
        component: 'DatePicker',
        label: '生日',
        componentProps: {
          valueFormat: 'YYYY-MM-DD',
          style: {
            width: '100%',
          },
        },
      },
      {
        field: 'email',
        component: 'Input',
        label: '邮箱',
      },
    ];
  };

  export default defineComponent({
    components: { BasicForm },
    props: ['id'],
    setup(props) {
      //表单配置
      const [registerForm, { setFieldsValue }] = useForm({
        schemas: getSchemas(),
        showActionButtonGroup: false,
        baseColProps: { span: 24 },
      });
      let params = { id: props.id };
      defHttp.get({ url: '/test/jeecgDemo/queryById', params }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
          let values = pick(res.result, 'name', 'age', 'birthday', 'sex', 'email');
          setFieldsValue(values);
        }
      });

      return {
        registerForm,
      };
    },
  });
</script>
