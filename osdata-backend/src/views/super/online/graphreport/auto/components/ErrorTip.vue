<script lang="ts">
  import { defineComponent, h } from 'vue';
  import { Empty } from 'ant-design-vue';

  export default defineComponent({
    name: 'ErrorTip',
    props: {
      // 提示文本
      tip: String,
      // 外层标签名
      tag: { type: String, default: 'div' },
    },
    render() {
      return h(
        this.tag,
        {
          class: ['error-tip'],
        },
        h(Empty, { description: this.tip })
      );
    },
  });
</script>

<style lang="less" scoped>
  .error-tip {
    padding: 20px;
    background: white;
    text-align: center;
  }
</style>