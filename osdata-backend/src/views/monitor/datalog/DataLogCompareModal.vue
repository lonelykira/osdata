<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" title="数据版本对比" width="60%" destroyOnClose :showOkBtn="false">
      <!-- 版本选择区 -->
      <div class="compare-header">
        <div class="compare-header__info">
          <span class="compare-header__label">数据表：</span>
          <a-tag color="blue">{{ dataTable }}</a-tag>
          <span class="compare-header__label" style="margin-left: 16px">数据ID：</span>
          <span class="compare-header__id">{{ dataId }}</span>
        </div>
        <div class="compare-header__selector">
          <span class="compare-header__label">版本对比：</span>
          <a-select
            placeholder="选择版本"
            @change="handleChange1"
            v-model:value="params.dataId1"
            style="width: 120px"
          >
            <a-select-option v-for="log in dataVersionList" :key="log.value" :value="log.value">
              V{{ log.text }}
            </a-select-option>
          </a-select>
          <span class="compare-header__vs">VS</span>
          <a-select
            placeholder="选择版本"
            @change="handleChange2"
            v-model:value="params.dataId2"
            style="width: 120px"
          >
            <a-select-option v-for="log in dataVersionList" :key="log.value" :value="log.value">
              V{{ log.text }}
            </a-select-option>
          </a-select>
        </div>
      </div>

      <!-- 差异统计 -->
      <div class="compare-stats" v-if="dataSource.length > 0">
        <a-tag color="red">{{ diffCount }} 处差异</a-tag>
        <a-tag color="green">{{ dataSource.length - diffCount }} 处相同</a-tag>
        <span class="compare-stats__total">共 {{ dataSource.length }} 个字段</span>
      </div>

      <!-- 对比表格 -->
      <div class="compare-table" v-if="isUpdate">
        <table class="compare-table__inner">
          <thead>
            <tr>
              <th class="col-field">字段名</th>
              <th class="col-value">
                <span class="version-tag version-tag--left">V{{ dataVersion1Num }}</span>
              </th>
              <th class="col-status"></th>
              <th class="col-value">
                <span class="version-tag version-tag--right">V{{ dataVersion2Num }}</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in dataSource" :key="idx" :class="{ 'row-diff': row.isDiff, 'row-same': !row.isDiff }">
              <td class="col-field">
                <span class="field-name">{{ row.code }}</span>
              </td>
              <td class="col-value" :class="{ 'cell-diff': row.isDiff }">
                <span class="cell-text">{{ formatValue(row.dataVersion1) }}</span>
              </td>
              <td class="col-status">
                <span v-if="row.isDiff" class="diff-icon">≠</span>
                <span v-else class="same-icon">=</span>
              </td>
              <td class="col-value" :class="{ 'cell-diff': row.isDiff }">
                <span class="cell-text">{{ formatValue(row.dataVersion2) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, ref, reactive, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { queryCompareList, queryDataVerList } from './datalog.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'DataLogCompareModal',
    components: {
      BasicModal,
    },
    emits: ['register', 'btnOk'],
    setup() {
      const { createMessage } = useMessage();
      const dataSource = ref<any[]>([]);
      const dataVersion1Num = ref('');
      const dataVersion2Num = ref('');
      const isUpdate = ref(true);
      const dataId = ref('');
      const dataTable = ref('');
      const dataVersionList = ref<any[]>([]);
      const params = reactive({ dataId1: '', dataId2: '' });

      const diffCount = computed(() => dataSource.value.filter((r) => r.isDiff).length);

      const [register, { setModalProps }] = useModalInner(async (data) => {
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          const checkedRows = data.selectedRows;
          dataTable.value = checkedRows[0].dataTable;
          dataId.value = checkedRows[0].dataId;
          params.dataId1 = checkedRows[0].id;
          params.dataId2 = checkedRows[1].id;
          await initDataVersionList();
          await initTableData();
        }
      });

      async function initTableData() {
        queryCompareList(unref(params)).then((res) => {
          dataVersion1Num.value = res[0].dataVersion;
          dataVersion2Num.value = res[1].dataVersion;
          const json1 = JSON.parse(res[0].dataContent);
          const json2 = JSON.parse(res[1].dataContent);
          // 收集所有字段（兼顾两边都有的和只有一边有的）
          const allKeys = new Set([...Object.keys(json1), ...Object.keys(json2)]);
          const data: any[] = [];
          allKeys.forEach((fieldKey) => {
            const v1 = json1[fieldKey] ?? '';
            const v2 = json2[fieldKey] ?? '';
            data.push({
              code: fieldKey,
              dataVersion1: v1,
              dataVersion2: v2,
              isDiff: String(v1) !== String(v2),
            });
          });
          // 差异项排前面
          data.sort((a, b) => (a.isDiff === b.isDiff ? 0 : a.isDiff ? -1 : 1));
          dataSource.value = data;
        });
      }

      function handleChange1(value) {
        if (params.dataId2 == value) {
          createMessage.warning('相同版本号不能比较');
          return;
        }
        params.dataId1 = value;
        initTableData();
      }

      function handleChange2(value) {
        if (params.dataId1 == value) {
          createMessage.warning('相同版本号不能比较');
          return;
        }
        params.dataId2 = value;
        initTableData();
      }

      async function initDataVersionList() {
        queryDataVerList({ dataTable: dataTable.value, dataId: dataId.value }).then((res) => {
          dataVersionList.value = res.map((value) => ({
            text: value['dataVersion'],
            value: value['id'],
          }));
        });
      }

      function formatValue(val) {
        if (val === null || val === undefined || val === '') return '--';
        return String(val);
      }

      return {
        dataSource,
        isUpdate,
        dataVersionList,
        dataVersion1Num,
        dataVersion2Num,
        register,
        handleChange1,
        handleChange2,
        params,
        dataTable,
        dataId,
        diffCount,
        formatValue,
      };
    },
  });
</script>
<style lang="less" scoped>
  .compare-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 6px;
    margin-bottom: 12px;
    flex-wrap: wrap;
    gap: 8px;

    &__info {
      display: flex;
      align-items: center;
    }

    &__label {
      font-size: 13px;
      color: #8c8c8c;
      white-space: nowrap;
    }

    &__id {
      font-size: 12px;
      color: #595959;
      font-family: 'Consolas', 'Monaco', monospace;
      word-break: break-all;
    }

    &__selector {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    &__vs {
      font-weight: 600;
      color: #faad14;
      font-size: 14px;
    }
  }

  .compare-stats {
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 8px;

    &__total {
      font-size: 12px;
      color: #8c8c8c;
      margin-left: 4px;
    }
  }

  .compare-table {
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    overflow: hidden;

    &__inner {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;

      thead {
        tr {
          background: #fafafa;
        }

        th {
          padding: 10px 12px;
          font-weight: 500;
          color: #595959;
          border-bottom: 1px solid #f0f0f0;
          text-align: left;
        }
      }

      tbody {
        tr {
          transition: background 0.2s;

          &:hover {
            background: #fafafa;
          }

          &:not(:last-child) td {
            border-bottom: 1px solid #f5f5f5;
          }
        }

        td {
          padding: 8px 12px;
          color: #333;
          vertical-align: top;
        }
      }
    }
  }

  .col-field {
    width: 140px;
    min-width: 120px;
  }

  .col-value {
    width: 40%;
  }

  .col-status {
    width: 36px;
    text-align: center !important;
  }

  .field-name {
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 12px;
    color: #1890ff;
  }

  .cell-text {
    word-break: break-all;
    font-size: 12px;
    line-height: 1.5;
  }

  .row-diff {
    .field-name {
      font-weight: 600;
    }
  }

  .cell-diff {
    background: #fff7e6;

    .cell-text {
      color: #d46b08;
      font-weight: 500;
    }
  }

  .diff-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: #fff1f0;
    color: #ff4d4f;
    font-size: 12px;
    font-weight: 700;
  }

  .same-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: #f6ffed;
    color: #52c41a;
    font-size: 12px;
    font-weight: 700;
  }

  .version-tag {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;

    &--left {
      background: #e6f7ff;
      color: #1890ff;
    }

    &--right {
      background: #f6ffed;
      color: #52c41a;
    }
  }
</style>
