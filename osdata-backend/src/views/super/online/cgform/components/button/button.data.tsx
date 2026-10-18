import {Icon} from "@/components/Icon";

export function getButtonIconRender({text}) {
  if (!text) {
    return ''
  }
  // @ts-ignore
  return <Icon icon={'ant-design:' + text}/>;
}
