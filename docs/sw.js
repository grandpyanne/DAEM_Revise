// Offline support: every file of the app is cached on the first visit, so it works without
// internet afterwards. tools/build_web.py rewrites VERSION and FILES; don't edit them by hand.
const VERSION = "42c545824f";
const FILES = ["./", "app.js", "data/content.json", "data/exams.json", "data/sketches.json", "figures/ec_2017_f1.png", "figures/ec_2024_f1.png", "figures/ec_2024_f2.png", "figures/ec_2024_f3.png", "figures/ec_2024_f4.png", "figures/ec_2024_f5.png", "figures/ec_2024_f6.png", "figures/ec_2024_f7.png", "figures/ec_2024_f8.png", "figures/ec_2025_f1.png", "figures/ec_2025_f2.png", "figures/ec_2025_f3.png", "figures/ec_2025_f4.png", "figures/ec_2025_f5.png", "figures/ec_2025_f6.png", "figures/ec_2025_f7.png", "figures/ec_2025_t1.png", "figures/ec_ttl_f2.png", "figures/mt_2017_f2.png", "figures/mt_2018_f1.png", "figures/mt_2018_f2.png", "figures/mt_2018_f3.png", "figures/mt_2019_f1.png", "figures/mt_2022_f2.png", "figures/mt_2022_f3.png", "figures/mt_2023_f1.png", "figures/mt_2023_f2.png", "figures/mt_2023_f3.png", "figures/mt_2023_f4.png", "figures/mt_2024_f1.png", "figures/mt_2026m_f1.png", "figures/mt_2026m_f2.png", "figures/mt_a2024_f1.png", "figures/mt_n2019_f2.png", "figures/mt_n2021_f1.png", "figures/mt_n2022_f1.png", "figures/mt_n2023_f1.png", "icon-180.png", "icon-192.png", "icon-512.png", "img/hot_spring.svg", "img/ic_launcher.svg", "img/mascot_happy.svg", "img/mascot_sleepy.svg", "index.html", "manifest.webmanifest", "sketch.js", "style.css"];
const CACHE = "daem-revise-" + VERSION;

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE).then((c) => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()),
  );
});

// Cache first (fast and offline); anything not cached yet is fetched and kept.
self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;
  event.respondWith(
    caches.match(event.request, { ignoreSearch: true }).then((hit) =>
      hit || fetch(event.request).then((res) => {
        if (res.ok && new URL(event.request.url).origin === location.origin) {
          const copy = res.clone();
          caches.open(CACHE).then((c) => c.put(event.request, copy));
        }
        return res;
      }),
    ),
  );
});
