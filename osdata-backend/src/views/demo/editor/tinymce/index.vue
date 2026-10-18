<template>
  <div :style="{ height: contentHeight, overflowY: 'scroll' }">
    <PageWrapper title="富文本组件示例">
      <Tinymce v-model="value" @change="handleChange" width="100%" />
      <div style="height: 1000px"></div>
    </PageWrapper>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { Tinymce } from '/@/components/Tinymce/index';
  import { PageWrapper } from '/@/components/Page';
  import { useLayoutHeight } from '@/layouts/default/content/useContentViewHeight';
  export default defineComponent({
    components: { Tinymce, PageWrapper },
    setup() {
      const value = ref('hello world!');
      const { headerHeightRef } = useLayoutHeight();
      function handleChange(value: string) {
        console.log(value);
      }
      //【issues/9448】
      const contentHeight = computed(() => {
        return `calc(100vh - ${headerHeightRef.value}px)`;
      });
      return { handleChange, value, contentHeight };
    },
  });
</script>
