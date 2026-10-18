import type { InjectionKey } from 'vue';
import type { Emitter } from '/@/utils/mitt';
import { createContext, useContext } from '/@/hooks/core/useContext';
import mitt from '/@/utils/mitt';
import {onMounted, onUnmounted} from 'vue'

export interface OnlineEmitterContextProps {
  /*activeName?: 'onlineEvent'*/
  onlineEmitter: Emitter
}
const key: InjectionKey<OnlineEmitterContextProps> = Symbol();

/**
 * 事件编码-打开弹窗
 */
export const EVENT_OPEN_CODE: string = 'openpopmodal';
/**
 * 事件编码-关闭弹窗，获取表单数据
 */
const EVENT_SUCCESS_CODE: string = 'successpopmodal';

/**
 * 设置弹框事件-online列表
 * @param callback
 */
export function useOnlineListPopEvent(callback){
  const emitter = mitt();
  function openPopModal(params){
    callback(params);
    console.log('事件触发完成,', params)
  }

  emitter.on(EVENT_OPEN_CODE, openPopModal)
/*  onUnmounted(()=>{
    emitter.off(EVENT_OPEN_CODE, openPopModal)
    console.log('事件解绑完成-createOpenPopModalEvent')
  });
  onMounted(()=>{
   
    console.log('事件绑完成-createOpenPopModalEvent')
  });*/
  createOnlineEventContext({
    onlineEmitter: emitter
  });
  console.log('事件绑完成,')
}

/**
 * 关闭弹窗，返回表单数据
 * @param params
 */
export function useOnlinePopFormEvent(){
  const { onlineEmitter } = useOnlineEventContext();
  function emitFormData(data){
    onlineEmitter.emit(EVENT_SUCCESS_CODE, data)
  }
  return {
    emitFormData
  }
}


/**
 * 关闭弹窗，返回表单数据
 * @param params
 */
export function useOnlineFormEvent(callback){
  const context = useOnlineEventContext();
  const { onlineEmitter } = context;
  function emitData(data){
    callback(data);
    console.log('useOnlineFormEvent事件触发完成,', data)
  }
  onUnmounted(()=>{
    onlineEmitter && onlineEmitter.off(EVENT_SUCCESS_CODE, emitData)
    console.log('事件解绑完成-createOpenPopModalEvent')
  });
  onMounted(()=>{
    onlineEmitter && onlineEmitter.on(EVENT_SUCCESS_CODE, emitData)
    console.log('事件绑完成-createOpenPopModalEvent')
  });
  function openPopModal(emitData){
    console.log('openPopModal', emitData)
    onlineEmitter && onlineEmitter.emit(EVENT_OPEN_CODE, emitData)
  }
  return {
    openPopModal
  }
}

/**
 * 触发弹框事件
 * @param params
 */
/*export function getOnlinePopEvent(){
  const { onlineEmitter } = useOnlineEventContext();
  return {
    onlineEmitter,
    eventCode: EVENT_OPEN_CODE
  };
}*/

function createOnlineEventContext(context: OnlineEmitterContextProps) {
  return createContext<OnlineEmitterContextProps>(context, key, { readonly: false, native: true });
}

export function useOnlineEventContext() {
  return useContext<OnlineEmitterContextProps>(key);
}