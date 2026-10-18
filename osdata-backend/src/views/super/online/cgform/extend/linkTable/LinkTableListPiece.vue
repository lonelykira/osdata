<template>
  <div class="link-table-piece" @click="handleClick">
    <span>{{ text }}</span>
  </div>
</template>

<script>
  /**
   * 关联记录 在列表上显示的 小块
   */
  import { propTypes } from '/@/utils/propTypes';
  export default {
    name: 'LinkTableListPiece',
    props: {
      text: propTypes.string.def(''),
      id: propTypes.string.def(''),
    },
    emits: ['tab'],
    setup(props, { emit }) {
      function handleClick(e) {
        e?.stopPropagation();
        e?.preventDefault();
        console.log(props.id);
        emit('tab', props.id);
      }
      return {
        handleClick,
      };
    },
  };
</script>

<style scoped lang="less">
  .link-table-piece {
    background-color: #e8e8e8;
    border-radius: 3px;
    color: #333;
    display: inline-block;
    padding: 0 10px;
    position: relative;
    cursor: pointer;
    background-color: #e8e8e8;
    border-radius: 3px;
    line-height: 21px;
    margin-right: 3px;
    /*margin: 6px 0 0 6px;*/
    padding: 0 7px;
    max-width: 100px;
    > span {
      display: inline-block !important;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      vertical-align: top;
      white-space: nowrap;
    }
    &:hover {
      background-color: #dfebfa;
      color: #2196f3;
    }
  }
</style>
