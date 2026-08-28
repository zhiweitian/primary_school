(function () {
  if (!("serviceWorker" in navigator)) return;
  if (/PrimarySchoolTablet/.test(navigator.userAgent)) return;
  const link = document.querySelector('link[rel="manifest"]');
  if (!link) return;
  const base = new URL(".", link.href);
  navigator.serviceWorker.register(new URL("sw.js", base));
})();
