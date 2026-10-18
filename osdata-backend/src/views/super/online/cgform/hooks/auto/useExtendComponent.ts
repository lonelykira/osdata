import {add} from "/@/components/Form/src/componentMap";
import LinkTableSelect from '../../extend/linkTable/LinkTableSelect.vue';
import LinkTableCard from '../../extend/linkTable/LinkTableCard.vue';
import OnlineSelectCascade from '../../auto/comp/OnlineSelectCascade.vue';

const componentKeyMap = {};

/**
 * 用于往form中添加组件
 */
export function useExtendComponent() {

  addComponent('OnlineSelectCascade', OnlineSelectCascade);
  addComponent('LinkTableSelect', LinkTableSelect);
  addComponent('LinkTableCard', LinkTableCard);

  /**
   * 避免重复添加
   */
  function addComponent(key, comp) {
    if(!componentKeyMap[key]){
      add(key, comp)
      componentKeyMap[key] = 1;
    }
  }

  /**
   * 关联记录的查询控件不宜用卡片模式 统一使用下拉模式 
   */
  function linkTableCard2Select(schema) {
    if("LinkTableCard"==schema.component){
      schema.component = 'LinkTableSelect';
      schema.componentProps.popContainer = 'body';
    }
  }

  return {
    addComponent,
    linkTableCard2Select
  }
}