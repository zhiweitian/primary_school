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

  const PlayWallet = {
    COST,
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
