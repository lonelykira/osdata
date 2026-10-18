<template>
  <BasicModal wrapClassName="link-table-config-modal" v-bind="$attrs" title="关联记录配置" @register="registerModal" keyboard :canFullscreen="false" cancelText="关闭" @ok="handleSubmit">
    <a-spin :spinning="spinningLoading">
        <BasicForm @register="registerForm">
        </BasicForm>
    </a-spin>
  </BasicModal>
</template>

<script>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ref,computed,nextTick} from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import {  omit } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default {
    name: 'LinkTableConfigModal',
    emits: ['success', 'register'],
    components: {
      BasicModal,
      BasicForm
    },
    setup(_p, { emit }) {
      const spinningLoading = ref(false);
      const { createMessage: $message } = useMessage();
      // 是否是外键的关联记录配置
      //const fkStatus = ref(false);
      const fieldName = ref('')
      // update-begin--author:liaozhiyang---date:20260312---for:【QQYUN-9441】online一对多加上关联记录和他表字段
      const isSubTableOneToMany = ref(false);
      // update-end--author:liaozhiyang---date:20260312---for:【QQYUN-9441】online一对多加上关联记录和他表字段
      let oldValue = {}
      //useModalInner
      const [registerModal,{closeModal}] = useModalInner(async (data) => {
        //fkStatus.value = data.isFk || false;
        console.log('data', data);
        //spinningLoading.value = true;
        isSubTableOneToMany.value = data.isSubTableOneToMany || false;
        oldValue = {...data.record};
        await setFieldsValue({dictTable: data.record.dictTable});
        // 字段设置值在表名之后 防止表名的change将字段的值置空
        setTimeout(async ()=>{
          let otherValue = omit(data.record, 'dictTable')
          await setFieldsValue(otherValue);
          await clearValidate();
        }, 200);
        fieldName.value = data.fieldName;
      });
      
      const titleFieldName = ref('');
      const imageFieldName = ref('')
      const fieldOptions = ref([]);
      const imageFieldOptions = ref([])
      async function getFieldOptions(tableName){
        if(tableName){
          const url = '/online/cgform/field/listByHeadCode';
          const dataList = await defHttp.get({ url, params:{headCode: tableName} });
          if(dataList && dataList.length>0){
            let arr1 = dataList.filter(item=>item.dbFieldName!='id' && item.dbIsPersist == 1 && item.isShowList == 1);
            console.log('字段字典', arr1)
            if(arr1.length>0){
              fieldOptions.value = arr1.map(item=>{
                return {
                  text: item.dbFieldTxt,
                  value: item.dbFieldName
                }
              });
            }else{
              fieldOptions.value = []
            }
            
            let arr2 = dataList.filter(item=>item.dbFieldName!='id' && item.fieldShowType == 'image' && item.dbIsPersist == 1);
            console.log('图片字段', arr2)
            if(arr2.length>0){
              imageFieldOptions.value = arr2.map(item=>{
                return {
                  text: item.dbFieldTxt,
                  value: item.dbFieldName
                }
              });
            }else{
              imageFieldOptions.value = [{text:'无图片字段可以选择', value:'',key:'', disabled: true}]
            }
          }else{
            fieldOptions.value = []
            imageFieldOptions.value = [{text:'无图片字段可以选择', value:'', key:'',disabled: true}]
          }
        }
      }
      async function handleTableChange(tableName){
        titleFieldName.value = '';
        imageFieldName.value = ''
        // 表名修改重新获取字段
        await getFieldOptions(tableName);
      }
      const otherFieldOptions = computed(()=>{
        let arr = fieldOptions.value;
        let titleField = titleFieldName.value;
        let imageField = imageFieldName.value;
        return arr.filter(item=>item.value!=titleField && item.value!=imageField);
      });
      
      
      const formSchemas = [
        {
          label: 'rowKey',
          field: 'rowKey',
          component: 'Input',
          show: false
        },
        {
          label: 'dictField',
          field: 'dictField',
          component: 'Input',
          defaultValue: 'id',
          show: false
        },
        {
          label: '字段描述',
          field: 'dbFieldTxt',
          component: 'Input',
          required: true
        },
        {
          label: '关联表',
          field: 'dictTable',
          component: 'JSearchSelect',
          required: true,
          componentProps:({ formActionType }) => {
            return {
              dict: 'onl_cgform_head where copy_type = 0,table_txt,table_name',
              pageSize: 10,
              async: true,
              immediateChange: true,
              popContainer: '.link-table-config-modal',
              params:{order: 'desc', column: 'create_time'},
              onChange:async (name)=>{
                // 重新设置表单值
                if(oldValue.titleField || oldValue.otherFields){
                  await formActionType.setFieldsValue({
                    titleField: '',
                    otherFields: '',
                    imageField: ''
                  });
                  await formActionType.clearValidate();
                }
                // 通过表名查询字段
                await handleTableChange(name);
              }
            }
          }
        },
        {
          label: '标题字段',
          field: 'titleField',
          component: 'JSearchSelect',
          required: true,
          componentProps:{
            async: false,
            popContainer: '.link-table-config-modal',
            dictOptions: fieldOptions,
            immediateChange: true,
            onChange:(str)=>{
              titleFieldName.value = str;
              oldValue['titleField'] = str;
            }
          }
        },
        {
          label: '封面图片',
          field: 'imageField',
          component: 'JSearchSelect',
          ifShow: () => !isSubTableOneToMany.value,
          componentProps:{
            async: false,
            popContainer: '.link-table-config-modal',
            dictOptions: imageFieldOptions,
            immediateChange: true,
            onChange:(str)=>{
              imageFieldName.value = str;
              oldValue['imageFieldName'] = str;
            }
          }
        },
        {
          label: '其他字段',
          field: 'otherFields',
          component: 'JSelectMultiple',
          ifShow: () => !isSubTableOneToMany.value,
          // update-begin--author:liaozhiyang---date:20240320---for：【QQYUN-8574】关联记录最多选6个字段
          componentProps: ({ schema, tableAction, formActionType, formModel }) => {
            return {
              popContainer: '.link-table-config-modal',
              options: otherFieldOptions.value,
              onChange: (str) => {
                if (str.split(',').length > 6) {
                  const result = str.split(',');
                  result.pop();
                  const eStr = result.join(',');
                  setTimeout(() => {
                    formModel.otherFields = eStr;
                    oldValue['otherFields'] = eStr;
                  }, 0);
                  $message.warning('最多选择6个字段~');
                } else {
                  oldValue['otherFields'] = str;
                }
                console.info('oldValue', formModel);
              },
            };
          },
          // update-end--author:liaozhiyang---date:20240320---for：【QQYUN-8574】关联记录最多选6个字段
        },
        
        {
          label: '显示方式',
          field: 'showType',
          component: 'Select',
          defaultValue: 'card',
          ifShow: () => !isSubTableOneToMany.value,
          componentProps: {
            options: [
              { label: '卡片', value: 'card' },
              { label: '下拉框', value: 'select'},
            ],
          }
        },
        {
          label: '是否多选',
          field: 'multiSelect',
          component: 'RadioGroup',
          defaultValue: false,
          componentProps: {
            options: [
              { label: '否', value: false },
              { label: '是', value: true },
            ],
          }
        },
        {
          label: '列表只读',
          field: 'isListReadOnly',
          component: 'RadioGroup',
          defaultValue: false,
          ifShow: () => !isSubTableOneToMany.value,
          componentProps: {
            options: [
              { label: '否', value: false },
              { label: '是', value: true },
            ],
          },
        }
      ]

      // 表单配置
      const [ registerForm, {validate, setFieldsValue, clearValidate, resetFields }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      async function handleSubmit(){
        let data = await validate()
        data['fieldName'] = fieldName.value;
 /*     dbFieldTxt: "姓名"
        dictTable: "test"
        multiSelect: false
        otherFields: "create_by,xxx"
        showType: "card",
        imageField: 'imagexx',
        titleField: "namexx"*/
        emit('success', data);
        closeModal();
      }
      
      
      return {
        registerModal,
        spinningLoading,
        registerForm,
        handleSubmit
      };
    },
  };
</script>

<style scoped></style>
