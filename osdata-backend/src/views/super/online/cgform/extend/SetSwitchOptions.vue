<template>
  <div class="setSwitchOptions">
    <p><span>是</span><a-input v-model:value="valueY" @change="handleChange" /></p>
    <p><span>否</span><a-input v-model:value="valueN" @change="handleChange" /></p>
  </div>
</template>

<script setup name="SwitchOptions">
  import { ref, watch, onMounted } from 'vue';
  import { isArray } from '/@/utils/is';
  const props = defineProps({
    value: {
      type: [Array, String],
      default: ['Y', 'N'],
    },
  });
  const emit = defineEmits('change', 'update:value');
  const defaultY = 'Y';
  const defaultN = 'N';
  const valueY = ref(defaultY);
  const valueN = ref(defaultN);

  watch(
    () => props.value,
    (value) => {
      if (typeof value === 'string') {
        const arr = value.split(',');
        valueY.value = arr[0];
        valueN.value = arr[1];
      } else if (isArray(value)) {
        valueY.value = value[0];
        valueN.value = value[1];
      }
    },
    { immediate: true }
  );
  const handleChange = () => {
    if (valueY.value != '' && valueN.value != '') {
      send();
    }
  };
  const send = () => {
    let Y = Number(valueY.value);
    let N = Number(valueN.value);
    // 都能转成number就转，否则就是string
    if (Number.isNaN(Y) || Number.isNaN(N)) {
      Y = valueY.value;
      N = valueN.value;
    }
    emit('change', [Y, N]);
    emit('update:value', [Y, N]);
  };
  // 进入组件就把默认值外传
  onMounted(() => {
    send();
  })
</script>

<style lang="less" scoped>
  .setSwitchOptions {
    display: flex;
    align-items: center;
    p {
      width: 30%;
      display: flex;
      align-items: center;
      span {
        margin-right: 5px;
        font-size: 13px;
      }
      &:first-child {
        margin-right: 16px;
      }
    }
  }
</style>
