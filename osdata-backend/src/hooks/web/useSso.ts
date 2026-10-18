// 单点登录核心类
import { getToken } from '/@/utils/auth';
import { getUrlParam } from '/@/utils';
import { useGlobSetting } from '/@/hooks/setting';
import { validateCasLogin } from '/@/api/sys/user';
import { useUserStore } from '/@/store/modules/user';
const globSetting = useGlobSetting();
const openSso = globSetting.openSso;

/**
 * 获取 CAS 登录回调地址，保留当前页面路径并移除 CAS 追加的 ticket 参数
 * for: 【issues/9797】开启CAS验证，回跳地址错误
 */
export function getCasServiceUrl() {
  const serviceUrl = new URL(window.location.href);
  serviceUrl.searchParams.delete('ticket');
  return serviceUrl.href;
}

export function useSso() {
  // 代码逻辑说明: 【QQYUN-7805】SSO登录强制用http #957---
  let locationUrl = document.location.protocol +"//" + window.location.host + '/';

  /**
   * 单点登录
   */
  async function ssoLogin() {
    if (openSso == 'true') {
      let token = getToken();
      let ticket = getUrlParam('ticket');
      if (!token) {
        const loginServiceUrl = getCasServiceUrl();
        if (ticket) {
          await validateCasLogin({
            ticket: ticket,
            service: loginServiceUrl,
          }).then((res) => {
            const userStore = useUserStore();
            userStore.setToken(res.token);
            return userStore.afterLoginAction(true, {});
          });
        } else {
          window.location.href = globSetting.casBaseUrl + '/login?service=' + encodeURIComponent(loginServiceUrl);
        }
      }
    }
  }

  /**
   * 退出登录
   */
  async function ssoLoginOut() {
    window.location.href = globSetting.casBaseUrl + '/logout?service=' + encodeURIComponent(locationUrl);
  }
  return { ssoLogin, ssoLoginOut };
}
