<template>
  <div class="p-2">
    <JPopupOnlReport :id="id" :multi="true" :clickToRowSelect="false"></JPopupOnlReport>
  </div>
</template>
<script lang="ts">
  import JPopupOnlReport from '/@/components/jeecg/OnLine/JPopupOnlReport.vue';
  import { defineComponent } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRoute } from 'vue-router';

  export default defineComponent({
    name: 'OnlCgReportList',
    components: {
      JPopupOnlReport,
    },
    setup(props, { emit, refs }) {
      const { createMessage } = useMessage();
      const route = useRoute();
      const path = route.path;
      const id = path.substr(path.lastIndexOf('/') + 1);
      return {
        id,
      };
    },
  });
</script>
<style lang="less" scoped></style>
