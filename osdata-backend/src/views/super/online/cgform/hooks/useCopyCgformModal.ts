import { isRef, unref, watch, Ref, ComputedRef } from 'vue';
import Clipboard from 'clipboard';
import { ModalOptionsEx, useMessage } from '/@/hooks/web/useMessage';
import {buildUUID} from "/@/utils/uuid";

/** 带复制按钮的弹窗 */
interface IOptions extends ModalOptionsEx {
  // 要复制的文本，可以是一个 ref 对象，动态更新
  copyText: string | Ref<string> | ComputedRef<string>;
  copyTitle: string;
  componentName: string;
}

const COPY_CLASS = 'copy-this-text';
const CLIPBOARD_TEXT = 'data-clipboard-text';

export function useCopyModal() {
  return { createCopyModal };
}

const { createMessage, createConfirm } = useMessage();

/** 创建复制弹窗 */
function createCopyModal(options: Partial<IOptions>) {
  const url = unref(options.copyText);
  let menuComponentName = options.componentName? unref(options.componentName): null;
  const insertMenuSql = `INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external) 
                       VALUES ('${buildUUID()}', NULL, '${options.copyTitle}', '${url}', '1', '${menuComponentName}', NULL, 0, NULL, '1', 0.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', null, NULL, NULL, 0)`;

  
  let modal = createConfirm({
    ...options,
    iconType: options.iconType ?? 'info',
    width: options.width ?? 500,
    title: options.title ?? '复制',
    closable: true,
    maskClosable: options.maskClosable ?? true,
    cancelText: '复制SQL',
    okText: options.okText ?? '复制URL',
    cancelButtonProps: {
      class: 'copy-this-sql',
      'data-clipboard-text': insertMenuSql,
    } as any,
    okButtonProps: {
      ...options.okButtonProps,
      class: COPY_CLASS,
      [CLIPBOARD_TEXT]: url,
    } as any,
    onOk() {
      return new Promise((resolve: any) => {
        const clipboard = new Clipboard('.' + COPY_CLASS);
        clipboard.on('success', () => {
          clipboard.destroy();
          createMessage.success('复制URL成功');
          resolve();
        });
        clipboard.on('error', () => {
          createMessage.error('该浏览器不支持自动复制');
          clipboard.destroy();
          resolve();
        });
      });
    },
    onCancel() {
      return new Promise((resolve: any) => {
        const clipboard = new Clipboard('.copy-this-sql');
        clipboard.on('success', () => {
          clipboard.destroy();
          createMessage.success('复制插入菜单SQL成功');
          resolve();
        });
        clipboard.on('error', () => {
          createMessage.error('该浏览器不支持自动复制');
          clipboard.destroy();
          resolve();
        });
      });
    },
  });

  // 动态更新 copyText
  if (isRef(options.copyText)) {
    watch(options.copyText, (copyText) => {
      modal.update({
        okButtonProps: {
          ...options.okButtonProps,
          class: COPY_CLASS,
          [CLIPBOARD_TEXT]: copyText,
        } as any,
      });
    });
  }
  return modal;
}
