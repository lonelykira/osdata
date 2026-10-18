<template>
  <BasicModal wrapClassName="link-table-field-config-modal" v-bind="$attrs" title="他表字段配置" @register="registerModal" keyboard :canFullscreen="false" cancelText="关闭" @ok="handleSubmit">
    <a-spin :spinning="spinningLoading">
        <BasicForm @register="registerForm">
        </BasicForm>
    </a-spin>
  </BasicModal>
</template>

<script>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ref,computed } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import {  omit } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';
  
  export default {
    name: 'LinkTableFieldConfigModal',
    emits: ['success', 'register'],
    props: {
      isSubTableOneToMany: {
        type: Boolean,
        default: false
      }
    },
    components: {
      BasicModal,
      BasicForm
    },
    setup(props, { emit }) {
      const spinningLoading = ref(false);
      const linkTableOptions = ref([]);
      const fieldOptions = ref([]);
      let tableAndFieldsMap = {};
      let oldValue = {}
      const { createMessage: $message } = useMessage();
      
      //useModalInner
      const [registerModal,{closeModal}] = useModalInner( async (data) => {
        console.log('data', data);
        oldValue = {...data.record};
        //spinningLoading.value = true;
        tableAndFieldsMap = data.tableAndFieldsMap;
        await initLinkTableOptions()
        await setFieldsValue({dictTable: data.record.dictTable});
        // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-45】更关联记录中的其他字段，他表字段中显示字段没有更新
        if (oldValue.dictTable) {
          handleChange(oldValue.dictTable);
        }
        // update-end--author:liaozhiyang---date:20240517---for：【TV360X-45】更关联记录中的其他字段，他表字段中显示字段没有更新
        setTimeout(async ()=>{
          let otherValue = omit(data.record, 'dictTable')
          await setFieldsValue(otherValue);
          await clearValidate();
        }, 200);
      });
      
      async function initLinkTableOptions(){
        let fieldNames = Object.keys(tableAndFieldsMap);
        if(!fieldNames || fieldNames.length==0){
          linkTableOptions.value = []
        }else{
          let arr = []
          for(let name of fieldNames){
            arr.push({
              text: tableAndFieldsMap[name].title,
              value: name
            })
          }
          linkTableOptions.value = arr
        }
      }
      
      async function handleChange(fieldName){
        if(fieldName){
          const {table, fields} = tableAndFieldsMap[fieldName];
          if(!table){
            $message.warning('请先完善字段['+fieldName+']关联记录的配置')
            return;
          }
          const url = '/online/cgform/field/listByHeadCode';
          const dataList = await defHttp.get({ url, params:{headCode: table} });
          if(dataList && dataList.length>0){
            let arr = dataList.map(item=>{
              return {
                text: item.dbFieldTxt,
                value: item.dbFieldName
              }
            });
            console.log('字段字典', arr);
            let fieldArray = fields.split(',')
            // update-begin--author:liaozhiyang---date:20260323---for:【QQYUN-14951】一对多他表字段需能选择所有字段
            if (props.isSubTableOneToMany) {
              fieldOptions.value = arr;
            } else {
              fieldOptions.value = arr.filter(item=>fieldArray.includes(item.value));
            }
            // update-end--author:liaozhiyang---date:20260323---for:【QQYUN-14951】一对多他表字段需能选择所有字段
          }else{
            fieldOptions.value = []
          }
        }
      }
      
      const formSchemas = [
        {
          label: 'rowKey',
          field: 'rowKey',
          component: 'Input',
          show: false
        },
        {
          label: '字段描述',
          field: 'dbFieldTxt',
          component: 'Input',
          required: true
        },
        {
          label: '关联记录',
          field: 'dictTable',
          component: 'JSearchSelect',
          required: true,
          componentProps:({ formActionType }) => {
            let tempOptions = linkTableOptions.value;
            return {
              async: false,
              popContainer: '.link-table-field-config-modal',
              dictOptions: tempOptions,
              immediateChange: true,
              onChange:async (name)=>{
                if(oldValue.dictText){
                  await formActionType.setFieldsValue({
                    dictText: ''
                  });
                  await formActionType.clearValidate();
                }
                handleChange(name);
              }
            }
          }
        },
        {
          label: '显示字段',
          field: 'dictText',
          component: 'JSearchSelect',
          required: true,
          componentProps:{
            async: false,
            popContainer: '.link-table-field-config-modal',
            dictOptions: fieldOptions,
            onChange: (value)=>{
              oldValue['dictText'] = value;
            }
          }
        }
      ]

      // 表单配置
      const [ registerForm, {validate, setFieldsValue, clearValidate}] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      async function handleSubmit(){
        const data = await validate()
 /*     dbFieldTxt: "姓名"
        dictTable: "test"
        multiSelect: false
        otherFields: "create_by,xxx"
        showType: "card"
        titleField: "namexx"*/
        console.log('handlesub', data);
        emit('success', data)
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
