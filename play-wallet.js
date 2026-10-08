(function (global) {
  const LS_KEY = "ps-play-wallet";
  const COST = 40;

  function round(n) {
    return Math.round(n * 4) / 4;
  }

  function load() {
    try {
      const s = JSON.parse(localStorage.getItem(LS_KEY));
      if (s && typeof s.balance === "number") return s;
    } catch (_) {}
    return { balance: 0 };
  }

  function save(s) {
    localStorage.setItem(LS_KEY, JSON.stringify(s));
    try {
      global.dispatchEvent(new CustomEvent("ps-wallet", { detail: s.balance }));
    } catch (_) {}
    const b = global.PrimarySchool;
    if (b && typeof b.onWallet === "function") {
      try { b.onWallet(JSON.stringify(s)); } catch (_) {}
    }
  }

  const PLAY_MS = 10 * 60 * 1000;
  const PLAY_LS = "ps-play-until";
  const STARTS_LS = "ps-play-starts";

  function loadStarts() {
    try {
      const a = JSON.parse(localStorage.getItem(STARTS_LS) || "[]");
      return Array.isArray(a) ? a.filter(x => typeof x === "number") : [];
    } catch (_) {
      return [];
    }
  }

  function logStart() {
    const a = loadStarts();
    a.push(Date.now());
    if (a.length > 200) a.splice(0, a.length - 200);
    try { localStorage.setItem(STARTS_LS, JSON.stringify(a)); } catch (_) {}
  }

  function playUntil() {
    try { return Number(localStorage.getItem(PLAY_LS)) || 0; } catch (_) { return 0; }
  }

  function playLeftMs() {
    return Math.max(0, playUntil() - Date.now());
  }

  function emitPlay() {
    try { global.dispatchEvent(new Event("ps-play")); } catch (_) {}
  }

  const PlayWallet = {
    COST,
    PLAY_MS,
    get() {
      return load().balance || 0;
    },
    add(delta) {
      const n = Number(delta);
      if (!(n > 0)) return this.get();
      const s = load();
      s.balance = round((s.balance || 0) + n);
      save(s);
      return s.balance;
    },
    spend(cost) {
      const need = Number(cost) || COST;
      const s = load();
      if ((s.balance || 0) + 1e-9 < need) return -1;
      s.balance = round((s.balance || 0) - need);
      save(s);
      return s.balance;
    },
    set(balance) {
      const s = { balance: round(Math.max(0, Number(balance) || 0)) };
      save(s);
      return s.balance;
    },
    playLeftMs,
    isPlayActive() {
      return playLeftMs() > 0;
    },
    starts() {
      return loadStarts();
    },
    tryPlay() {
      if (playLeftMs() > 0) return false;
      const n = this.spend(this.COST);
      if (n < 0) return false;
      logStart();
      try { localStorage.setItem(PLAY_LS, String(Date.now() + PLAY_MS)); } catch (_) {}
      emitPlay();
      const b = global.PrimarySchool;
      if (b && typeof b.startPlay === "function") {
        try { b.startPlay(); } catch (_) {}
      }
      return true;
    }
  };

  global.PlayWallet = PlayWallet;

  const stats = global.PracticeStats;
  if (stats && typeof stats.create === "function") {
    const origCreate = stats.create;
    stats.create = function (id) {
      const p = origCreate(id);
      const origRecord = p.record.bind(p);
      p.record = function (correct) {
        const r = origRecord(correct);
        if (correct) {
          const d = global.WALLET_POINTS != null ? Number(global.WALLET_POINTS) : 1;
          if (d > 0) PlayWallet.add(d);
        }
        return r;
      };
      return p;
    };
  }
})(window);
