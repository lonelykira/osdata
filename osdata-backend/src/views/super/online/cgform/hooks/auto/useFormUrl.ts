import { ref, unref } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '/@/store/modules/user';
import { message } from 'ant-design-vue';

export function useFormUrl() {
  const route = useRoute();
  const token = ref(route.query.token);
  const createToken = ref('');
  const userStore = useUserStore();
  if (unref(token)) {
    // 校验token是否合法
  } else {
    if (userStore.getToken) {
      // 登录了系统弄，有了token
      token.value = userStore.getToken;
    } else {
      // 既没登录，url也没token
      getToken();
    }
  }
  // 没有token，通过接口去获取token，再缓存到本地并设置到url上重新渲染
  function getToken() {
    const hide = message.loading('获取token中...', 0);
    setTimeout(() => {
      createToken.value =
        'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTAwMTc2NzUsInVzZXJuYW1lIjoiYWRtaW4ifQ.0q5ywkLF144SaqumsSgEXO5ERtqaYp8jqouIUxxhELc';

      setToken();
      setUrlToken();
      hide();
    }, 3e3);
  }
  function setToken() {
    userStore.setToken(createToken.value);
    // userStore.setTenant(1);
  }
  function setUrlToken() {
    window.location.replace(`${route.fullPath}?token=${createToken.value}`);
  }
  return { token };
}
