<script setup>
/**
 * 国家（地区）选择弹窗组件
 * 关键词搜索 + 勾选国家，勾选后回传给父组件
 * 供数据查询页使用
 */
import { ref, computed, watch } from "vue";
import { message } from "ant-design-vue";
import { getCountryTree } from "@/api/wdi";

// 单次数据查询支持的国家数量上限（与后端 /webapi/wdi/data/query 校验一致）
const MAX_COUNTRY_COUNT = 10;

const props = defineProps({
  open: { type: Boolean, default: false }, // 弹窗显隐
  selected: { type: Array, default: () => [] }, // 已选国家代码数组
});
const emit = defineEmits(["update:open", "confirm"]);

// ===== 数据 =====
const allCountries = ref([]); // 全量国家列表（一次性加载）
const loading = ref(false);
const keyword = ref(""); // 搜索关键词
const checkedMap = ref({}); // 勾选状态 { code: countryObject }

// 弹窗打开时初始化
watch(
  () => props.open,
  async (val) => {
    if (!val) return;
    const map = {};
    props.selected.forEach((item) => {
      const obj = typeof item === "string" ? { code: item, name: item } : item;
      map[obj.code] = obj;
    });
    checkedMap.value = map;

    if (!allCountries.value.length) {
      loading.value = true;
      try {
        const tree = (await getCountryTree()) || [];
        // 分组树拍平为全量国家列表：name=中文名(主要显示), nameEn=英文简称(列表中英双语展示)
        allCountries.value = tree.flatMap((region) =>
          (region.children || []).map((c) => ({
            code: c.countryCode,
            name: c.countryNameCn || c.shortName,
            nameEn: c.shortName || c.countryNameCn,
          })),
        );
      } finally {
        loading.value = false;
      }
    }
  },
);

// 按关键词过滤国家
const filteredCountries = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) return allCountries.value;
  return allCountries.value.filter(
    (c) =>
      (c.name || "").toLowerCase().includes(kw) ||
      (c.nameEn || "").toLowerCase().includes(kw) ||
      (c.code || "").toLowerCase().includes(kw),
  );
});

// 已选国家列表
const checkedList = computed(() => Object.values(checkedMap.value));

// 勾选/取消勾选
function toggleCheck(country, checked) {
  const code = country.code || country.countryCode;
  if (checked) {
    checkedMap.value[code] = {
      code,
      name: country.name || country.countryNameCn || country.shortName,
    };
  } else {
    delete checkedMap.value[code];
  }
}

// 判断是否已勾选
function isChecked(country) {
  const code = country.code || country.countryCode;
  return !!checkedMap.value[code];
}

// 全选当前过滤结果（超过单次查询上限时截断并提示）
function selectFiltered() {
  const list = filteredCountries.value.slice(0, MAX_COUNTRY_COUNT);
  list.forEach((c) => toggleCheck(c, true));
  if (filteredCountries.value.length > MAX_COUNTRY_COUNT) {
    message.warning(
      `单次查询最多支持 ${MAX_COUNTRY_COUNT} 个国家，已自动截取前 ${MAX_COUNTRY_COUNT} 个`,
    );
  }
}

// 清空已选
function clearAll() {
  checkedMap.value = {};
}

// 确认
function handleConfirm() {
  emit("confirm", checkedList.value);
  emit("update:open", false);
}

// 关闭
function handleClose() {
  emit("update:open", false);
}
</script>

<template>
  <a-modal
    :open="open"
    title="选择国家 / 地区"
    width="900px"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <div class="country-body">
        <!-- 顶部操作区 -->
        <div class="toolbar">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索国家名称或代码，如 中国 / CHN"
            allow-clear
          />
          <a-button size="small" @click="selectFiltered">全选当前结果</a-button>
        </div>

        <!-- 国家网格列表 -->
        <div class="country-grid">
          <div
            v-for="c in filteredCountries"
            :key="c.code || c.countryCode"
            class="country-item"
          >
            <a-checkbox
              :checked="isChecked(c)"
              @change="(e) => toggleCheck(c, e.target.checked)"
            >
              {{ c.name }}
              <span class="country-en" v-if="c.nameEn && c.nameEn !== c.name">{{
                c.nameEn
              }}</span>
            </a-checkbox>
          </div>
          <a-empty
            v-if="!filteredCountries.length"
            description="未找到匹配的国家"
          />
        </div>
      </div>
    </a-spin>

    <!-- 底部：已选回显 -->
    <template #footer>
      <div class="picker-footer">
        <div class="checked-tags">
          <a-tag
            v-for="item in checkedList"
            :key="item.code"
            closable
            color="blue"
            @close="toggleCheck(item, false)"
          >
            {{ item.name }}
          </a-tag>
          <span v-if="!checkedList.length" class="empty-tip">尚未选择国家</span>
        </div>
        <div class="footer-btns">
          <a-button @click="clearAll">清空</a-button>
          <a-button @click="handleClose">取消</a-button>
          <a-button type="primary" @click="handleConfirm">
            确定（{{ checkedList.length }}）
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<style lang="scss" scoped>
.country-body {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .toolbar {
    display: flex;
    gap: 8px;
  }

  // 国家网格（三列，弹窗加宽后利用横向空间）
  .country-grid {
    height: 360px;
    overflow-y: auto;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 4px 12px;

    .country-item {
      padding: 4px 2px;

      // 英文名: 小号浅色, 跟在中文名后同行展示
      .country-en {
        margin-left: 6px;
        font-size: 12px;
        color: $text-color-tertiary;
      }
    }
  }
}

// 底部回显区
.picker-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .checked-tags {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    max-height: 44px;
    overflow-y: auto;

    // 扁平紧凑标签：清除默认 margin-bottom(行距撑高根因)
    :deep(.ant-tag) {
      margin: 0;
      padding: 0 6px;
      font-size: 12px;
      line-height: 18px;
      border-radius: 2px;
    }

    .empty-tip {
      font-size: 12px;
      color: $text-color-tertiary;
    }
  }

  .footer-btns {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}
</style>
