import {Button, Space} from 'ant-design-vue'
import {useMessage} from "@/hooks/web/useMessage";

const {createConfirm} = useMessage()

/**
 * 创建列表删除时的提示弹窗
 * @param deleteFn 删除方法
 * @param removeFn 移出方法
 */
export function showListDeleteConfirm(deleteFn, removeFn) {
  const {destroy} = createConfirm({
    title: '确认删除表单吗？',
    content: () => (
      <div style="font-size: 13px; color: #666; line-height: 1.8;">
        <div>移除：仅删除配置，保留数据库表和数据</div>
        <div>删除：同时删除数据库表和数据（不可恢复）</div>
      </div>
    ),
    iconType: 'info',
    closable: true,
    maskClosable: true,
    width: 400,
    footer: () => (
      <div style="text-align: right; margin-top: 12px;">
        <Space>
          <Button onClick={() => destroy()}>取消</Button>
          <Button type="primary" ghost onClick={getFn(removeFn)}>移除表单</Button>
          <Button type="primary" danger onClick={getFn(deleteFn)}>删除表单</Button>
        </Space>
      </div>
    ),
  });

  function getFn(func) {
    return async () => {
      await func()
      destroy()
    }
  }

}
