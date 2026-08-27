const CACHE = "ps-v3";
const PRECACHE = [
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./pwa.js",
  "./icons/icon.svg",
  "./icons/icon-192.png",
  "./icons/icon-512.png",
  "./practice-stats-store.js",
  "./vendor/d3.min.js",
  "./data/practice-stats.json",
  "./knowledge-tree/index.html",
  "./knowledge-tree/grade1-math-priority-tree.js",
  "./knowledge-tree/grade2-math-priority-tree.js",
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
  "./apps/grade2/vertical-division.html",
  "./apps/grade2/read-write-within-yi.html",
  "./apps/grade2/vertical-multiplication.html",
  "./apps/grade2/length-units.html",
  "./apps/grade2/length-convert.html"
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
