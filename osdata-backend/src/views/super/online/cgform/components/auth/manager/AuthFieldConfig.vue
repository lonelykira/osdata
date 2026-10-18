<template>
  <div class="auth-field-config">
    <!-- update-begin--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索 -->
    <a-input-search
      v-model:value="fieldSearchText"
      class="field-search"
      placeholder="搜索字段名称/描述"
      allowClear
      @search="applyFieldSearch"
    />
    <!-- update-end--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索 -->
    <BasicTable @register="registerTable" @change="handleTableChange" :loading="tableLoading">
      <!-- update-begin--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选 -->
      <template #headerCell="{ column }">
        <template v-if="column.dataIndex === 'switch'">
          <a-switch :loading="allSloading" v-model:checked="allSwitch" size="small" @change="handleChangeSwitch"></a-switch>启用
        </template>
        <template v-else-if="column.dataIndex === 'list'">
          <a-checkbox :indeterminate="listIndeterminate" v-model:checked="allListControl" :disabled="!allSwitch" @change="handleChangeList">{{ column.customTitle }}</a-checkbox>
        </template>
        <template v-else-if="column.dataIndex === 'form'">
          <a-checkbox :indeterminate="formIndeterminate" v-model:checked="allFormControl" :disabled="!allSwitch" @change="handleChangeForm">{{ column.customTitle }}</a-checkbox>
        </template>
        <template v-else>
          {{ column.customTitle }}
        </template>
      </template>
      <!-- update-end--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选 -->
      <template #switch="{ text, record }">
        <a-switch size="small" :checked="record.status === 1" @change="(flag) => onUpdateStatus(flag, record)" />
      </template>

      <template #list="{ text, record }">
        <a-checkbox :checked="record.listShow" :disabled="record.status === 0" @change="(e) => onCheckboxChange(e, record, 1)"> 可见 </a-checkbox>
      </template>

      <template #form="{ text, record }">
        <a-checkbox :checked="record.formShow" :disabled="record.status === 0" @change="(e) => onCheckboxChange(e, record, 2)"> 可见 </a-checkbox>
        <a-checkbox :checked="record.formEditable" :disabled="record.status === 0" @change="(e) => onCheckboxChange(e, record, 3)">
          可编辑
        </a-checkbox>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { watch, defineComponent, ref } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { authFieldLoadData, authFieldUpdateCheckbox, authFieldUpdateStatus, batchAuthFieldUpdateStatus, batchAuthFieldUpdateCheckbox } from '../auth.api';
  import { authFieldColumns, isAuthDataField } from '../auth.data';

  export default defineComponent({
    name: 'AuthFieldConfig',
    components: { BasicTable },
    props: {
      headId: {
        type: String,
        default: '',
        required: true,
      },
    },
    emits: ['update:authFields'],
    setup(props, { emit }) {
      const cgformId = ref('');
      const allSwitch = ref(false);
      const allListControl = ref(false);
      const allFormControl = ref(false);
      const allSloading = ref(false);
      const tableLoading = ref(false);
      const formIndeterminate = ref(false);
      const listIndeterminate = ref(false);
      // update-begin--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索
      const fieldSearchText = ref('');
      const allFieldData = ref<any[]>([]);

      const [registerTable, { reload, getTableRef, setPagination, setTableData }] = useTable({
        api: loadData,
        rowKey: 'code',
        bordered: true,
        columns: authFieldColumns,
        showIndexColumn: false,
      });
      // update-end--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索

      watch(
        () => props.headId,
        (headId) => {
          cgformId.value = headId.split('?')[0];
          fieldSearchText.value = '';
          // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-149】点击权限控制进入页面后，分页没有重置
          getTableRef().value && setPagination({ current: 1, pageSize: 10 });
          // update-end--author:liaozhiyang---date:20240520---for：【TV360X-149】点击权限控制进入页面后，分页没有重置
          reload().catch(() => null);
        },
        { immediate: true }
      );
      // update-begin--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索
      function filterFieldData(data: any[], keyword: string) {
        const kw = keyword.trim().toLowerCase();
        if (!kw) return data;
        return data.filter(
          (item) => String(item.code || '').toLowerCase().includes(kw) || String(item.title || '').toLowerCase().includes(kw)
        );
      }

      function applyFieldSearch() {
        if (!allFieldData.value.length) return;
        const displayData = filterFieldData(allFieldData.value, fieldSearchText.value);
        setTableData(displayData);
        setPagination({ current: 1 });
        const { current, pageSize } = getTableRef().value!.getPaginationRef();
        setCurDataStatus(current, pageSize, displayData);
      }

      watch(fieldSearchText, (value) => {
        if (!value) {
          applyFieldSearch();
        }
      });
      // update-end--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索

      // 加载数据
      async function loadData(params) {
        const exclude = ['id'];
        let data = await authFieldLoadData(cgformId.value, params);
        let fields: any[] = [];
        let filterData: any[] = [];
        data.forEach((item) => {
          if (exclude.indexOf(item.code) < 0) {
            if (item.isShowForm == 1 || item.isShowList == 1) {
              filterData.push(item);
            }
            //update-begin-author:taoyan date:2022-8-9 for: VUEN-1957 【online】同步数据库新增字段未提交
            if (isAuthDataField(item)) {
              fields.push({
                text: item.title,
                value: item.code,
                // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-201】权限管理条件根据控件过滤
                view: item.fieldShowType,
                dbType: item.dbType,
                // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-201】权限管理条件根据控件过滤
                // -update-begin--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
                dictField: item.dictField,
                dictTable: item.dictTable,
                dictText: item.dictText,
                fieldExtendJson: item.fieldExtendJson,
                // -update-end--author:scott---date:20260722---for：【数据权限】规则值根据规则字段控件类型自动切换下拉/字典选择
              });
            }
            //update-end-author:taoyan date:2022-8-9 for: VUEN-1957 【online】同步数据库新增字段未提交
          }
        });

        // update-begin--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索
        allFieldData.value = filterData;
        emit('update:authFields', fields);
        const displayData = filterFieldData(filterData, fieldSearchText.value);
        // update-end--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
        setCurDataStatus(params.pageNo, params.pageSize, displayData);
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
        return displayData;
      }

      async function onUpdateStatus(flag, record) {
        await authFieldUpdateStatus({
          cgformId: cgformId.value,
          code: record.code,
          status: flag ? 1 : 0,
        });
        if (!(record.formEditable || record.formShow || record.listShow)) {
          record.formEditable = true;
          record.formShow = true;
          record.listShow = true;
        }
        record.status = Math.abs(record.status - 1);
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
        itemChange();
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
      }

      async function onCheckboxChange(event, record, switchFlag) {
        let checked = event.target.checked;
        await authFieldUpdateCheckbox({
          cgformId: cgformId.value,
          code: record.code,
          switchFlag: switchFlag,
          listShow: checked,
          formShow: checked,
          formEditable: checked,
        });
        if (switchFlag == 1) {
          record.listShow = checked;
        } else if (switchFlag == 2) {
          record.formShow = checked;
        } else if (switchFlag == 3) {
          record.formEditable = checked;
        }
        // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-2087】依次去掉当前行的列表控制可见、表单控制可见、可编辑时启动状态没变
        if (record.listShow === false && record.formShow === false && record.formEditable === false) {
          record.status = 0;
        }
        // update-end--author:liaozhiyang---date:20240807---for：【TV360X-2087】依次去掉当前行的列表控制可见、表单控制可见、可编辑时启动状态没变
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
        itemChange();
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
      }
      // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
      // 加载每一页数据时设置状态
      function setCurDataStatus(current, pageSize, data) {
        const result: any = [];
        if (data?.length) {
          const max = current * pageSize > data.length ? data.length : current * pageSize;
          for (let i = current * pageSize - pageSize; i < max; i++) {
            const item = data[i];
            result.push(item);
          }
        }
        if (result.length) {
          // 默认都是true，只有一项为false则总开关即是false
          allSwitch.value = true;
          allListControl.value = true;
          allFormControl.value = true;
          result.forEach((item) => {
            if (allSwitch.value && item.status == 0) {
              allSwitch.value = false;
            }
            if (allListControl.value && item.listShow == false) {
              allListControl.value = false;
            }
            if (allFormControl.value && (item.formEditable == false || item.formShow == false)) {
              allFormControl.value = false;
            }
          });
          if (allListControl.value == true) {
            listIndeterminate.value = false;
          } else {
            const findItem = result.find((item) => item.listShow);
            if (findItem) {
              listIndeterminate.value = true;
            } else {
              listIndeterminate.value = false;
            }
          }
          if (allFormControl.value == true) {
            formIndeterminate.value = false;
          } else {
            const findItem = result.find((item) => item.formEditable || item.formShow);
            if (findItem) {
              formIndeterminate.value = true;
            } else {
              formIndeterminate.value = false;
            }
          }
        } else {
          allSwitch.value = false;
          allListControl.value = false;
          allFormControl.value = false;
        }
      }
      // 单独项状态变动时
      const itemChange = () => {
        const { current, pageSize } = getTableRef().value!.getPaginationRef();
        const allDataSource = getTableRef().value!.getDataSource();
        setCurDataStatus(current, pageSize, allDataSource);
      };

      // 获取某页dataSource
      const getCurrentDataSource = (current, pageSize) => {
        const result: any = [];
        const allDataSource = getTableRef().value!.getDataSource();
        if (allDataSource?.length) {
          const max = current * pageSize > allDataSource.length ? allDataSource.length :current * pageSize;
          for (let i = current * pageSize - pageSize; i < max; i++) {
            const item = allDataSource[i];
            result.push(item);
          }
        }
        return result;
      };
      // 开关（表头|总的）
      const handleChangeSwitch = async (checked) => {
        tableLoading.value = true;
        allSwitch.value = checked;
        const { current, pageSize } = getTableRef().value!.getPaginationRef();
        const dataSource = getCurrentDataSource(current, pageSize);
        let params = dataSource.map((item) => ({ cgformId: item.cgformId, code: item.code, status: checked ? 1 : 0 }));
        allSloading.value = true;
        await batchAuthFieldUpdateStatus(params);
        dataSource.forEach((item) => {
          if (checked) {
            item.status = 1;
          } else {
            item.status = 0;
          }
          // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-1992】数据权限-字段权限全选启用，列表控制和表单控制勾选
          if (!(item.formEditable || item.formShow || item.listShow)) {
            item.formEditable = true;
            item.formShow = true;
            item.listShow = true;
          }
        });
        // update-end--author:liaozhiyang---date:20240807---for：【TV360X-1992】数据权限-字段权限全选启用，列表控制和表单控制勾选
        allSloading.value = false;
        tableLoading.value = false;
        const allDataSource = getTableRef().value!.getDataSource();
        setCurDataStatus(current, pageSize, allDataSource);
      };
      const handleChangeList = async (e) => {
        tableLoading.value = true;
        const checked = e.target.checked;
        allListControl.value = checked;
        const { current, pageSize } = getTableRef().value!.getPaginationRef();
        const dataSource = getCurrentDataSource(current, pageSize);
        let params = dataSource.map((item) => ({ cgformId: item.cgformId, code: item.code, switchFlag: 1, listShow: !!checked }));
        await batchAuthFieldUpdateCheckbox(params);
        dataSource.forEach((item) => {
          item.listShow = !!checked;
          // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-2087】依次去掉当前行的列表控制可见、表单控制可见、可编辑时启动状态没变
          if (item.listShow === false && item.formShow === false && item.formEditable === false) {
            item.status = 0;
            allSwitch.value = false;
          }
          // update-end--author:liaozhiyang---date:20240807---for：【TV360X-2087】依次去掉当前行的列表控制可见、表单控制可见、可编辑时启动状态没变
        });
        if (checked) {
          listIndeterminate.value = false;
        }
        tableLoading.value = false;
      };
      const handleChangeForm = async (e) => {
        tableLoading.value = true;
        const checked = e.target.checked;
        allFormControl.value = checked;
        const { current, pageSize } = getTableRef().value!.getPaginationRef();
        const dataSource = getCurrentDataSource(current, pageSize);
        const params = [
          ...dataSource.map((item) => ({ cgformId: item.cgformId, code: item.code, switchFlag: 4, formShow: !!checked, formEditable: !!checked  })),
        ];
        await batchAuthFieldUpdateCheckbox(params);
        dataSource.forEach((item) => {
          item.formEditable = !!checked;
          item.formShow = !!checked;
          // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-2087】依次去掉当前行的列表控制可见、表单控制可见、可编辑时启动状态没变
          if (item.listShow === false && item.formShow === false && item.formEditable === false) {
            item.status = 0;
            allSwitch.value = false;
          }
          // update-end--author:liaozhiyang---date:20240807---for：【TV360X-2087】依次去掉当前行的列表控制可见、表单控制可见、可编辑时启动状态没变
        });
        if (checked) {
          formIndeterminate.value = false;
        }
        tableLoading.value = false;
      };
      // 监听页码变动
      const handleTableChange = (pagination) => {
        // const { current, pageSize } = pagination;
        // const dataSource = getCurrentDataSource(current, pageSize);
        // // 先置初始值
        // allSwitch.value = false;
        // if(dataSource.length) {

        // }
      }
      // update-end--author:liaozhiyang---date:20240612---for：【TV360X-148】权限配置加全选
      return {
        registerTable,
        onUpdateStatus,
        onCheckboxChange,
        handleChangeSwitch,
        allSwitch,
        allFormControl,
        allListControl,
        allSloading,
        handleTableChange,
        handleChangeList,
        handleChangeForm,
        tableLoading,
        formIndeterminate,
        listIndeterminate,
        fieldSearchText,
        applyFieldSearch,
      };
    },
  });
</script>

<style lang="less" scoped>
  /* update-begin--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索 */
  .field-search {
    width: 260px;
    margin-bottom: 12px;
  }
  /* update-end--author:liaozhiyang---date:20260810---for：【LHZP-589】字段权限新增一个搜索 */
  .auth-field-config :deep(.ant-checkbox + span) {
    padding-left: 2px;
  }
  :deep(.ant-table-thead) {
    .ant-switch {
      margin-right: 5px;
    }
    .ant-checkbox-wrapper {
      .ant-checkbox {
        margin-right: 4px;
      }
      .ant-checkbox-disabled + span {
        color: #000;
      }
    }
  }
  html[data-theme='light'] {
    .auth-field-config {
      :deep(.ant-table-thead) {
        .ant-checkbox-wrapper {
          .ant-checkbox-disabled + span {
            color: #000;
          }
        }
      }
    }
  }
  html[data-theme='dark'] {
    .auth-field-config {
      :deep(.ant-table-thead) {
        .ant-checkbox-wrapper {
          color: rgba(255, 255, 255, 0.65);
          .ant-checkbox-disabled + span {
            color: rgba(255, 255, 255, 0.65);
          }
        }
      }
    }
  }
</style>
