@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.platform.ExternalPaymentCallbackBridge
import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import com.jeudry.rosafiesta.domain.platform.OAuthBrowserOutcome
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@JsFun("() => { try { return typeof window !== 'undefined' && !!window.localStorage; } catch (err) { return false; } }")
private external fun jsHasBrowserLocalStorage(): Boolean

@JsFun(
    """() => {
        try {
            if (typeof navigator === 'undefined') return true;
            if (navigator.onLine === false) return false;
            const conn = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
            if (!conn) return true;
            if (conn.saveData === true) return false;
            const netType = String(conn.type || '').toLowerCase();
            const isBlockedNetwork = ['cellular', 'bluetooth', 'none'].includes(netType);
            if (isBlockedNetwork) return false;
            return true;
        } catch (err) {
            return true;
        }
    }""",
)
private external fun jsIsWifiOrUnmeteredOnline(): Boolean

@JsFun("(key) => { try { return window.localStorage.getItem(key); } catch (err) { return null; } }")
private external fun jsLocalStorageGetItem(key: String): String?

@JsFun("(key, value) => { try { window.localStorage.setItem(key, value); return true; } catch (err) { return false; } }")
private external fun jsLocalStorageSetItem(key: String, value: String): Boolean

@JsFun("(key) => { try { window.localStorage.removeItem(key); } catch (err) {} }")
private external fun jsLocalStorageRemoveItem(key: String)

@JsFun(
    """(primaryHz, secondaryHz, durationMs, volume) => {
        try {
            if (typeof window === 'undefined') return;
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (!AudioCtx) return;
            if (!window.__rfAudioContext) {
                window.__rfAudioContext = new AudioCtx();
            }
            const ctx = window.__rfAudioContext;
            if (ctx.state === 'suspended') {
                ctx.resume();
            }
            const now = ctx.currentTime;
            const durationSec = Math.max(0.024, durationMs / 1000.0);
            const gain = ctx.createGain();
            const safeVol = Math.max(0.001, Math.min(1.0, volume * 0.22));
            gain.gain.setValueAtTime(0.0001, now);
            gain.gain.linearRampToValueAtTime(safeVol, now + 0.0025);
            gain.gain.exponentialRampToValueAtTime(0.0001, now + durationSec);
            gain.connect(ctx.destination);

            const osc = ctx.createOscillator();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(Math.max(40, primaryHz), now);
            if (secondaryHz > 20) {
                osc.frequency.exponentialRampToValueAtTime(secondaryHz, now + durationSec * 0.65);
            }
            osc.connect(gain);
            osc.onended = () => {
                try { osc.disconnect(); gain.disconnect(); } catch (err) {}
            };
            osc.start(now);
            osc.stop(now + durationSec + 0.01);
        } catch (err) {}
    }""",
)
private external fun jsPlayWebAudioChime(
    primaryHz: Double,
    secondaryHz: Double,
    durationMs: Int,
    volume: Double,
)

@JsFun(
    """() => {
        try {
            return typeof window !== 'undefined' && !!(window.SpeechRecognition || window.webkitSpeechRecognition);
        } catch (err) {
            return false;
        }
    }""",
)
private external fun jsIsWebSpeechSupported(): Boolean

@JsFun(
    """(onTranscript, onError, onEnded) => {
        try {
            const SpeechCtor = window.SpeechRecognition || window.webkitSpeechRecognition;
            if (!SpeechCtor) {
                onError('Web Speech API is not supported in this browser.');
                return false;
            }
            if (window.__rfSpeechRec) {
                try { window.__rfSpeechRec.abort(); } catch (err) {}
            }
            const rec = new SpeechCtor();
            rec.lang = 'es-DO';
            rec.continuous = false;
            rec.interimResults = true;
            rec.maxAlternatives = 1;
            rec.onresult = (event) => {
                let interimText = '';
                let finalText = '';
                for (let idx = event.resultIndex; idx < event.results.length; idx++) {
                    const piece = event.results[idx][0].transcript || '';
                    if (event.results[idx].isFinal) {
                        finalText += piece;
                    } else {
                        interimText += piece;
                    }
                }
                if (finalText.trim().length > 0) {
                    onTranscript(finalText.trim(), true);
                } else if (interimText.trim().length > 0) {
                    onTranscript(interimText.trim(), false);
                }
            };
            rec.onerror = (event) => {
                onError(event && event.error ? String(event.error) : 'speech-recognition-error');
            };
            rec.onend = () => {
                onEnded();
            };
            window.__rfSpeechRec = rec;
            rec.start();
            return true;
        } catch (err) {
            onError(err && err.message ? String(err.message) : 'speech-start-failed');
            return false;
        }
    }""",
)
private external fun jsStartWebSpeechRecognition(
    onTranscript: (String, Boolean) -> Unit,
    onError: (String) -> Unit,
    onEnded: () -> Unit,
): Boolean

@JsFun(
    """() => {
        try {
            const hasActiveSpeech = typeof window !== 'undefined' ? !!window.__rfSpeechRec : false;
            if (hasActiveSpeech) {
                window.__rfSpeechRec.stop();
                window.__rfSpeechRec = null;
            }
        } catch (err) {}
    }""",
)
private external fun jsStopWebSpeechRecognition()

@JsFun(
    """() => {
        try {
            if (typeof window === 'undefined') return '';
            if (window.localStorage) {
                const stored = window.localStorage.getItem('ROSAFIESTA_API_URL');
                const hasStoredUrl = stored ? stored.trim().length > 0 : false;
                if (hasStoredUrl) return stored.trim();
            }
            const globalUrl = window.__ROSAFIESTA_API_URL ? String(window.__ROSAFIESTA_API_URL).trim() : '';
            if (globalUrl.length > 0) {
                return globalUrl;
            }
            return '';
        } catch (err) {
            return '';
        }
    }""",
)
private external fun jsReadConfiguredApiBaseUrl(): String

@JsFun(
    """(url) => {
        try {
            const canOpenWindow = typeof window !== 'undefined' ? !!window.open : false;
            if (!canOpenWindow) return false;
            window.open(url, '_blank', 'noopener,noreferrer');
            return true;
        } catch (err) {
            return false;
        }
    }""",
)
private external fun jsOpenExternalWindow(url: String): Boolean

@JsFun(
    """() => {
        try {
            if (typeof window === 'undefined' || !window.location) return 'https://app.rosafiesta.do/oauth-callback.html';
            const basePath = window.location.pathname.replace(/\/[^/]*$/, '/');
            return window.location.origin + basePath + 'oauth-callback.html';
        } catch (err) {
            return 'https://app.rosafiesta.do/oauth-callback.html';
        }
    }""",
)
private external fun jsCurrentBrowserOAuthRedirectUri(): String

@JsFun(
    """(authorizeUrl, expectedApiOrigin, onComplete) => {
        try {
            if (typeof window === 'undefined' || !window.open) {
                onComplete('', '', 'OAUTH_BROWSER_UNAVAILABLE');
                return;
            }
            const popup = window.open(authorizeUrl, 'rosafiesta_oauth', 'width=520,height=680');
            if (!popup) {
                onComplete('', '', 'OAUTH_BROWSER_UNAVAILABLE');
                return;
            }
            let settled = false;
            let timer = 0;
            const cleanup = () => {
                if (timer) window.clearInterval(timer);
                window.removeEventListener('message', onMessage);
            };
            const finish = (code, state, oauthErr) => {
                if (settled) return;
                settled = true;
                cleanup();
                try { popup.close(); } catch (err) {}
                onComplete(code || '', state || '', oauthErr || '');
            };
            const onMessage = (event) => {
                if (!event || !event.data || event.data.type !== 'rosafiesta-oauth-callback') return;
                const sameOrigin = event.origin === window.location.origin;
                const apiOrigin = expectedApiOrigin && event.origin === expectedApiOrigin;
                if (!sameOrigin && !apiOrigin) return;
                finish(event.data.code || '', event.data.state || '', event.data.error || '');
            };
            window.addEventListener('message', onMessage);
            let ticks = 0;
            const maxTicks = 720;
            timer = window.setInterval(() => {
                ticks += 1;
                if (ticks >= maxTicks) {
                    finish('', '', 'OAUTH_TIMEOUT');
                    return;
                }
                if (popup.closed) {
                    finish('', '', 'OAUTH_CANCELLED');
                    return;
                }
                try {
                    if (popup.location && popup.location.origin === window.location.origin) {
                        const params = new URLSearchParams(popup.location.search || '');
                        const code = params.get('code') || '';
                        const state = params.get('state') || '';
                        const oauthErr = params.get('error') || '';
                        if (code.length > 0 || oauthErr.length > 0) {
                            finish(code, state, oauthErr);
                        }
                    }
                } catch (crossOriginWait) {}
            }, 250);
        } catch (err) {
            onComplete('', '', 'OAUTH_BROWSER_UNAVAILABLE');
        }
    }""",
)
private external fun jsLaunchOAuthPopup(
    authorizeUrl: String,
    expectedApiOrigin: String,
    onComplete: (String, String, String) -> Unit,
)

@JsFun(
    """(promptTitle, onDone) => {
        try {
            if (typeof window === 'undefined' || !window.isSecureContext || !window.PublicKeyCredential || !navigator.credentials) {
                onDone(false, 'DEVICE_AUTH_UNAVAILABLE');
                return;
            }
            window.PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable()
                .then((available) => {
                    if (!available) {
                        onDone(false, 'DEVICE_AUTH_UNAVAILABLE');
                        return;
                    }
                    const challenge = new Uint8Array(32);
                    crypto.getRandomValues(challenge);
                    const userId = new Uint8Array(16);
                    crypto.getRandomValues(userId);
                    navigator.credentials.create({
                        publicKey: {
                            challenge: challenge,
                            rp: { name: 'RosaFiesta' },
                            user: { id: userId, name: 'owner@rosafiesta.do', displayName: promptTitle || 'RosaFiesta' },
                            pubKeyCredParams: [{ type: 'public-key', alg: -7 }],
                            authenticatorSelection: { authenticatorAttachment: 'platform', userVerification: 'required' },
                            timeout: 30000
                        }
                    }).then(() => {
                        onDone(true, '');
                    }).catch(() => {
                        onDone(false, 'DEVICE_AUTH_CANCELLED');
                    });
                })
                .catch(() => onDone(false, 'DEVICE_AUTH_UNAVAILABLE'));
        } catch (err) {
            onDone(false, 'DEVICE_AUTH_UNAVAILABLE');
        }
    }""",
)
private external fun jsAuthenticateWebAuthnDeviceOwner(
    promptTitle: String,
    onDone: (Boolean, String) -> Unit,
)

@JsFun(
    """(vapidPublicKey, onDone) => {
        const decodeVapidKey = (base64Url) => {
            const padded = base64Url + '='.repeat((4 - (base64Url.length % 4)) % 4);
            const raw = window.atob(padded.replace(/-/g, '+').replace(/_/g, '/'));
            const out = new Uint8Array(raw.length);
            for (let index = 0; index < raw.length; index += 1) {
                out[index] = raw.charCodeAt(index);
            }
            return out;
        };
        try {
            if (typeof window === 'undefined' || typeof Notification === 'undefined') {
                onDone('');
                return;
            }
            Notification.requestPermission().then((perm) => {
                if (perm !== 'granted' || !navigator.serviceWorker) {
                    onDone('');
                    return;
                }
                navigator.serviceWorker.register('./sw.js').catch(() => null);
                navigator.serviceWorker.ready.then((reg) => {
                    if (!reg || !reg.pushManager) {
                        onDone('');
                        return;
                    }
                    reg.pushManager.getSubscription().then((existing) => {
                        if (existing) {
                            onDone(JSON.stringify(existing));
                            return;
                        }
                        const cleanKey = (vapidPublicKey || '').trim();
                        if (!cleanKey) {
                            onDone('');
                            return;
                        }
                        reg.pushManager.subscribe({
                            userVisibleOnly: true,
                            applicationServerKey: decodeVapidKey(cleanKey)
                        }).then((created) => {
                            onDone(created ? JSON.stringify(created) : '');
                        }).catch(() => onDone(''));
                    }).catch(() => onDone(''));
                }).catch(() => onDone(''));
            }).catch(() => onDone(''));
        } catch (err) {
            onDone('');
        }
    }""",
)
private external fun jsRequestWebNotificationsAndPush(
    vapidPublicKey: String,
    onDone: (String) -> Unit,
)

@JsFun(
    """(expectedApiOrigin, onPaymentReturn) => {
        try {
            if (typeof window === 'undefined') return;
            if (!window.__rfPaymentListenerAttached) {
                window.__rfPaymentListenerAttached = true;
                window.addEventListener('message', (event) => {
                    if (!event || !event.data || event.data.type !== 'rosafiesta-payment-callback') return;
                    const sameOrigin = event.origin === window.location.origin;
                    const apiOrigin = expectedApiOrigin && event.origin === expectedApiOrigin;
                    if (!sameOrigin && !apiOrigin) return;
                    onPaymentReturn(
                        String(event.data.sessionId || ''),
                        String(event.data.eventId || ''),
                        Boolean(event.data.cancelled)
                    );
                });
            }
            if (window.location && window.location.search) {
                const params = new URLSearchParams(window.location.search);
                const sessionId = params.get('session_id') || '';
                const eventId = params.get('event_id') || '';
                const cancelled = params.get('cancelled') === '1';
                if (sessionId || cancelled) {
                    try {
                        window.history.replaceState(null, '', window.location.pathname + (window.location.hash || ''));
                    } catch (historyErr) {}
                    onPaymentReturn(sessionId, eventId, cancelled);
                }
            }
        } catch (err) {}
    }""",
)
private external fun jsInstallPaymentReturnListener(
    expectedApiOrigin: String,
    onPaymentReturn: (String, String, Boolean) -> Unit,
)

@JsFun(
    """(title, body) => {
        try {
            if (typeof Notification === 'undefined' || Notification.permission !== 'granted') return;
            new Notification(title, { body: body });
        } catch (err) {}
    }""",
)
private external fun jsShowWebNotification(title: String, body: String)

@JsFun(
    """(fileName, mimeType, base64Payload) => {
        try {
            if (typeof document === 'undefined') return false;
            if (typeof URL === 'undefined') return false;
            const binaryStr = atob(base64Payload);
            const bytes = new Uint8Array(binaryStr.length);
            for (let idx = 0; idx < binaryStr.length; idx++) {
                bytes[idx] = binaryStr.charCodeAt(idx) & 0xff;
            }
            const blob = new Blob([bytes], { type: mimeType });
            const blobUrl = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = blobUrl;
            link.download = fileName;
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            setTimeout(() => URL.revokeObjectURL(blobUrl), 5000);
            return true;
        } catch (err) {
            return false;
        }
    }""",
)
private external fun jsTriggerBrowserDownload(
    fileName: String,
    mimeType: String,
    base64Payload: String,
): Boolean

@JsFun(
    """(title, text) => {
        try {
            if (typeof navigator === 'undefined') return false;
            if (navigator.share) {
                navigator.share({ title: title, text: text }).catch(() => {});
                return true;
            }
            const canWriteClipboard = navigator.clipboard ? !!navigator.clipboard.writeText : false;
            if (canWriteClipboard) {
                navigator.clipboard.writeText(title + '\n' + text).catch(() => {});
                return true;
            }
            return false;
        } catch (err) {
            return false;
        }
    }""",
)
private external fun jsTriggerWebTextShare(title: String, text: String): Boolean

@JsFun(
    """(acceptAttr, onPicked, onCancelled) => {
        try {
            if (typeof document === 'undefined') {
                onCancelled();
                return;
            }
            const input = document.createElement('input');
            input.type = 'file';
            const hasAcceptFilter = acceptAttr ? acceptAttr.length > 0 : false;
            if (hasAcceptFilter) {
                input.accept = acceptAttr;
            }
            input.style.display = 'none';
            let settled = false;
            const finishCancel = () => {
                if (settled) return;
                settled = true;
                try { input.remove(); } catch (err) {}
                onCancelled();
            };
            input.addEventListener('cancel', finishCancel);
            input.addEventListener('change', () => {
                const file = input.files && input.files[0];
                if (!file) {
                    finishCancel();
                    return;
                }
                const reader = new FileReader();
                reader.onload = () => {
                    if (settled) return;
                    settled = true;
                    try { input.remove(); } catch (err) {}
                    const dataUrl = String(reader.result || '');
                    const commaPos = dataUrl.indexOf(',');
                    const base64Part = commaPos >= 0 ? dataUrl.substring(commaPos + 1) : '';
                    onPicked(file.name || 'adjunto', file.type || '', base64Part);
                };
                reader.onerror = finishCancel;
                reader.readAsDataURL(file);
            });
            document.body.appendChild(input);
            input.click();
        } catch (err) {
            onCancelled();
        }
    }""",
)
private external fun jsPickBrowserFile(
    acceptAttr: String,
    onPicked: (String, String, String) -> Unit,
    onCancelled: () -> Unit,
)

internal class WasmSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = AppAudioConfig.DEFAULT_VOLUME
    override var enabledCategories: Set<SoundEffectCategory> = AppAudioConfig.DEFAULT_CATEGORIES

    override fun play(effect: SoundEffect) {
        if (!canPlayEffect(effect)) return
        jsPlayWebAudioChime(
            primaryHz = effect.primaryHz,
            secondaryHz = effect.secondaryHz,
            durationMs = effect.durationMs,
            volume = soundVolume.coerceIn(0f, 1f).toDouble(),
        )
    }
}

internal class WasmLocalStorageSecureStorage : SecureStorage {
    private val headlessFallback = InMemorySecureStorage()

    override suspend fun get(key: String): String? {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return null
        if (!jsHasBrowserLocalStorage()) {
            return headlessFallback.get(cleanKey)
        }
        return jsLocalStorageGetItem("$STORAGE_NAMESPACE$cleanKey")
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        val cleanValue = value.trim()
        if (!jsHasBrowserLocalStorage()) {
            headlessFallback.put(cleanKey, cleanValue)
            return
        }
        val saved = jsLocalStorageSetItem("$STORAGE_NAMESPACE$cleanKey", cleanValue)
        if (!saved) {
            headlessFallback.put(cleanKey, cleanValue)
        }
    }

    override suspend fun remove(key: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        if (!jsHasBrowserLocalStorage()) {
            headlessFallback.remove(cleanKey)
            return
        }
        jsLocalStorageRemoveItem("$STORAGE_NAMESPACE$cleanKey")
        headlessFallback.remove(cleanKey)
    }

    private companion object {
        const val STORAGE_NAMESPACE = "rosafiesta.secure."
    }
}

internal class WasmVoiceInputMonitor : VoiceInputMonitor {
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _amplitudeLevel = MutableStateFlow(0f)
    override val amplitudeLevel: StateFlow<Float> = _amplitudeLevel.asStateFlow()

    private val _hasHardwareMicrophone = MutableStateFlow(jsIsWebSpeechSupported())
    override val hasHardwareMicrophone: StateFlow<Boolean> = _hasHardwareMicrophone.asStateFlow()

    private val _recognizedTranscript = MutableStateFlow("")
    override val recognizedTranscript: StateFlow<String> = _recognizedTranscript.asStateFlow()

    private val _capturedWavBytes = MutableStateFlow<ByteArray?>(null)
    override val capturedWavBytes: StateFlow<ByteArray?> = _capturedWavBytes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    override fun startListening(onTranscriptResult: (String) -> Unit) {
        if (!jsIsWebSpeechSupported()) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Web Speech API is not available in this browser."
            _isListening.value = false
            return
        }

        _errorMessage.value = null
        _recognizedTranscript.value = ""
        _capturedWavBytes.value = null
        _hasHardwareMicrophone.value = true
        _isListening.value = true
        _amplitudeLevel.value = 0.28f

        val started = jsStartWebSpeechRecognition(
            onTranscript = { text, isFinal ->
                _recognizedTranscript.value = text
                _isTranscribing.value = !isFinal
                _amplitudeLevel.value = if (isFinal) 0f else 0.55f
                if (isFinal) {
                    _isListening.value = false
                    onTranscriptResult(text)
                }
            },
            onError = { errorCode ->
                _errorMessage.value = "Browser speech recognition error ($errorCode)."
                _isListening.value = false
                _isTranscribing.value = false
                _amplitudeLevel.value = 0f
            },
            onEnded = {
                _isListening.value = false
                _isTranscribing.value = false
                _amplitudeLevel.value = 0f
            },
        )
        if (!started) {
            _isListening.value = false
            _amplitudeLevel.value = 0f
        }
    }

    override fun stopListening() {
        jsStopWebSpeechRecognition()
        _isListening.value = false
        _isTranscribing.value = false
        _amplitudeLevel.value = 0f
    }
}

internal class WasmPlatformActions : PlatformActions {
    init {
        val apiOrigin = extractOrigin(defaultApiBaseUrl())
        jsInstallPaymentReturnListener(apiOrigin) { sessionId, eventId, cancelled ->
            ExternalPaymentCallbackBridge.dispatchPaymentPayload(
                sessionId = sessionId,
                eventId = eventId,
                cancelled = cancelled,
            )
        }
    }

    private fun extractOrigin(rawUrl: String): String {
        val schemeIdx = rawUrl.indexOf("://")
        if (schemeIdx < 0) return ""
        val slashAfterHost = rawUrl.indexOf('/', startIndex = schemeIdx + 3)
        return if (slashAfterHost > 0) rawUrl.substring(0, slashAfterHost) else rawUrl
    }

    override fun defaultApiBaseUrl(): String {
        val configured = jsReadConfiguredApiBaseUrl()
        return ExternalUrlBuilder.normalizeBaseUrl(configured)
    }

    override fun openExternalUrl(url: String): Boolean {
        val cleanUrl = url.trim()
        if (cleanUrl.isEmpty()) return false
        return jsOpenExternalWindow(cleanUrl)
    }

    override fun shareContent(
        title: String,
        text: String,
        fileName: String?,
        fileBytes: ByteArray?,
        mimeType: String,
    ): Boolean {
        if (fileBytes != null) {
            if (fileBytes.isNotEmpty()) {
                val safeName = ExternalUrlBuilder.sanitizeFileName(fileName ?: "rosafiesta-compartido.pdf")
                return saveDocument(safeName, mimeType, fileBytes) != null
            }
        }
        return jsTriggerWebTextShare(title.trim(), text.trim())
    }

    override fun saveDocument(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String? {
        if (bytes.isEmpty()) return null
        val safeName = ExternalUrlBuilder.sanitizeFileName(fileName)
        val resolvedMime = mimeType.trim().ifEmpty { ExternalUrlBuilder.inferMimeType(safeName) }
        val base64Payload = standardBase64Encode(bytes)
        val triggered = jsTriggerBrowserDownload(safeName, resolvedMime, base64Payload)
        return if (triggered) safeName else null
    }

    override suspend fun pickMediaFile(allowedExtensions: List<String>): PickedMediaFile? {
        val acceptAttr = allowedExtensions
            .map { ext -> ext.trim().trimStart('.').lowercase() }
            .filter { ext -> ext.isNotEmpty() }
            .joinToString(",") { ext -> ".$ext" }

        return suspendCancellableCoroutine { continuation ->
            jsPickBrowserFile(
                acceptAttr = acceptAttr,
                onPicked = { pickedName, browserMime, base64Payload ->
                    val decodedBytes = standardBase64Decode(base64Payload)
                    val resolvedMime = browserMime.trim().ifEmpty {
                        ExternalUrlBuilder.inferMimeType(pickedName)
                    }
                    val result = PickedMediaFile(
                        fileName = pickedName,
                        mimeType = resolvedMime,
                        sizeBytes = decodedBytes.size.toLong(),
                        bytes = decodedBytes,
                    )
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                },
                onCancelled = {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                },
            )
        }
    }

    override fun isOnWifiNetwork(): Boolean = jsIsWifiOrUnmeteredOnline()

    override fun authenticateDeviceOwner(
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        jsAuthenticateWebAuthnDeviceOwner(promptTitle) { ok, errCode ->
            onResult(ok, errCode.ifEmpty { null })
        }
    }

    override suspend fun launchOAuthBrowserFlow(
        resolveAuthorizeUrl: suspend (redirectUri: String) -> String?,
    ): OAuthBrowserOutcome {
        val redirectUri = jsCurrentBrowserOAuthRedirectUri()
        val authorizeUrl = resolveAuthorizeUrl(redirectUri)
            ?: return OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_START_FAILED")
        val apiOrigin = extractOrigin(defaultApiBaseUrl())
        return suspendCancellableCoroutine { continuation ->
            jsLaunchOAuthPopup(authorizeUrl, apiOrigin) { code, state, errorCode ->
                if (!continuation.isActive) return@jsLaunchOAuthPopup
                continuation.resume(
                    OAuthBrowserOutcome(
                        code = code.ifEmpty { null },
                        state = state.ifEmpty { null },
                        redirectUri = redirectUri,
                        errorCode = errorCode.ifEmpty { null },
                    ),
                )
            }
        }
    }

    override fun registerPushNotifications(
        appVariant: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        registerPushNotificationsWithConfig(appVariant, "", onResult)
    }

    override fun registerPushNotificationsWithConfig(
        appVariant: String,
        vapidPublicKey: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        jsRequestWebNotificationsAndPush(vapidPublicKey.trim()) { rawSubscription ->
            onResult("web", rawSubscription.trim().ifEmpty { null })
        }
    }

    override fun showLocalNotification(title: String, body: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        jsShowWebNotification(cleanTitle, body.trim())
    }

    private fun standardBase64Encode(bytes: ByteArray): String {
        val alphabet = STANDARD_BASE64_ALPHABET
        val output = StringBuilder(((bytes.size + 2) / 3) * 4)
        for (index in bytes.indices step 3) {
            val byte0 = bytes[index].toInt() and 0xFF
            val has1 = index + 1 < bytes.size
            val has2 = index + 2 < bytes.size
            val byte1 = if (has1) bytes[index + 1].toInt() and 0xFF else 0
            val byte2 = if (has2) bytes[index + 2].toInt() and 0xFF else 0
            val triple = (byte0 shl 16) or (byte1 shl 8) or byte2
            output.append(alphabet[(triple ushr 18) and 0x3F])
            output.append(alphabet[(triple ushr 12) and 0x3F])
            output.append(if (has1) alphabet[(triple ushr 6) and 0x3F] else '=')
            output.append(if (has2) alphabet[triple and 0x3F] else '=')
        }
        return output.toString()
    }

    private fun standardBase64Decode(encoded: String): ByteArray {
        val clean = encoded.trim().trimEnd('=')
        if (clean.isEmpty()) return ByteArray(0)
        val outputSize = (clean.length * 3) / 4
        val result = ByteArray(outputSize)
        var outIndex = 0
        for (charIndex in clean.indices step 4) {
            val c0 = STANDARD_BASE64_ALPHABET.indexOf(clean[charIndex]).coerceAtLeast(0)
            val c1 = if (charIndex + 1 < clean.length) {
                STANDARD_BASE64_ALPHABET.indexOf(clean[charIndex + 1]).coerceAtLeast(0)
            } else {
                0
            }
            val has2 = charIndex + 2 < clean.length
            val c2 = if (has2) STANDARD_BASE64_ALPHABET.indexOf(clean[charIndex + 2]).coerceAtLeast(0) else 0
            val has3 = charIndex + 3 < clean.length
            val c3 = if (has3) STANDARD_BASE64_ALPHABET.indexOf(clean[charIndex + 3]).coerceAtLeast(0) else 0
            val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
            if (outIndex < outputSize) result[outIndex++] = ((triple ushr 16) and 0xFF).toByte()
            if (has2) {
                if (outIndex < outputSize) result[outIndex++] = ((triple ushr 8) and 0xFF).toByte()
            }
            if (has3) {
                if (outIndex < outputSize) result[outIndex++] = (triple and 0xFF).toByte()
            }
        }
        return result
    }

    private companion object {
        const val STANDARD_BASE64_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    }
}

public actual fun createPlatformSoundPlayer(): SoundPlayer = WasmSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage = WasmLocalStorageSecureStorage()

public actual fun createPlatformVoiceInputMonitor(): VoiceInputMonitor = WasmVoiceInputMonitor()

public actual fun createPlatformActions(): PlatformActions = WasmPlatformActions()
