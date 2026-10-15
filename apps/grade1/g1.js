(function (g) {
  const AUTO_MS = 1400;
  const FRUIT = ["🍎", "🍊", "🍓", "🍇", "🍐", "🍉", "⭐", "🔵"];
  let prof, profUI, answered = false, timer = null, lastKey = "";

  function rand(a, b) {
    return a + Math.floor(Math.random() * (b - a + 1));
  }
  function pick(arr) {
    return arr[rand(0, arr.length - 1)];
  }
  function $(id) {
    return document.getElementById(id);
  }
  function fresh(make) {
    let q, n = 0;
    do { q = make(); n++; } while (q.key === lastKey && n < 14);
    lastKey = q.key;
    return q;
  }
  function fruits(n, mark) {
    const f = pick(FRUIT);
    let html = "";
    for (let i = 0; i < n; i++) html += `<span class="${mark && i >= mark ? "gone" : ""}">${f}</span>`;
    return html;
  }
  function dots(n, cls) {
    return "<i class='" + (cls || "on") + "'></i>".repeat(n) + "<i></i>".repeat(Math.max(0, 10 - n));
  }
  function frame(n, cls) {
    return `<div class="tf">${dots(Math.min(10, n), cls)}</div>`;
  }
  function sticks(n) {
    const t = Math.floor(n / 10), o = n % 10;
    return `<div class="sticks">
      <div class="tens">${"<i class='ten'></i>".repeat(t)}</div>
      <div class="ones-row">${"<i class='one'></i>".repeat(o)}</div>
    </div>`;
  }
  function needHint() {
    const s = prof.stats();
    if (!s.total || s.total < 8) return true;
    return s.proficiency == null || s.proficiency < 80;
  }
  function resetShell() {
    if (timer) { clearTimeout(timer); timer = null; }
    answered = false;
    const st = $("status");
    if (st) { st.textContent = ""; st.className = "status"; }
    const nb = $("nextBtn");
    if (nb) nb.classList.remove("show");
  }
  function finish(ok, tip, next) {
    if (answered) return;
    answered = true;
    document.querySelectorAll("button, input").forEach(el => {
      if (el.id === "nextBtn") return;
      el.disabled = true;
    });
    const nb = $("nextBtn");
    if (nb) nb.disabled = false;
    prof.record(ok);
    profUI.render();
    const st = $("status");
    if (ok) {
      st.textContent = "答对了！";
      st.className = "status ok";
      timer = setTimeout(next, AUTO_MS);
    } else {
      st.textContent = tip;
      st.className = "status err";
      if (nb) nb.classList.add("show");
    }
  }
  function choices(list, onPick) {
    return `<div class="choices">${list.map(x =>
      `<button type="button"${x.c ? ` class="${x.c}"` : ""} data-v="${x.v}">${x.t}</button>`
    ).join("")}</div>`;
  }
  function bindChoices(onPick) {
    document.querySelectorAll(".choices button, .shape-btn").forEach(b => {
      b.onclick = () => onPick(b.dataset.v);
    });
  }
  function pad(max, onPick) {
    const bits = [];
    for (let i = 0; i <= max; i++) bits.push(`<button type="button" data-n="${i}">${i}</button>`);
    $("pad").innerHTML = bits.join("");
    $("pad").querySelectorAll("button").forEach(b => {
      b.onclick = () => onPick(+b.dataset.n);
    });
  }
  function gcd(a, b) {
    while (b) [a, b] = [b, a % b];
    return a;
  }
  function frac(n, d) {
    return `<span class="frac"><span>${n}</span><span>${d}</span></span>`;
  }
  function fracIn() {
    return `<span class="frac in"><input inputmode="numeric" maxlength="3" autocomplete="off"><input inputmode="numeric" maxlength="3" autocomplete="off"></span>`;
  }
  function box(cls) {
    return `<input class="${cls || ""}" inputmode="decimal" autocomplete="off">`;
  }
  function vals() {
    return [...document.querySelectorAll("#card input")].map(el => el.value.trim());
  }
  function same(a, b) {
    const x = parseFloat(a);
    return a !== "" && !isNaN(x) && Math.abs(x - parseFloat(b)) < 1e-9;
  }
  function sameFrac(n, d, n2, d2) {
    return +d > 0 && +n * +d2 === +n2 * +d;
  }
  function bindOk(check) {
    const ok = () => {
      const v = vals();
      if (v.some(x => x === "")) return;
      check(v);
    };
    $("okBtn").onclick = ok;
    document.querySelectorAll("#card input").forEach(el => {
      el.addEventListener("keydown", e => { if (e.key === "Enter") ok(); });
    });
    const first = document.querySelector("#card input");
    if (first) first.focus();
  }
  function okBtn() {
    return `<div class="actions"><button class="btn wide" id="okBtn" type="button">确定</button></div>`;
  }
  function shuffle(a) {
    for (let i = a.length - 1; i > 0; i--) {
      const j = rand(0, i);
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }
  function bar(d, n, w, n2) {
    w = w || 240;
    const h = 34, cw = w / d;
    let s = `<svg viewBox="0 0 ${w + 4} ${h + 4}" width="${w + 4}" height="${h + 4}">`;
    for (let i = 0; i < d; i++) {
      const fill = i < n ? "#ffd166" : i < n + (n2 || 0) ? "#06d6a0" : "rgba(255,255,255,0.08)";
      s += `<rect x="${2 + i * cw}" y="2" width="${cw}" height="${h}" fill="${fill}" stroke="#a8dadc" stroke-width="2"/>`;
    }
    return s + "</svg>";
  }
  function boot(start) {
    $("nextBtn").onclick = start;
    PracticeStats.init("../../data/practice-stats.json").then(() => {
      prof = PracticeStats.create();
      profUI = prof.bind($("totalDone"), $("proficiency"));
      start();
    });
  }

  g.G1 = {
    rand, pick, fresh, $, fruits, frame, sticks, needHint,
    resetShell, finish, choices, bindChoices, pad, boot,
    gcd, frac, fracIn, box, vals, same, sameFrac, bindOk, okBtn, shuffle, bar
  };
})(window);
