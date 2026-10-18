<template>
  <div class="root">
    <!-- 第 1 层：宿主背景 -->
    <div class="bg-layer"></div>
    <!-- 第 2 层：大屏 iframe -->
    <iframe ref="iframeRef" class="bigscreen-iframe" :src="iframeSrc" allowtransparency="true" frameborder="0"></iframe>
    <!-- 第 3 层：宿主交互层（PE:none 容器 + PE:auto 子元素） -->
    <div class="host-overlay">
      <main class="host-main">
        <section class="panel">
          <div class="panel-title">控制大屏（host:component-update → queryData）</div>
          <div class="panel-body">
            <div v-if="!components.length" class="empty">等大屏 ready 后这里会出现组件按钮...</div>
            <div v-for="c in components" :key="c.id" class="comp-row">
              <div class="comp-meta">
                <span class="comp-type">{{ c.type }}</span>
                <span class="comp-id" :title="c.id">#{{ c.id.slice(0, 8) }}</span>
              </div>
              <div class="comp-actions">
                <button class="btn" @click="pushSampleData(c)">塞示例数据</button>
                <button class="btn ghost" @click="pushRandomData(c)">塞随机数据</button>
                <button class="btn ghost" @click="resetData(c)">重新查询</button>
              </div>
            </div>
          </div>
        </section>

        <section class="panel log-panel">
          <div class="panel-title">事件日志</div>
          <div class="panel-body log">
            <div v-for="(log, idx) in logs" :key="idx" class="log-item" :class="log.dir">
              <span class="log-dir">{{ log.dir === 'in' ? '⬅' : '➡' }}</span>
              <span class="log-type">{{ log.type }}</span>
              <span class="log-payload">{{ log.payload }}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { useBigScreenBridge } from './useBigScreenBridge';
  import { getToken, getTenantId } from '/@/utils/auth';

  const iframeRef = ref();

  const CHANNEL = 'demo-overlay';
  const iframeSrc = computed(() => {
    const token = getToken() || '';
    const tenantId = getTenantId() || '';
    return `${window._CONFIG['domianURL']}/drag/share/view/1212561321588641792?token=${token}&tenantId=${tenantId}&embedMode=1&channel=${CHANNEL}`;
  });
  
  const { ready, components, on, sendComponentUpdate } = useBigScreenBridge(iframeRef, { channel: CHANNEL });

  const logs = ref([]);
  function pushLog(dir, type, payload) {
    logs.value.unshift({ dir, type, payload: payload != null ? JSON.stringify(payload).slice(0, 200) : '-' });
    if (logs.value.length > 60) logs.value.pop();
  }

  on('bridge:ready', (p) => pushLog('in', 'bridge:ready', { components: (p?.components || []).length, designSize: p?.designSize }));
  on('bridge:click', (p) => pushLog('in', 'bridge:click', p));
  on('bridge:component-updated', (p) => pushLog('in', 'bridge:component-updated', p));

  function samplePayloadFor(c) {
    if (/Pie/i.test(c.type)) {
      return [
        { name: '华东', value: 320 },
        { name: '华北', value: 240 },
        { name: '华南', value: 180 },
        { name: '西南', value: 90 },
      ];
    }
    if (/3d/i.test(c.type)) {
      return [
        { name: 'Q1', value: 120 },
        { name: 'Q2', value: 200 },
        { name: 'Q3', value: 280 },
        { name: 'Q4', value: 340 },
      ];
    }
    return ['1月', '2月', '3月', '4月', '5月', '6月'].map((m, i) => ({ name: m, value: 100 + i * 60 }));
  }

  function pushSampleData(c) {
    const data = samplePayloadFor(c);
    sendComponentUpdate(c.id, data);
    pushLog('out', 'host:component-update', { componentId: c.id.slice(0, 8), type: c.type, data });
  }

  function pushRandomData(c) {
    const isPie = /Pie/i.test(c.type);
    const labels = isPie ? ['华东', '华北', '华南', '西北', '西南'] : ['1月', '2月', '3月', '4月', '5月', '6月'];
    const data = labels.map((name) => ({ name, value: Math.floor(50 + Math.random() * 950) }));
    sendComponentUpdate(c.id, data);
    pushLog('out', 'host:component-update', { componentId: c.id.slice(0, 8), type: c.type, data });
  }

  function resetData(c) {
    sendComponentUpdate(c.id, null);
    pushLog('out', 'host:component-update (reset)', { componentId: c.id.slice(0, 8) });
  }
</script>

<style lang="less" scoped>
  .root {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
  }

  /* z=0 背景 */
  .bg-layer {
    position: absolute;
    inset: 0;
    z-index: 0;
    background-image: url('https://fastly.picsum.photos/id/352/1920/1080.jpg?hmac=SXXWgAiZjzzAOVDRPx5YeJNDZnoSZkuCqWDBxDtIFFo');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }

  /* z=1 大屏 iframe（透明） */
  .bigscreen-iframe {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    background: transparent;
    z-index: 1;
    border: 0;
  }

  /* z=2 宿主交互层 */
  .host-overlay {
    position: absolute;
    inset: 0;
    z-index: 2;
    /* 容器自身不接收事件，避免大面积盖住下面 iframe 的可点区域 */
    pointer-events: none;
    display: grid;
    grid-template-columns: 26% 1fr 26%;
    grid-template-rows: auto 1fr;
    color: #e6f0ff;
  }

  .host-main {
    grid-column: 2 / 3;
    grid-row: 2 / 3;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-height: 0;
    /* 自身仍 PE:none，让 panel 这一层 auto；这样 host-main 的 padding 间隙不会拦截 iframe 区域 */
    pointer-events: none;
  }

  .panel {
    pointer-events: auto;
    background: rgba(7, 16, 32, 0.82);
    border: 1px solid rgba(110, 180, 255, 0.22);
    border-radius: 6px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    min-height: 0;
    &.log-panel {
      flex: 1 1 0;
      min-height: 200px;
    }
    .panel-title {
      padding: 8px 12px;
      font-size: 13px;
      font-weight: 600;
      letter-spacing: 0.5px;
      border-bottom: 1px solid rgba(110, 180, 255, 0.18);
    }
    .panel-body {
      padding: 10px 12px;
      overflow: auto;
    }
  }

  .empty {
    font-size: 12px;
    opacity: 0.6;
    padding: 6px 0;
  }

  .comp-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    padding: 5px 0;
    border-bottom: 1px dashed rgba(255, 255, 255, 0.06);
    &:last-child {
      border-bottom: none;
    }
    .comp-meta {
      display: flex;
      align-items: baseline;
      gap: 8px;
      .comp-type {
        font-size: 13px;
        font-weight: 600;
        color: #91d5ff;
      }
      .comp-id {
        font-size: 11px;
        opacity: 0.55;
        font-family: ui-monospace, Menlo, monospace;
      }
    }
    .comp-actions {
      display: flex;
      gap: 6px;
    }
  }

  .btn {
    border: 1px solid rgba(110, 180, 255, 0.3);
    background: rgba(24, 144, 255, 0.2);
    color: #fff;
    border-radius: 3px;
    padding: 3px 10px;
    font-size: 12px;
    cursor: pointer;
    &:hover {
      background: #1890ff;
      border-color: #1890ff;
    }
    &.ghost {
      background: transparent;
    }
  }

  .log {
    font-size: 12px;
    font-family: ui-monospace, Menlo, monospace;
  }
  .log-item {
    display: flex;
    gap: 8px;
    padding: 2px 0;
    .log-dir {
      width: 16px;
      flex-shrink: 0;
    }
    .log-type {
      width: 220px;
      flex-shrink: 0;
      opacity: 0.9;
    }
    .log-payload {
      flex: 1;
      opacity: 0.65;
      word-break: break-all;
    }
    &.in .log-dir {
      color: #52c41a;
    }
    &.out .log-dir {
      color: #1890ff;
    }
  }
</style>
