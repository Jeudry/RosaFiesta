/**
 * RosaFiesta Web Push and notification click Service Worker.
 */

self.addEventListener("install", function (event) {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", function (event) {
  event.waitUntil(self.clients.claim());
});

self.addEventListener("push", function (event) {
  var payload = {
    title: "RosaFiesta",
    body: "Tienes una actualización nueva en tu evento.",
    url: "/",
    tag: "rosafiesta-alert"
  };

  if (event.data) {
    try {
      var parsed = event.data.json();
      if (parsed && typeof parsed === "object") {
        if (parsed.title) payload.title = String(parsed.title);
        if (parsed.body) payload.body = String(parsed.body);
        if (parsed.url) payload.url = String(parsed.url);
        if (parsed.tag) payload.tag = String(parsed.tag);
      }
    } catch (jsonErr) {
      var rawText = event.data.text();
      if (rawText) payload.body = rawText;
    }
  }

  event.waitUntil(
    self.registration.showNotification(payload.title, {
      body: payload.body,
      tag: payload.tag,
      data: { url: payload.url }
    })
  );
});

self.addEventListener("notificationclick", function (event) {
  event.notification.close();
  var targetUrl = "/";
  if (event.notification.data && event.notification.data.url) {
    targetUrl = String(event.notification.data.url);
  }

  event.waitUntil(
    self.clients.matchAll({ type: "window", includeUncontrolled: true }).then(function (windowClients) {
      for (var index = 0; index < windowClients.length; index += 1) {
        var client = windowClients[index];
        if (client && "focus" in client) {
          return client.focus();
        }
      }
      if (self.clients.openWindow) {
        return self.clients.openWindow(targetUrl);
      }
      return undefined;
    })
  );
});
