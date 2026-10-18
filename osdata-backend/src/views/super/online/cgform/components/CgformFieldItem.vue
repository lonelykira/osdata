<template>
  <div class="cgf-item">
    <label class="cgf-label">
      <span v-if="required" class="cgf-required">*</span>{{ label }}
    </label>
    <a-input
      v-if="type === 'input'"
      :value="modelValue"
      :disabled="disabled"
      :allow-clear="allowClear"
      :status="status"
      style="width: 100%"
      @update:value="emit('update:modelValue', $event)"
      @change="emit('change', $event)"
    />
    <a-input-number
      v-else-if="type === 'input-number'"
      :value="modelValue"
      :disabled="disabled"
      style="width: 100%"
      @update:value="emit('update:modelValue', $event)"
      @change="emit('change', $event)"
    />
    <a-select
      v-else-if="type === 'select'"
      :value="modelValue"
      :options="options"
      :allow-clear="false"
      :disabled="disabled"
      :status="status"
      style="width: 100%"
      @update:value="emit('update:modelValue', $event)"
      @change="emit('change', $event)"
    />
    <a-radio-group
      v-else-if="type === 'radio-group'"
      :value="modelValue"
      :options="options"
      :disabled="disabled"
      @update:value="emit('update:modelValue', $event)"
      @change="emit('change', $event)"
    />
  </div>
</template>

<script setup lang="ts">
  defineProps<{
    label: string;
    type: 'input' | 'select' | 'input-number' | 'radio-group';
    modelValue?: any;
    required?: boolean;
    disabled?: boolean;
    allowClear?: boolean;
    options?: { label: string; value: any }[];
    status?: '' | 'error';
  }>();

  const emit = defineEmits<{
    (e: 'update:modelValue', value: any): void;
    (e: 'change', value: any): void;
  }>();
</script>
