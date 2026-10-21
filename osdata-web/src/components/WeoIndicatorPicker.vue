<script setup>
/**
 * WEO 指标选择弹窗组件
 * 按主题分组浏览 + 关键词搜索，勾选指标后回传给父组件
 * 供 WEO 数据查询页使用
 */
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getTopicTree, getIndicatorPage } from '@/api/weo'
import { debounce } from '@/utils/tools'

// 单次数据查询支持的指标数量上限（与后端 /webapi/weo/data/query 校验一致）
const MAX_INDICATOR_COUNT = 10

const props = defineProps({
  open: { type: Boolean, default: false }, // 弹窗显隐
  selected: { type: Array, default: () => [] } // 已选指标代码数组
})
const emit = defineEmits(['update:open', 'confirm'])

// ===== 数据 =====
const topics = ref([]) // 主题列表
const activeTopic = ref(null) // 当前激活主题
const indicatorList = ref([]) // 指标列表
const loading = ref(false) // 指标加载中
const keyword = ref('') // 搜索关键词
const checkedMap = ref({}) // 勾选状态 { code: indicatorObject }

// 弹窗打开时初始化数据
watch(
  () => props.open,
  async (val) => {
    if (!val) return
    // 同步外部已选指标到勾选状态
    const map = {}
    props.selected.forEach((item) => {
      const obj = typeof item === 'string' ? { code: item, name: item } : item
      map[obj.code] = obj
    })
    checkedMap.value = map

    if (!topics.value.length) {
      const res = (await getTopicTree()) || []
      // 主题树映射为选项结构：code=主题编码(topicCode)，name=中文名 + en=英文名
      topics.value = res.map((t) => ({
        code: t.topicCode,
        name: t.topicCn || t.topicEn || t.topicCode,
        en: t.topicEn || t.topicCode
      }))
      if (topics.value.length) activeTopic.value = topics.value[0]
    }
    fetchIndicators()
  }
)

// 查询指标列表（按主题 + 关键词，来自 /webapi/weo/indicator/page）
async function fetchIndicators() {
  loading.value = true
  try {
    const params = { pageNo: 1, pageSize: 50 }
    if (activeTopic.value) params.topicCode = activeTopic.value.code
    if (keyword.value) params.keyword = keyword.value
    const res = await getIndicatorPage(params)
    indicatorList.value = (res?.records || []).map((s) => ({
      code: s.indicatorCode,
      name: s.indicatorNameCn || s.indicatorName,
      unit: s.unit,
      scale: s.scale
    }))
  } finally {
    loading.value = false
  }
}

// 关键词防抖搜索
const onSearch = debounce(() => fetchIndicators(), 300)

// 切换主题
function switchTopic(topic) {
  activeTopic.value = topic
  fetchIndicators()
}

// 已选指标列表（弹窗底部回显）
const checkedList = computed(() => Object.values(checkedMap.value))

// 勾选/取消勾选指标（超过单次查询上限时拦截并提示）
function toggleCheck(indicator, checked) {
  const code = indicator.code || indicator.indicatorCode
  if (checked) {
    if (!checkedMap.value[code] && checkedList.value.length >= MAX_INDICATOR_COUNT) {
      message.warning(`单次查询最多支持 ${MAX_INDICATOR_COUNT} 个指标，请先取消部分已选指标`)
      return
    }
    checkedMap.value[code] = { code, name: indicator.name || indicator.indicatorName }
  } else {
    delete checkedMap.value[code]
  }
}

// 判断是否已勾选
function isChecked(indicator) {
  const code = indicator.code || indicator.indicatorCode
  return !!checkedMap.value[code]
}

// 清空已选
function clearAll() {
  checkedMap.value = {}
}

// 确认选择
function handleConfirm() {
  emit('confirm', checkedList.value)
  emit('update:open', false)
}

// 关闭弹窗
function handleClose() {
  emit('update:open', false)
}
</script>

<template>
  <a-modal
    :open="open"
    title="选择 WEO 指标"
    width="1080px"
    @cancel="handleClose"
  >
    <div class="picker-body">
      <!-- 左侧：主题列表 -->
      <div class="topic-panel">
        <div class="panel-title">指标主题</div>
        <div
          v-for="topic in topics"
          :key="topic.code"
          class="topic-item"
          :class="{ active: activeTopic && activeTopic.code === topic.code }"
          :title="topic.name"
          @click="switchTopic(topic)"
        >
          <span class="topic-name">{{ topic.name }}</span>
          <span class="topic-en">{{ topic.en }}</span>
        </div>
      </div>

      <!-- 右侧：指标搜索 + 列表 -->
      <div class="indicator-panel">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索指标名称或代码，如 GDP"
          allow-clear
          @input="onSearch"
        />
        <div class="indicator-list">
          <a-spin :spinning="loading">
            <div
              v-for="item in indicatorList"
              :key="item.code || item.indicatorCode"
              class="indicator-item"
            >
              <a-checkbox
                :checked="isChecked(item)"
                @change="(e) => toggleCheck(item, e.target.checked)"
              >
                <span class="indicator-name">{{ item.name || item.indicatorName }}</span>
                <span class="indicator-code">{{ item.code || item.indicatorCode }}</span>
                <span class="indicator-extra" v-if="item.unit || item.scale">
                  <template v-if="item.unit">单位：{{ item.unit }}</template>
                  <template v-if="item.unit && item.scale">｜</template>
                  <template v-if="item.scale">数量级：{{ item.scale }}</template>
                </span>
              </a-checkbox>
            </div>
            <a-empty v-if="!loading && !indicatorList.length" description="暂无指标" />
          </a-spin>
        </div>
      </div>
    </div>

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
          <span v-if="!checkedList.length" class="empty-tip">尚未选择指标</span>
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
.picker-body {
  display: flex;
  gap: 16px;
  height: 420px;
}

// 左侧主题面板
.topic-panel {
  width: 260px;
  border-right: 1px solid $border-color;
  padding-right: 12px;
  overflow-y: auto;

  .panel-title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
    color: $text-color-secondary;
  }

  .topic-item {
    padding: 6px 10px;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    color: $text-color-secondary;
    transition: all 0.2s;

    // 中文名 + 英文名两行展示, 各行超长省略号截断, 悬停 title 查看全名
    .topic-name {
      display: block;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .topic-en {
      display: block;
      margin-top: 1px;
      font-size: 11px;
      color: $text-color-tertiary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    &:hover {
      color: $primary-color;
      background: $primary-bg;
    }

    &.active {
      color: $primary-color;
      background: $primary-bg;
      font-weight: 500;
    }
  }
}

// 右侧指标面板
.indicator-panel {
  flex: 1;
  display: flex;
  flex-direction: column;

  .indicator-list {
    flex: 1;
    margin-top: 10px;
    overflow-y: auto;

    .indicator-item {
      padding: 6px 4px;

      .indicator-name {
        font-size: 13px;
      }

      .indicator-code {
        margin-left: 8px;
        font-size: 12px;
        color: $text-color-tertiary;
      }

      // 单位/数量级附加信息
      .indicator-extra {
        margin-left: 8px;
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
