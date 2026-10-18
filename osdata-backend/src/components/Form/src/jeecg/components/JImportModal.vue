<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      title="导入 Excel"
      :width="620"
      :minHeight="0"
      :bodyStyle="{ padding: '20px 24px 16px' }"
      :canFullscreen="false"
      @cancel="handleClose"
      :confirmLoading="uploading"
      destroyOnClose
    >
      <div class="j-import-modal">
        <!--是否校验-->
        <div v-if="online" class="validate-setting">
          <div class="validate-setting__content">
            <div class="validate-setting__title">导入数据校验</div>
            <div class="validate-setting__description">开启后将校验文件内容，并反馈不符合规则的数据</div>
          </div>
          <a-switch :checked="validateStatus == 1" @change="handleChangeValidateStatus" checked-children="是" un-checked-children="否" />
        </div>
        <!--上传-->
        <a-upload-dragger
          class="import-uploader"
          name="file"
          accept=".xls,.xlsx"
          :multiple="true"
          :fileList="fileList"
          :showUploadList="false"
          @remove="handleRemove"
          :beforeUpload="beforeUpload"
        >
          <template v-if="fileList.length === 0">
            <div class="upload-icon">
              <InboxOutlined />
            </div>
            <div class="upload-title">点击或拖拽 Excel 文件到此处</div>
            <div class="upload-description">支持 .xls、.xlsx 格式，可一次选择多个文件</div>
          </template>
          <div v-else class="selected-files">
            <div class="selected-files__header">
              <div>
                <div class="selected-files__title"><CheckCircleFilled /> 已选择 {{ fileList.length }} 个文件</div>
                <div class="selected-files__description">点击此区域可继续添加文件</div>
              </div>
              <span class="selected-files__add">继续添加</span>
            </div>
            <div class="selected-files__list">
              <div v-for="(file, index) in fileList" :key="file.uid || `${file.name}-${index}`" class="selected-file" @click.stop>
                <span class="selected-file__icon"><FileExcelOutlined /></span>
                <span class="selected-file__name" :title="file.name">{{ file.name }}</span>
                <a-button type="text" class="selected-file__remove" title="移除文件" @click.stop="handleRemove(file)">
                  <DeleteOutlined />
                </a-button>
              </div>
            </div>
          </div>
        </a-upload-dragger>
      </div>
      <!--页脚-->
      <template #footer>
        <a-button @click="handleClose">关闭</a-button>
        <a-button type="primary" @click="handleImport" :disabled="uploadDisabled" :loading="uploading">{{
          uploading ? '上传中...' : '开始上传'
        }}</a-button>
      </template>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { CheckCircleFilled, DeleteOutlined, FileExcelOutlined, InboxOutlined } from '@ant-design/icons-vue';
  import { defineComponent, ref, unref, watchEffect, computed, h } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { defHttp } from '/@/utils/http/axios';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'JImportModal',
    components: {
      BasicModal,
      CheckCircleFilled,
      DeleteOutlined,
      FileExcelOutlined,
      InboxOutlined,
    },
    props: {
      url: {
        type: String,
        default: '',
        required: false,
      },
      biz: {
        type: String,
        default: '',
        required: false,
      },
      //是否online导入
      online: {
        type: Boolean,
        default: false,
        required: false,
      },
    },
    emits: ['ok', 'register'],
    setup(props, { emit, refs }) {
      const { createMessage, createWarningModal } = useMessage();
      //注册弹框
      const [register, { closeModal }] = useModalInner((data) => {
        reset(data);
      });
      const glob = useGlobSetting();
      const attrs = useAttrs();
      const uploading = ref(false);
      //文件集合
      const fileList = ref([]);
      //上传url
      const uploadAction = ref('');
      const foreignKeys = ref('');
      //校验状态
      const validateStatus = ref(1);
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      //监听url
      watchEffect(() => {
        props.url && (uploadAction.value = `${glob.uploadUrl}${props.url}`);
      });
      //按钮disabled状态
      const uploadDisabled = computed(() => !(unref(fileList).length > 0));

      //关闭方法
      function handleClose() {
        // 代码逻辑说明: 【QQYUN-7477】关闭弹窗清空内容（之前上传失败关闭后不会清除）
        closeModal();
        reset();
      }

      //校验状态切换
      function handleChangeValidateStatus(checked) {
        validateStatus.value = !!checked ? 1 : 0;
      }

      //移除上传文件
      function handleRemove(file) {
        const index = unref(fileList).indexOf(file);
        const newFileList = unref(fileList).slice();
        newFileList.splice(index, 1);
        fileList.value = newFileList;
      }

      //上传前处理
      function beforeUpload(file) {
        fileList.value = [...unref(fileList), file];
        return false;
      }

      //文件上传
      function handleImport() {
        let { biz, online } = props;
        const formData = new FormData();
        if (biz) {
          formData.append('isSingleTableImport', biz);
        }
        if (unref(foreignKeys) && unref(foreignKeys).length > 0) {
          formData.append('foreignKeys', unref(foreignKeys));
        }
        // 代码逻辑说明: 【issues/6124】当用户没有【Online表单开发】页面的权限时用户无权导入从表数据
        if (isObject(foreignKeys.value)) {
          formData.append('foreignKeys', JSON.stringify(foreignKeys.value));
        }
        if (!!online) {
          formData.append('validateStatus', unref(validateStatus));
        }
        unref(fileList).forEach((file, index) => {
          formData.append(`files[${index}]`, file);
        });
        uploading.value = true;

        //TODO 请求怎样处理的问题
        let headers = {
          'Content-Type': 'multipart/form-data;boundary = ' + new Date().getTime(),
        };
        defHttp.post({ url: props.url, params: formData, headers }, { isTransformResponse: false }).then((res) => {
          uploading.value = false;
          if (res.success) {
            if (res.code == 201) {
              errorTip(res.message, res.result);
            } else {
              createMessage.success(res.message);
            }
            handleClose();
            reset();
            emit('ok');
          } else {
            createMessage.warning(res.message);
          }
        }).catch(() => {
          uploading.value = false;
        });
      }

      //错误信息提示
      function errorTip(tipMessage, fileUrl) {
        //update-begin---author:scott ---date:20260813  for：优化Excel导入弹窗并修复错误提示HTML注入风险-----------
        const resolvedUrl = new URL(`${glob.uploadUrl}${fileUrl}`, window.location.origin);
        const href = ['http:', 'https:'].includes(resolvedUrl.protocol) ? resolvedUrl.href : '#';
        const resultMessage = String(tipMessage ?? '')
          .replace(/,/g, '，')
          .replace(/:/g, '：');
        createWarningModal({
          title: '导入完成，部分数据未导入',
          centered: true,
          width: 480,
          okText: '知道了',
          content: h('div', { class: 'j-import-result-content' }, [
            h('div', { class: 'j-import-result-summary' }, [
              h('div', { class: 'j-import-result-summary__label' }, '导入结果'),
              h('div', { class: 'j-import-result-summary__message' }, resultMessage),
            ]),
            h('a', { class: 'j-import-result-download', href, target: '_blank', rel: 'noopener noreferrer' }, [
              h('span', { class: 'j-import-result-download__main' }, [h(FileExcelOutlined), h('span', '下载错误数据明细')]),
              h('span', { class: 'j-import-result-download__meta' }, 'Excel'),
            ]),
          ]),
        });
        //update-end---author:scott ---date:20260813  for：优化Excel导入弹窗并修复错误提示HTML注入风险-----------
      }

      //重置
      function reset(arg?) {
        fileList.value = [];
        uploading.value = false;
        foreignKeys.value = arg;
        validateStatus.value = 1;
      }

      return {
        register,
        getBindValue,
        uploadDisabled,
        fileList,
        uploading,
        validateStatus,
        handleClose,
        handleChangeValidateStatus,
        handleRemove,
        beforeUpload,
        handleImport,
      };
    },
  });
</script>

<style scoped lang="less">
  .j-import-modal {
    .validate-setting {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 24px;
      margin-bottom: 16px;
      padding: 12px 16px;
      border: 1px solid #edf0f5;
      border-radius: 8px;
      background: #f7f9fc;
    }

    .validate-setting__content {
      min-width: 0;
    }

    .validate-setting__title {
      color: #1f2937;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
    }

    .validate-setting__description {
      margin-top: 2px;
      color: #8c8c8c;
      font-size: 12px;
      line-height: 20px;
    }

    .import-uploader {
      :deep(.ant-upload-drag) {
        border-color: #cbd5e1;
        border-radius: 10px;
        background: #fbfcfe;
        transition:
          border-color 0.2s ease,
          background-color 0.2s ease;
      }

      :deep(.ant-upload-drag:hover) {
        border-color: @primary-color;
        background: fade(@primary-color, 3%);
      }

      :deep(.ant-upload-btn) {
        padding: 28px 16px 26px;
      }
    }

    .upload-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 52px;
      height: 52px;
      margin-bottom: 12px;
      border-radius: 14px;
      color: @primary-color;
      background: fade(@primary-color, 10%);
      font-size: 26px;
    }

    .upload-title {
      color: #262626;
      font-size: 15px;
      font-weight: 500;
      line-height: 24px;
    }

    .upload-description {
      margin-top: 4px;
      color: #8c8c8c;
      font-size: 13px;
      line-height: 22px;
    }

    .selected-files {
      width: 100%;
      text-align: left;
    }

    .selected-files__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 12px;
    }

    .selected-files__title {
      color: #262626;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;

      :deep(.anticon) {
        margin-right: 6px;
        color: #52c41a;
      }
    }

    .selected-files__description {
      margin-top: 2px;
      color: #8c8c8c;
      font-size: 12px;
      line-height: 20px;
    }

    .selected-files__add {
      flex: none;
      color: @primary-color;
      font-size: 13px;
    }

    .selected-files__list {
      max-height: 116px;
      overflow-y: auto;
    }

    .selected-file {
      display: flex;
      align-items: center;
      min-height: 38px;
      padding: 4px 6px;
      border: 1px solid #e8edf3;
      border-radius: 6px;
      background: #f5f7fa;
      transition:
        border-color 0.2s ease,
        background-color 0.2s ease;

      & + & {
        margin-top: 4px;
      }

      &:hover {
        border-color: fade(@primary-color, 18%);
        background: fade(@primary-color, 6%);

        .selected-file__remove {
          opacity: 1;
        }
      }
    }

    .selected-file__icon {
      flex: none;
      margin-right: 8px;
      color: #22a06b;
      font-size: 18px;
    }

    .selected-file__name {
      min-width: 0;
      flex: 1;
      overflow: hidden;
      color: #4b5563;
      font-size: 13px;
      line-height: 22px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .selected-file__remove {
      flex: none;
      color: #7a8491;
      opacity: 0.75;

      &:hover {
        color: #ff4d4f;
      }
    }
  }

  :global(.j-import-result-content) {
    margin-top: 4px;
  }

  :global(.j-import-result-summary) {
    padding: 12px 14px;
    border: 1px solid #ffe7a3;
    border-radius: 8px;
    background: #fffaf0;
  }

  :global(.j-import-result-summary__label) {
    margin-bottom: 4px;
    color: #ad6800;
    font-size: 12px;
    font-weight: 500;
    line-height: 20px;
  }

  :global(.j-import-result-summary__message) {
    color: #595959;
    font-size: 14px;
    line-height: 22px;
  }

  :global(.j-import-result-download) {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding: 10px 12px;
    border: 1px solid #d9eaf7;
    border-radius: 8px;
    background: #f7fbff;
    transition:
      border-color 0.2s ease,
      background-color 0.2s ease;

    &:hover {
      border-color: fade(@primary-color, 35%);
      background: fade(@primary-color, 6%);
    }
  }

  :global(.j-import-result-download__main) {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: @primary-color;
    font-size: 14px;
    font-weight: 500;
  }

  :global(.j-import-result-download__meta) {
    color: #8c8c8c;
    font-size: 12px;
  }
</style>
