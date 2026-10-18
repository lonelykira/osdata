<template>
  <div>
    <template v-if="hasFile">
      <div v-for="(file, fileKey) of [innerFile || {}]" :key="fileKey" class="j-vxe-file-item">
        <a-tooltip v-if="file.status === 'uploading'" :title="`上传中(${Math.floor(file.percent)}%)`">
          <span class="j-vxe-file-content">
            <LoadingOutlined />
            <span class="j-vxe-file-name">上传中…</span>
          </span>
        </a-tooltip>

        <a-tooltip v-else-if="file.status === 'done'" :title="file.name" :mouseEnterDelay="0.4">
          <button
            type="button"
            class="j-vxe-file-content j-vxe-file-content--action"
            title=""
            :disabled="cellProps.disabled"
            :aria-label="`管理附件，共 ${uploadCount} 个`"
            @click="handleMoreOperation"
          >
            <Icon icon="ant-design:paper-clip" />
            <span class="j-vxe-file-name">{{ shortFileName }}</span>
            <span v-if="uploadCount > 1" class="j-vxe-file-count">{{ uploadCount }}</span>
          </button>
        </a-tooltip>

        <a-tooltip v-else :title="file.message || '上传失败'">
          <span class="j-vxe-file-content">
            <Icon icon="ant-design:exclamation-circle" style="color: red" />
            <span class="j-vxe-file-name">{{ shortFileName }}</span>
          </span>
        </a-tooltip>

        <Dropdown v-if="file.status !== 'uploading'" :trigger="['click']" placement="bottomRight">
          <a-tooltip title="操作">
            <button type="button" class="j-vxe-file-action" aria-label="文件操作">
              <Icon icon="ant-design:ellipsis-outlined" />
            </button>
          </a-tooltip>
          <template #overlay>
            <a-menu>
              <a-menu-item :disabled="cellProps.disabled" @click="handleMoreOperation">
                <span><Icon icon="ant-design:bars" />&nbsp;管理</span>
              </a-menu-item>
              <a-menu-item v-if="originColumn.allowDownload !== false" @click="handleClickDownloadFile">
                <span><Icon icon="ant-design:download" />&nbsp;下载</span>
              </a-menu-item>
              <a-menu-item :disabled="cellProps.disabled" v-if="originColumn.allowRemove !== false" @click="handleClickDeleteFile">
                <span><Icon icon="ant-design:delete" />&nbsp;删除</span>
              </a-menu-item>
            </a-menu>
          </template>
        </Dropdown>
      </div>
    </template>

    <a-button
      v-if="!cellProps.disabledTable"
      v-show="!hasFile"
      class="j-vxe-upload-trigger"
      size="small"
      preIcon="ant-design:upload"
      :disabled="cellProps.disabled"
      @click="handleMoreOperation"
    >
      {{ originColumn.btnText || '附件' }}
    </a-button>
    <JUploadModal :value="modalValue" @register="registerModel" @change="onModalChange" />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { useFileCell, enhanced, components } from '../hooks/useFileCell';

  export default defineComponent({
    name: 'JVxeFileCell',
    components: components,
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      return useFileCell(props, UploadTypeEnum.file);
    },
    // 【组件增强】注释详见：JVxeComponent.Enhanced
    enhanced: enhanced,
  });
</script>

<style scoped lang="less">
  .j-vxe-file-item {
    display: inline-flex;
    align-items: center;
    max-width: 100%;
    height: 28px;
    overflow: hidden;
    color: var(--vxe-ui-font-color);
    vertical-align: middle;
    background-color: var(--vxe-ui-layout-background-color);
    border: 1px solid var(--vxe-ui-table-border-color);
    border-radius: 4px;
  }

  .j-vxe-file-content {
    display: inline-flex;
    align-items: center;
    min-width: 0;
    height: 100%;
    padding: 0 8px;
    color: inherit;
    background: transparent;
    border: 0;
    column-gap: 6px;
  }

  .j-vxe-file-content--action:not(:disabled) {
    cursor: pointer;

    &:hover,
    &:focus-visible {
      color: var(--vxe-ui-font-primary-color);
      background-color: rgba(64, 158, 255, 0.08);
    }

    &:focus-visible {
      outline: 2px solid var(--vxe-ui-font-primary-color) !important;
      outline-offset: -2px;
    }
  }

  .j-vxe-file-name {
    flex: 1 1 auto;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .j-vxe-file-count {
    flex: none;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    color: #fff;
    font-size: 10px;
    line-height: 16px;
    text-align: center;
    background-color: var(--vxe-ui-font-primary-color);
    border-radius: 8px;
  }

  .j-vxe-file-action {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    align-self: stretch;
    flex: 0 0 28px;
    width: 28px;
    padding: 0;
    color: var(--vxe-ui-font-color);
    cursor: pointer;
    background: transparent;
    border: 0;
    border-left: 1px solid var(--vxe-ui-table-border-color);
    transition:
      color 0.2s,
      background-color 0.2s;

    &:hover,
    &:focus-visible {
      color: var(--vxe-ui-font-primary-color);
      background-color: rgba(64, 158, 255, 0.08);
    }

    &:focus-visible {
      outline: 2px solid var(--vxe-ui-font-primary-color) !important;
      outline-offset: -2px;
    }
  }
</style>
