/**
 * Relays the OAuth authorization response from the popup window back to the opener.
 */
(function () {
    var params = new URLSearchParams(window.location.search || "");
    var payload = {
        type: "rosafiesta-oauth-callback",
        code: params.get("code") || "",
        state: params.get("state") || "",
        error: params.get("error") || "",
        errorDescription: params.get("error_description") || ""
    };
    if (window.opener) {
        if (!window.opener.closed) {
            try {
                window.opener.postMessage(payload, window.location.origin);
            } catch (err) {}
            window.setTimeout(function () {
                window.close();
            }, 150);
            return;
        }
    }
    if (window.name !== "rosafiesta_oauth") {
        try {
            window.sessionStorage.setItem("rosafiesta_pending_oauth", JSON.stringify(payload));
        } catch (storageErr) {}
        window.location.replace("./");
    }
})();
