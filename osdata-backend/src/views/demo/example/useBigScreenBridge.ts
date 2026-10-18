import { ref, onBeforeUnmount } from 'vue';

const PROTOCOL = 'bigscreen-bridge';
const VERSION = 1;

export function useBigScreenBridge(iframeRef, options) {
  const channel = options.channel ?? 'default';
  const ready = ref(false);
  const components = ref([]);
  const designSize = ref(null);
  const handlers = new Map();

  function on(type, fn) {
    if (!handlers.has(type)) handlers.set(type, new Set());
    handlers.get(type).add(fn);
    return () => handlers.get(type)?.delete(fn);
  }

  function onMessage(e) {
    const msg = e.data;
    if (!msg || msg.protocol !== PROTOCOL) return;
    if (msg.channel !== channel) return;
    if (msg.source !== 'bridge') return;

    if (msg.type === 'bridge:ready') {
      const p = msg.payload || {};
      designSize.value = p.designSize || null;
      components.value = Array.isArray(p.components) ? p.components : [];
      ready.value = true;
    }

    handlers.get(msg.type)?.forEach((fn) => fn(msg.payload, msg));
    handlers.get('*')?.forEach((fn) => fn(msg.payload, msg));
  }

  function send(type, payload) {
    const win = iframeRef.value?.contentWindow;
    if (!win) return;
    win.postMessage(
      { protocol: PROTOCOL, version: VERSION, channel, source: 'host', type, payload, ts: Date.now() },
      '*'
    );
  }

  window.addEventListener('message', onMessage);
  onBeforeUnmount(() => {
    window.removeEventListener('message', onMessage);
    handlers.clear();
  });

  return {
    ready,
    components,
    designSize,
    on,
    send,
    sendComponentUpdate: (componentId, data) => send('host:component-update', { componentId, data }),
  };
}
