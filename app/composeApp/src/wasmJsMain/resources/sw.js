/**
 * RosaFiesta Web Push, offline asset fetch, and notification click Service Worker.
 */

self.addEventListener("install", function (event) {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", function (event) {
  event.waitUntil(self.clients.claim());
});

self.addEventListener("fetch", function (event) {
  var request = event.request;
  if (!request) {
    return;
  }
  if (request.method !== "GET") {
    return;
  }
  var requestUrl = new URL(request.url);
  if (requestUrl.origin !== self.location.origin) {
    return;
  }
  event.respondWith(
    fetch(request).catch(function () {
      return caches.match(request);
    })
  );
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
      var isParsedObject = Boolean(parsed && typeof parsed === "object");
      if (isParsedObject) {
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
  if (event.notification.data) {
    if (event.notification.data.url) {
      targetUrl = String(event.notification.data.url);
    }
  }

  event.waitUntil(
    self.clients.matchAll({ type: "window", includeUncontrolled: true }).then(function (windowClients) {
      for (var index = 0; index < windowClients.length; index += 1) {
        var client = windowClients[index];
        if (!client) {
          continue;
        }
        if ("focus" in client) {
          return client.focus().then(function (focusedClient) {
            var receiver = focusedClient || client;
            var canPostMessage = Boolean(receiver && typeof receiver.postMessage === "function");
            if (canPostMessage) {
              receiver.postMessage({ type: "rosafiesta-notification-click", url: targetUrl });
            }
            var canNavigate = Boolean(receiver && "navigate" in receiver && targetUrl);
            if (canNavigate) {
              return receiver.navigate(targetUrl).catch(function () {
                return receiver;
              });
            }
            return receiver;
          });
        }
      }
      if (self.clients.openWindow) {
        return self.clients.openWindow(targetUrl);
      }
      return undefined;
    })
  );
});
