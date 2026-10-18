import {router} from "@/router";

import {routerBeforeEach, SHARE_LOGIN_ROUTE, SHARE_ROUTE} from "./route";

export function register() {
  router.addRoute(SHARE_LOGIN_ROUTE);
  router.addRoute(SHARE_ROUTE);

  router.beforeEach(routerBeforeEach);
}
