const CACHE = "ps-v49";
const PRECACHE = [
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./pwa.js",
  "./icons/icon.svg",
  "./icons/icon-192.png",
  "./icons/icon-512.png",
  "./practice-stats-config.js",
  "./practice-stats-store.js",
  "./play-wallet.js",
  "./knowledge-tree/index.html",
  "./knowledge-tree/math-tree.js",
  "./apps/grade1/g1.css",
  "./apps/grade1/g1.js",
  "./apps/grade1/num10.html",
  "./apps/grade1/add10.html",
  "./apps/grade1/num20.html",
  "./apps/grade1/carry.html",
  "./apps/grade1/borrow.html",
  "./apps/grade1/word20.html",
  "./apps/grade1/num100.html",
  "./apps/grade1/cmp100.html",
  "./apps/grade1/mental100.html",
  "./apps/grade1/written.html",
  "./apps/grade1/word100.html",
  "./apps/grade1/solid.html",
  "./apps/grade1/plane.html",
  "./apps/vertical-addition.html",
  "./apps/vertical-subtraction.html",
  "./apps/word-typing.html",
  "./apps/dino.html",
  "./apps/grade2/approx-describe-quantity.html",
  "./apps/grade2/estimate-calc.html",
  "./apps/grade2/compare-within-10000.html",
  "./apps/grade2/approx-number-basics.html",
  "./apps/grade2/approx-number-basics-steps.html",
  "./apps/grade2/multiplication-table.html",
  "./apps/grade2/multiplication-meaning.html",
  "./apps/grade2/multiplication-word.html",
  "./apps/grade2/division-meaning.html",
  "./apps/grade2/mul-div-relation.html",
  "./apps/grade2/division-word.html",
  "./apps/grade2/mixed-ops.html",
  "./apps/grade2/round-addsub.html",
  "./apps/grade2/length-word.html",
  "./apps/grade2/classify-stats.html",
  "./apps/grade2/vertical-division.html",
  "./apps/grade2/table-division.html",
  "./apps/grade2/read-write-within-yi.html",
  "./apps/grade2/vertical-multiplication.html",
  "./apps/grade2/length-units.html",
  "./apps/grade2/length-convert.html",
  "./apps/grade2/remainder-division.html",
  "./apps/grade2/clock.html",
  "./apps/grade3/mul-oral.html",
  "./apps/grade3/div-oral.html",
  "./apps/grade3/frac-intro.html",
  "./apps/grade3/frac-compare.html",
  "./apps/grade3/frac-addsub.html",
  "./apps/grade3/frac-of.html",
  "./apps/grade3/decimal-intro.html",
  "./apps/grade3/decimal-compare.html",
  "./apps/grade3/decimal-addsub.html",
  "./apps/grade3/length-mm-km.html",
  "./apps/grade3/mass-units.html",
  "./apps/grade3/time-calc.html",
  "./apps/grade3/perimeter.html",
  "./apps/grade3/area.html",
  "./apps/grade3/area-units.html",
  "./apps/grade3/angles.html",
  "./apps/grade4/angle-measure.html",
  "./apps/grade4/approx-big.html",
  "./apps/grade4/avg.html",
  "./apps/grade4/dec-addsub.html",
  "./apps/grade4/dec-meaning.html",
  "./apps/grade4/dec-shift.html",
  "./apps/grade4/laws-simplify.html",
  "./apps/grade4/laws.html",
  "./apps/grade4/order-ops.html",
  "./apps/grade4/parallel.html",
  "./apps/grade4/qty-model.html",
  "./apps/grade4/triangle.html",
  "./apps/grade5/area-para.html",
  "./apps/grade5/area-trap.html",
  "./apps/grade5/area-tri.html",
  "./apps/grade5/dec-div.html",
  "./apps/grade5/dec-mul.html",
  "./apps/grade5/div-rules.html",
  "./apps/grade5/equation.html",
  "./apps/grade5/factors.html",
  "./apps/grade5/frac-addsub.html",
  "./apps/grade5/frac-basic.html",
  "./apps/grade5/frac-dec.html",
  "./apps/grade5/frac-improper.html",
  "./apps/grade5/frac-meaning.html",
  "./apps/grade5/frac-reduce.html",
  "./apps/grade5/gcd-lcm.html",
  "./apps/grade5/letters.html",
  "./apps/grade5/prime.html",
  "./apps/grade5/surf-cuboid.html",
  "./apps/grade5/symmetry.html",
  "./apps/grade5/vol-cuboid.html",
  "./apps/grade6/circle-a.html",
  "./apps/grade6/circle-c.html",
  "./apps/grade6/cone.html",
  "./apps/grade6/cylinder.html",
  "./apps/grade6/direct-inverse.html",
  "./apps/grade6/frac-div.html",
  "./apps/grade6/frac-mul.html",
  "./apps/grade6/frac-word.html",
  "./apps/grade6/negative.html",
  "./apps/grade6/percent-app.html",
  "./apps/grade6/percent.html",
  "./apps/grade6/position.html",
  "./apps/grade6/proportion.html",
  "./apps/grade6/ratio-share.html",
  "./apps/grade6/ratio.html",
  "./apps/grade6/reciprocal.html",
  "./apps/grade6/scale.html",
  "./apps/grade2/img/length-kid.jpg",
  "./apps/grade2/img/length-door.jpg",
  "./apps/grade2/img/length-tree.jpg",
  "./apps/grade2/img/length-pencil.jpg",
  "./apps/grade2/img/length-eraser.jpg",
  "./apps/grade2/img/length-book.jpg",
  "./apps/grade2/img/length-ant.jpg",
  "./apps/grade2/img/length-finger.jpg"
];

self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE)
      .then(cache => cache.addAll(PRECACHE))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", event => {
  const req = event.request;
  if (req.method !== "GET") return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;

  event.respondWith(
    fetch(req).then(res => {
      if (res.ok) {
        const copy = res.clone();
        caches.open(CACHE).then(c => c.put(req, copy));
      }
      return res;
    }).catch(() => caches.match(req))
  );
});
