(function (global) {
  const WINDOW = 10;
  const LS_KEY = "ps-practice-stats";
  const SAVE_API = "/api/practice-stats";

  let dataUrl = "data/practice-stats.json";
  let cache = null;
  let saveTimer = null;
  let initPromise = null;
  let initGen = 0;

  function canUseHttp() {
    return location.protocol === "http:" || location.protocol === "https:";
  }

  function emptyStore() {
    return { nodes: {} };
  }

  function rate(recent) {
    if (!recent || !recent.length) return null;
    const ok = recent.reduce((s, x) => s + x, 0);
    return Math.round((ok / recent.length) * 100);
  }

  function readLS() {
    try {
      const raw = localStorage.getItem(LS_KEY);
      if (raw) return JSON.parse(raw);
    } catch (_) {}
    return null;
  }

  function writeLS(store) {
    try {
      localStorage.setItem(LS_KEY, JSON.stringify(store));
    } catch (_) {}
  }

  function mergeNodes(a, b) {
    if (!a) return b ? { total: b.total || 0, recent: [...(b.recent || [])] } : null;
    if (!b) return { total: a.total || 0, recent: [...(a.recent || [])] };
    const total = Math.max(a.total || 0, b.total || 0);
    let recent;
    if ((a.total || 0) > (b.total || 0)) recent = a.recent || [];
    else if ((b.total || 0) > (a.total || 0)) recent = b.recent || [];
    else recent = (b.recent?.length || 0) >= (a.recent?.length || 0) ? (b.recent || []) : (a.recent || []);
    return { total, recent: [...recent] };
  }

  function mergeStores(a, b) {
    const out = emptyStore();
    const nodesA = a?.nodes || {};
    const nodesB = b?.nodes || {};
    const keys = new Set([...Object.keys(nodesA), ...Object.keys(nodesB)]);
    keys.forEach(id => {
      const merged = mergeNodes(nodesA[id], nodesB[id]);
      if (merged) out.nodes[id] = merged;
    });
    return out;
  }

  function commitStore(store) {
    const merged = mergeStores(store, readLS() || emptyStore());
    cache = merged;
    writeLS(merged);
    return merged;
  }

  function migrateLegacy() {
    const legacy = emptyStore();
    let found = false;
    for (let i = localStorage.length - 1; i >= 0; i--) {
      const key = localStorage.key(i);
      if (!key || !key.startsWith("ps-prof-")) continue;
      found = true;
      try {
        const id = key.slice("ps-prof-".length);
        const item = JSON.parse(localStorage.getItem(key));
        legacy.nodes[id] = {
          total: item.total || 0,
          recent: Array.isArray(item.recent) ? item.recent : []
        };
      } catch (_) {}
      localStorage.removeItem(key);
    }
    return found ? legacy : null;
  }

  function flushSave() {
    if (!canUseHttp() || !cache) return;
    clearTimeout(saveTimer);
    saveTimer = null;
    const blob = new Blob([JSON.stringify(cache)], { type: "application/json" });
    if (navigator.sendBeacon) {
      navigator.sendBeacon(SAVE_API, blob);
    } else {
      fetch(SAVE_API, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cache),
        keepalive: true
      }).catch(() => {});
    }
  }

  function scheduleSave() {
    if (!canUseHttp()) return;
    clearTimeout(saveTimer);
    saveTimer = setTimeout(flushSave, 300);
  }

  function pullLocal() {
    const local = readLS();
    if (local) cache = mergeStores(cache || emptyStore(), local);
    if (!cache) cache = emptyStore();
    if (!cache.nodes) cache.nodes = {};
    return cache;
  }

  function ensureCache() {
    return pullLocal();
  }

  function syncFromRemote() {
    if (!canUseHttp()) return Promise.resolve(pullLocal());
    return fetch(dataUrl, { cache: "no-store" })
      .then(res => (res.ok ? res.json() : emptyStore()))
      .catch(() => emptyStore())
      .then(fileStore => {
        cache = mergeStores(fileStore, readLS() || emptyStore());
        commitStore(cache);
        return cache;
      });
  }

  function init(url) {
    if (url) dataUrl = url;
    if (initPromise) return initPromise;

    const gen = ++initGen;
    initPromise = (async () => {
      const legacy = migrateLegacy();
      cache = mergeStores(readLS() || emptyStore(), legacy || emptyStore());
      if (!canUseHttp()) {
        commitStore(cache);
        return cache;
      }
      try {
        const res = await fetch(dataUrl, { cache: "no-store" });
        if (gen !== initGen) return pullLocal();
        if (res.ok) {
          const fileStore = await res.json();
          cache = mergeStores(fileStore, readLS() || emptyStore());
        }
      } catch (_) {}
      if (gen !== initGen) return pullLocal();
      commitStore(cache);
      return cache;
    })();

    return initPromise;
  }

  function refresh() {
    pullLocal();
    return syncFromRemote();
  }

  function reload() {
    return refresh();
  }

  function get(nodeId) {
    const n = ensureCache().nodes[nodeId];
    if (!n) return { total: 0, proficiency: null };
    return { total: n.total || 0, proficiency: rate(n.recent) };
  }

  function format(nodeId) {
    const s = get(nodeId);
    if (!s.total) return "";
    const p = s.proficiency == null ? "—" : s.proficiency + "%";
    return `${s.total}题 · ${p}`;
  }

  function record(nodeId, correct) {
    const store = ensureCache();
    const n = store.nodes[nodeId] || { total: 0, recent: [] };
    n.total += 1;
    n.recent.push(correct ? 1 : 0);
    if (n.recent.length > WINDOW) n.recent.shift();
    store.nodes[nodeId] = n;
    commitStore(store);
    scheduleSave();
    return get(nodeId);
  }

  function nodeId(customId) {
    if (customId) return customId;
    const q = new URLSearchParams(location.search).get("node");
    if (q) return q;
    const path = location.pathname.split("/").pop() || "app";
    return path.replace(/\.html$/, "");
  }

  const listeners = new Set();
  let visHook = false;

  function notify() {
    listeners.forEach(fn => {
      try { fn(); } catch (_) {}
    });
  }

  if (!global._psStorageHook) {
    global._psStorageHook = true;
    window.addEventListener("storage", e => {
      if (e.key !== LS_KEY) return;
      pullLocal();
      notify();
    });
    window.addEventListener("beforeunload", flushSave);
    window.addEventListener("pagehide", flushSave);
  }

  function create(customId) {
    const key = nodeId(customId);
    return {
      id: key,
      record(correct) {
        return record(key, correct);
      },
      stats() {
        return get(key);
      },
      bind(totalEl, profEl) {
        const render = () => {
          pullLocal();
          const s = this.stats();
          if (totalEl) totalEl.textContent = s.total;
          if (profEl) profEl.textContent = s.proficiency == null ? "—" : s.proficiency + "%";
        };
        listeners.add(render);
        render();
        if (!visHook) {
          visHook = true;
          document.addEventListener("visibilitychange", () => {
            if (document.visibilityState !== "visible") return;
            refresh().then(render);
          });
          window.addEventListener("pageshow", e => {
            if (!e.persisted) return;
            initPromise = null;
            init(dataUrl).then(render);
          });
        }
        return {
          render,
          destroy() { listeners.delete(render); }
        };
      }
    };
  }

  global.PracticeStats = {
    WINDOW,
    init,
    reload,
    refresh,
    get,
    format,
    record,
    create,
    nodeId
  };
  global.Proficiency = { create, nodeId, WINDOW };
})(window);
