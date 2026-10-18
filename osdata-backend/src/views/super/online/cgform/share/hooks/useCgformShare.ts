import {ref} from 'vue';
import {router} from "@/router";

import {
  SHARE_ADD_ROUTER_NAME,
  SHARE_DETAIL_ROUTER_NAME,
  SHARE_LOGIN__ROUTER_NAME,
  SHARE_UPDATE_ROUTER_NAME
} from "../route";
import {useShareStore} from "../store/shareStore";
import {useUserStore} from "@/store/modules/user";
import {getCgformById, getCgformRecordById} from "../share.api";
import {parseExtConfigJson} from "../../util/utils";

export function useCgformShare() {
  const userStore = useUserStore()
  const shareStore = useShareStore()

  const pageLoading = ref(true)
  const pageErrorTip = ref('')

  async function initCgformShare() {
    try {
      // 检查url中的token
      await shareStore.checkUrlToken()
      const route = router.currentRoute.value
      // 检查缓存中的token
      if (!userStore.getToken) {
        // 跳转到登录页
        router.push({
          name: SHARE_LOGIN__ROUTER_NAME,
          query: {
            redirect: encodeURIComponent(route.path),
          }
        });
        return
      }
      // 获取 Online表单 的信息
      const {id: formId} = route.params
      if (!formId) {
        pageErrorTip.value = '参数错误'
        return
      }
      let res = await getCgformById(formId as string);
      if (!res.success) {
        pageErrorTip.value = res.message
        return
      }
      const record = res.result
      // 判断是否开启了外部链接
      const extJson = parseExtConfigJson(record);
      if (!extJson?.enableExternalLink) {
        pageErrorTip.value = '当前表单未开启外部链接'
        return
      }
      // 判断是否支持当前操作
      let externalLinkActions = extJson.externalLinkActions.split(',');
      if (route.name === SHARE_ADD_ROUTER_NAME) {
        if (!externalLinkActions.includes('add')) {
          pageErrorTip.value = '当前表单不支持外部新增'
          return
        }
      } else if (route.name === SHARE_UPDATE_ROUTER_NAME) {
        if (!externalLinkActions.includes('edit')) {
          pageErrorTip.value = '当前表单不支持外部编辑'
          return
        }
      } else if (route.name === SHARE_DETAIL_ROUTER_NAME) {
        if (!externalLinkActions.includes('detail')) {
          pageErrorTip.value = '当前表单不支持外部详情'
          return
        }
      } else {
        pageErrorTip.value = '未知的页面';
        return;
      }

      // 判断表单类型
      if (record.tableType == 3) {
        pageErrorTip.value = '不支持附表外部链接';
        return;
      }

      shareStore.setCgformRecord(record);

      // 查询数据
      if (route.name === SHARE_UPDATE_ROUTER_NAME || route.name === SHARE_DETAIL_ROUTER_NAME) {
        const {dataId} = route.params;
        if (!dataId) {
          pageErrorTip.value = '参数错误'
          return
        }
        res = await getCgformRecordById(formId as string, dataId as string);
        if (!res.success) {
          pageErrorTip.value = res.message
          return
        }
        const dataRecord = res.result
        if (dataRecord?.id !== dataId) {
          pageErrorTip.value = '数据不存在或已删除'
          return
        }
        shareStore.setDataRecord(dataRecord);
      }

    } catch (e: any) {
      pageErrorTip.value = e?.message || e
      console.error(e)
    } finally {
      pageLoading.value = false
    }
  }

  return {
    pageLoading,
    pageErrorTip,

    initCgformShare,
  }
}