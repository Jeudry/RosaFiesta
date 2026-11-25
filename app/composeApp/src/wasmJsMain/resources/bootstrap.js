/**
 * Initializes runtime API override and registers the RosaFiesta service worker.
 */
window.__ROSAFIESTA_API_URL = window.__ROSAFIESTA_API_URL || "";

if ("serviceWorker" in navigator) {
    window.addEventListener("load", function () {
        navigator.serviceWorker.register("./sw.js").catch(function () {});
    });
}
