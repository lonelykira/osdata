<template>
  <div class="LinkTablePopup">
    <a-input v-bind="$attrs" :value="showText" readOnly @click.stop="handleAddRecord" />
    <template v-if="queryMode === 'table'">
      <AllTableListModal
        v-if="popListModalShow"
        @register="registerListModal"
        :tableName="tableName"
        :textField="textField"
        :valueField="valueField"
        :imageField="imageField"
        :multi="multi"
        @success="addCard"
      />
    </template>
    <template v-else>
      <OnlinePopListModal
        v-if="popListModalShow"
        @register="registerListModal"
        :multi="multi"
        :id="popTableName"
        :addAuth="auths.add"
        @success="addCard"
      />
      <OnlinePopModal v-if="popFormModalShow" :id="popTableName" @register="registerFormModal" @success="updateCardData" topTip />
    </template>
  </div>
</template>

<script setup>
  import { computed, ref, watch, defineAsyncComponent } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useLinkTable } from './useLinkTable';
  const OnlinePopListModal = defineAsyncComponent(() => import('../../auto/comp/OnlinePopListModal.vue'));
  const OnlinePopModal = defineAsyncComponent(() => import('../../auto/comp/OnlinePopModal.vue'));
  const AllTableListModal = defineAsyncComponent(() => import('/@/components/Form/src/jeecg/components/JLinkTableCard/components/AllTableListModal.vue'));
  // 给vxetable使用
  defineOptions({ name: 'LinkTable' });
  const props = defineProps({
    valueField: { type: String, default: 'id' },
    textField: { type: String, default: '' },
    tableName: { type: String, default: '' },
    multi: { type: Boolean, default: false },
    value: { type: [String, Number] },
    // ["表单字段,表字典字段","表单字段,表字典字段"]
    linkFields: { type: Array, default: () => [] },
    imageField: { type: String, default: '' },
    queryMode: { type: String, default: 'online' },
  });

  const emit = defineEmits(['change', 'update:value']);
  const popListModalShow = ref(false);
  const popFormModalShow = ref(false);
  // ---- Modal ----
  const [registerListModal, { openModal: openListModal }] = useModal();
  const [registerFormModal, { openModal: openFormModal }] = useModal();

  // ---- 数据 ----
  const selectRecords = ref([]);
  const popTableName = computed(() => props.tableName);

  const { auths, textFieldArray, transData, loadOne, compareData, formatData, initFormData } = useLinkTable(props);

  const showText = computed(() => {
    if (selectRecords.value.length === 0 || textFieldArray.value.length === 0) {
      return '';
    }
    const field = textFieldArray.value[0];
    return selectRecords.value.map((r) => r[field] ?? '').join(', ');
  });

  // ---- 操作 ----
  function handleAddRecord() {
    popListModalShow.value = true;
    setTimeout(() => {
      openListModal(true, {
        selectedRowKeys: selectRecords.value.map((item) => item[props.valueField]),
        selectedRows: [...selectRecords.value],
      });
    }, 300);
  }

  function addCard(data) {
    selectRecords.value = data.map((item) => {
      const temp = { ...item };
      transData(temp);
      return temp;
    });
    emitValue();
  }

  function updateCardData(formData) {
    const arr = selectRecords.value;
    for (let i = 0; i < arr.length; i++) {
      if (arr[i][props.valueField] === formData[props.valueField]) {
        const temp = { ...formData };
        transData(temp);
        arr.splice(i, 1, temp);
        break;
      }
    }
    selectRecords.value = arr;
    emitValue();
  }

  // ---- emit ----
  function emitValue() {
    const arr = selectRecords.value;
    const values = [];
    const formData = {};
    const linkFieldArray = props.linkFields;
    if (arr.length > 0) {
      for (let i = 0; i < arr.length; i++) {
        values.push(arr[i][props.valueField]);
        initFormData(formData, linkFieldArray, arr[i]);
      }
    } else {
      initFormData(formData, linkFieldArray);
    }
    const text = values.join(',');
    formatData(formData);
    emit('change', text, formData);
    emit('update:value', text);
  }

  // ---- 监听 value 回显 ----
  watch(
    () => props.value,
    async (val) => {
      if (val) {
        const changed = compareData(selectRecords.value, val) === false;
        if (changed) {
          selectRecords.value = await loadOne(val);
        }
        if (props.linkFields?.length > 0) {
          emitValue();
        }
      } else {
        selectRecords.value = [];
      }
    },
    { immediate: true }
  );
</script>

<style scoped lang="less"></style>
