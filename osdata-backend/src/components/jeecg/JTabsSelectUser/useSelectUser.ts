import { defHttp } from '/@/utils/http/axios';
import { BasicColumn, FormSchema } from '/@/components/Table';

export enum Api {
  departList = '/sys/sysDepart/queryDepartTreeSync',
  userList = '/sys/user/list',
  departUserList = '/sys/user/queryUserByDepId',

  //获取所有岗位职级信息
  queryALLRankRelation = '/sys/sysDepart/getALLRankRelation',
  //根据关键字搜索部门
  searchBy = '/sys/sysDepart/searchBy'
}

/**
 * 获取部门树列表
 */
export const getDepartTreeData = (params?) => defHttp.get({ url: Api.departList, params });

/**
 * 获取用户列表
 */
export const getUserList = (params?) => defHttp.get({ url: Api.userList, params: { ...params, status: '1' } });

/**
 * 获取指定部门用户列表
 */
export const getDepartUserList = (params?) => defHttp.get({ url: Api.departUserList, params });
/**
 * 获取职级信息
 */
export const queryALLRankRelation = (params?) => defHttp.get({ url: Api.queryALLRankRelation, params,timeout: 2 * 60 * 1000 });

/**
 * 根据关键字搜索部门
 */
export const searchByKeywords = (params) => defHttp.get({ url: Api.searchBy, params });
/**
 * 用户列表
 */
export const columns: BasicColumn[] = [
  // {
  //   title: '用户账号',
  //   align: 'center',
  //   dataIndex: 'username',
  //   ellipsis: true,
  //   width: 130,
  // },
  {
    title: '用户姓名',
    align: 'center',
    width: 150,
    dataIndex: 'realname',
    ellipsis: true,
  },
  {
    title: '部门',
    align: 'center',
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
];

/**
 * 选中用户列表
 */
export const selectedUserColumns: BasicColumn[] = [
  {
    title: '用户姓名',
    align: 'center',
    width: 150,
    dataIndex: 'realname',
    ellipsis: true,
  },
];

/**
 * 查询条件
 */
export const searchFormSchema: FormSchema[] = [
  {
    label: '用户姓名',
    field: 'realname',
    component: 'Input',
    componentProps: {
      style: {
        width: '150px',
      },
    },
  },
  // {
  //   label: '用户账号',
  //   field: 'username',
  //   component: 'JInput',
  //   componentProps: {
  //     style: {
  //       width: '150px',
  //     },
  //   },
  // },
];
