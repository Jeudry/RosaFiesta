@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.platform.ExternalPaymentCallbackBridge
import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import com.jeudry.rosafiesta.domain.platform.OAuthBrowserOutcome
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import com.jeudry.rosafiesta.domain.sound.WavPcmEncoder
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSLocale
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_interface_type_wired
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecDuplicateItem
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.math.sqrt

private var registeredIosMediaPicker: (suspend (List<String>) -> PickedMediaFile?)? = null
private var registeredIosApnsToken: String? = null
private var pendingIosPushRegistrationCallback: ((platform: String, token: String?) -> Unit)? = null
private var retainedOAuthSession: ASWebAuthenticationSession? = null
private var retainedPresentationContext: ASWebAuthenticationPresentationContextProvidingProtocol? = null

/**
 * Registers the real APNs device token hex string obtained by `application(_:didRegisterForRemoteNotificationsWithDeviceToken:)`.
 */
public fun registerIosApnsDeviceToken(tokenHex: String?) {
    val cleanToken = tokenHex?.trim()?.ifEmpty { null }
    registeredIosApnsToken = cleanToken
    val pendingCallback = pendingIosPushRegistrationCallback
    pendingIosPushRegistrationCallback = null
    pendingCallback?.invoke("ios", cleanToken)
}

/**
 * Handles incoming iOS custom scheme deep links (`rosafiesta://payment/...`) opened from Safari or Stripe Checkout.
 */
public fun handleIosIncomingDeepLinkUrl(urlString: String?): Boolean {
    val cleanUrl = urlString?.trim().orEmpty()
    if (cleanUrl.isEmpty()) return false
    return ExternalPaymentCallbackBridge.dispatchPaymentDeepLink(cleanUrl)
}

/**
 * Registers an optional custom iOS media picker handler if a host `UIViewController` overrides the default
 * `UIDocumentPickerViewController` presentation.
 */
public fun registerIosMediaPickerBridge(
    picker: (suspend (List<String>) -> PickedMediaFile?)?,
) {
    registeredIosMediaPicker = picker
}

internal class IosSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = AppAudioConfig.DEFAULT_VOLUME
    override var enabledCategories: Set<SoundEffectCategory> = AppAudioConfig.DEFAULT_CATEGORIES
    private var activePlayer: AVAudioPlayer? = null

    override fun play(effect: SoundEffect) {
        if (!canPlayEffect(effect)) return
        runCatching {
            val haptic = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            haptic.prepare()
            haptic.impactOccurred()
        }
        runCatching {
            val baseName = effect.fileName.substringBeforeLast(".")
            val ext = effect.fileName.substringAfterLast(".", "m4a")
            val bundleUrl = NSBundle.mainBundle.URLForResource(
                name = baseName,
                withExtension = ext,
                subdirectory = "compose-resources/composeResources/rosafiesta.composeapp.generated.resources/files/audio",
            )
            val player = if (bundleUrl != null) {
                AVAudioPlayer(contentsOfURL = bundleUrl, error = null)
            } else {
                val wavBytes = PcmSoundSynthesizer.synthesizeWav(effect, soundVolume)
                val nsData = wavBytes.toIosNsData()
                AVAudioPlayer(data = nsData, error = null)
            }
            activePlayer?.stop()
            player.volume = soundVolume.coerceIn(0f, 1f)
            player.prepareToPlay()
            player.play()
            activePlayer = player
        }
    }
}

internal class IosKeychainSecureStorage : SecureStorage {
    override suspend fun get(key: String): String? {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return null
        return memScoped {
            val serviceRef = createCfString(KEYCHAIN_SERVICE) ?: return@memScoped null
            val accountRef = createCfString(cleanKey)
            if (accountRef == null) {
                CFRelease(serviceRef)
                return@memScoped null
            }
            val query = createBaseKeychainQuery(serviceRef, accountRef)
            if (query == null) {
                CFRelease(accountRef)
                CFRelease(serviceRef)
                return@memScoped null
            }
            try {
                CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
                CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
                val resultVar = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, resultVar.ptr)
                if (status != errSecSuccess) return@memScoped null
                val dataRef: CFDataRef = resultVar.value?.reinterpret() ?: return@memScoped null
                try {
                    val length = CFDataGetLength(dataRef).toInt()
                    if (length == 0) return@memScoped ""
                    val bytePointer = CFDataGetBytePtr(dataRef) ?: return@memScoped null
                    bytePointer.readBytes(length).decodeToString()
                } finally {
                    CFRelease(dataRef)
                }
            } finally {
                CFRelease(query)
                CFRelease(accountRef)
                CFRelease(serviceRef)
            }
        }
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        val utf8Bytes = value.trim().encodeToByteArray()

        val serviceRef = createCfString(KEYCHAIN_SERVICE) ?: return
        val accountRef = createCfString(cleanKey)
        if (accountRef == null) {
            CFRelease(serviceRef)
            return
        }
        val dataRef = createCfData(utf8Bytes)
        if (dataRef == null) {
            CFRelease(accountRef)
            CFRelease(serviceRef)
            return
        }

        val addDict = createBaseKeychainQuery(serviceRef, accountRef)
        if (addDict == null) {
            CFRelease(dataRef)
            CFRelease(accountRef)
            CFRelease(serviceRef)
            return
        }

        try {
            CFDictionarySetValue(addDict, kSecValueData, dataRef)
            CFDictionarySetValue(addDict, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            val addStatus = SecItemAdd(addDict, null)
            if (addStatus == errSecDuplicateItem) {
                val matchQuery = createBaseKeychainQuery(serviceRef, accountRef)
                val updateAttrs = CFDictionaryCreateMutable(
                    kCFAllocatorDefault,
                    2,
                    kCFTypeDictionaryKeyCallBacks.ptr,
                    kCFTypeDictionaryValueCallBacks.ptr,
                )
                if (matchQuery != null) {
                    if (updateAttrs != null) {
                        CFDictionarySetValue(updateAttrs, kSecValueData, dataRef)
                        CFDictionarySetValue(updateAttrs, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
                        SecItemUpdate(matchQuery, updateAttrs)
                        CFRelease(updateAttrs)
                    }
                    CFRelease(matchQuery)
                }
            }
        } finally {
            CFRelease(addDict)
            CFRelease(dataRef)
            CFRelease(accountRef)
            CFRelease(serviceRef)
        }
    }

    override suspend fun remove(key: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        val serviceRef = createCfString(KEYCHAIN_SERVICE) ?: return
        val accountRef = createCfString(cleanKey)
        if (accountRef == null) {
            CFRelease(serviceRef)
            return
        }
        val deleteQuery = createBaseKeychainQuery(serviceRef, accountRef)
        try {
            if (deleteQuery != null) {
                SecItemDelete(deleteQuery)
            }
        } finally {
            if (deleteQuery != null) {
                CFRelease(deleteQuery)
            }
            CFRelease(accountRef)
            CFRelease(serviceRef)
        }
    }

    private fun createBaseKeychainQuery(
        serviceRef: CFStringRef,
        accountRef: CFStringRef,
    ): CFMutableDictionaryRef? {
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            6,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        ) ?: return null
        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(dict, kSecAttrService, serviceRef)
        CFDictionarySetValue(dict, kSecAttrAccount, accountRef)
        return dict
    }

    private fun createCfString(value: String): CFStringRef? = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8)

    private fun createCfData(bytes: ByteArray): CFDataRef? {
        if (bytes.isEmpty()) {
            return CFDataCreate(kCFAllocatorDefault, null, 0)
        }
        return bytes.usePinned { pinned ->
            CFDataCreate(kCFAllocatorDefault, pinned.addressOf(0).reinterpret(), bytes.size.convert())
        }
    }

    private companion object {
        const val KEYCHAIN_SERVICE = "do.rosafiesta.secure"
    }
}

internal class IosVoiceInputMonitor : VoiceInputMonitor {
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _amplitudeLevel = MutableStateFlow(0f)
    override val amplitudeLevel: StateFlow<Float> = _amplitudeLevel.asStateFlow()

    private val _hasHardwareMicrophone = MutableStateFlow(false)
    override val hasHardwareMicrophone: StateFlow<Boolean> = _hasHardwareMicrophone.asStateFlow()

    private val _recognizedTranscript = MutableStateFlow("")
    override val recognizedTranscript: StateFlow<String> = _recognizedTranscript.asStateFlow()

    private val _capturedWavBytes = MutableStateFlow<ByteArray?>(null)
    override val capturedWavBytes: StateFlow<ByteArray?> = _capturedWavBytes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var audioEngine: AVAudioEngine? = null
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: SFSpeechRecognitionTask? = null
    private val pcmCaptureChunks = mutableListOf<ByteArray>()
    private var capturedSampleRateHz: Int = 16_000

    override fun startListening(onTranscriptResult: (String) -> Unit) {
        val currentStatus = SFSpeechRecognizer.authorizationStatus()
        if (currentStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusNotDetermined) {
            SFSpeechRecognizer.requestAuthorization { newStatus ->
                dispatch_async(dispatch_get_main_queue()) {
                    if (newStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                        beginSpeechSession(onTranscriptResult)
                    } else {
                        _hasHardwareMicrophone.value = false
                        _errorMessage.value = "Speech recognition authorization was denied on iOS."
                        _isListening.value = false
                    }
                }
            }
            return
        }

        if (currentStatus != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Speech recognition permission is not authorized in iOS Settings."
            _isListening.value = false
            return
        }

        dispatch_async(dispatch_get_main_queue()) {
            beginSpeechSession(onTranscriptResult)
        }
    }

    override fun stopListening() {
        dispatch_async(dispatch_get_main_queue()) {
            teardownAudioSession(emitWav = true)
        }
    }

    private fun beginSpeechSession(onTranscriptResult: (String) -> Unit) {
        teardownAudioSession(emitWav = false)
        _errorMessage.value = null
        _recognizedTranscript.value = ""
        _capturedWavBytes.value = null
        pcmCaptureChunks.clear()

        val dominicanRecognizer = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = "es-DO"))
        val recognizer = if (dominicanRecognizer.isAvailable()) {
            dominicanRecognizer
        } else {
            SFSpeechRecognizer(locale = NSLocale(localeIdentifier = "es-ES"))
        }
        if (!recognizer.isAvailable()) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "SFSpeechRecognizer is temporarily unavailable."
            _isListening.value = false
            return
        }

        val sessionConfigured = runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeMeasurement,
                options = AVAudioSessionCategoryOptionDefaultToSpeaker,
                error = null,
            )
            session.setActive(true, withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)
        }.getOrDefault(false)
        if (!sessionConfigured) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Unable to activate iOS AVAudioSession for microphone input."
            _isListening.value = false
            return
        }

        val engine = AVAudioEngine()
        val inputNode = engine.inputNode
        val recordingFormat = inputNode.outputFormatForBus(0u)
        val detectedSampleRate = recordingFormat.sampleRate.toInt()
        if (detectedSampleRate <= 0) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "No physical microphone input channel is available."
            _isListening.value = false
            return
        }
        capturedSampleRateHz = detectedSampleRate

        val request = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
        }
        recognitionRequest = request
        audioEngine = engine

        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 1024u,
            format = recordingFormat,
        ) { pcmBuffer, _ ->
            if (pcmBuffer != null) {
                request.appendAudioPCMBuffer(pcmBuffer)
                processTappedPcmBuffer(pcmBuffer)
            }
        }

        engine.prepare()
        val started = runCatching { engine.startAndReturnError(null) }.getOrDefault(false)
        if (!started) {
            teardownAudioSession(emitWav = false)
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Failed to start iOS AVAudioEngine microphone capture."
            return
        }

        _hasHardwareMicrophone.value = true
        _isListening.value = true
        _isTranscribing.value = true

        recognitionTask = recognizer.recognitionTaskWithRequest(request) { speechResult, speechError ->
            if (speechResult != null) {
                val formatted = speechResult.bestTranscription.formattedString.trim()
                if (formatted.isNotEmpty()) {
                    _recognizedTranscript.value = formatted
                }
                if (speechResult.isFinal()) {
                    teardownAudioSession(emitWav = true)
                    if (formatted.isNotEmpty()) {
                        onTranscriptResult(formatted)
                    }
                }
            }
            if (speechError != null) {
                val wasListening = _isListening.value
                teardownAudioSession(emitWav = true)
                if (wasListening) {
                    _errorMessage.value = speechError.localizedDescription
                }
            }
        }
    }

    private fun processTappedPcmBuffer(buffer: AVAudioPCMBuffer) {
        val frameCount = buffer.frameLength.toInt()
        if (frameCount <= 0) return
        val channelDataPtr = buffer.floatChannelData ?: return
        val firstChannel = channelDataPtr[0] ?: return

        var sumSquares = 0.0
        val pcmChunk = ByteArray(frameCount * 2)
        for (sampleIndex in 0 until frameCount) {
            val floatSample = firstChannel[sampleIndex].coerceIn(-1f, 1f)
            sumSquares += (floatSample * floatSample).toDouble()
            val intSample = (floatSample * 32767f).toInt().coerceIn(-32767, 32767)
            pcmChunk[sampleIndex * 2] = (intSample and 0xFF).toByte()
            pcmChunk[sampleIndex * 2 + 1] = ((intSample shr 8) and 0xFF).toByte()
        }
        if (pcmCaptureChunks.size < MAX_CAPTURED_TAP_CHUNKS) {
            pcmCaptureChunks.add(pcmChunk)
        }
        val rms = sqrt(sumSquares / frameCount)
        _amplitudeLevel.value = (rms * 5.0).toFloat().coerceIn(0f, 1f)
    }

    private fun teardownAudioSession(emitWav: Boolean) {
        runCatching {
            audioEngine?.inputNode?.removeTapOnBus(0u)
            audioEngine?.stop()
        }
        runCatching {
            recognitionRequest?.endAudio()
            recognitionTask?.cancel()
        }
        audioEngine = null
        recognitionRequest = null
        recognitionTask = null
        _isListening.value = false
        _isTranscribing.value = false
        _amplitudeLevel.value = 0f

        if (emitWav) {
            val totalBytes = pcmCaptureChunks.sumOf { chunk -> chunk.size }
            if (totalBytes > 0) {
                val mergedPcm = ByteArray(totalBytes)
                var offset = 0
                for (chunk in pcmCaptureChunks) {
                    chunk.copyInto(mergedPcm, destinationOffset = offset)
                    offset += chunk.size
                }
                _capturedWavBytes.value = WavPcmEncoder.encodeMono16BitPcmToWav(mergedPcm, capturedSampleRateHz)
            }
            pcmCaptureChunks.clear()
        }
    }

    private companion object {
        const val MAX_CAPTURED_TAP_CHUNKS = 400
    }
}

internal object IosSharedNetworkMonitor {
    private var lastObservedUnmeteredNetwork: Boolean = true
    private var monitorRef = nw_path_monitor_create().also { monitor ->
        nw_path_monitor_set_update_handler(monitor) { path ->
            if (path != null) {
                val satisfied = nw_path_get_status(path) == nw_path_status_satisfied
                val usesWifi = nw_path_uses_interface_type(path, nw_interface_type_wifi)
                val usesWired = nw_path_uses_interface_type(path, nw_interface_type_wired)
                val unmetered = if (usesWifi) true else usesWired
                lastObservedUnmeteredNetwork = if (satisfied) unmetered else false
            }
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_global_queue(0L, 0u))
        nw_path_monitor_start(monitor)
    }

    fun isUnmeteredConnected(): Boolean = lastObservedUnmeteredNetwork

    fun stop() {
        val current = monitorRef
        monitorRef = null
        if (current != null) {
            nw_path_monitor_cancel(current)
        }
    }
}

internal class IosPlatformActions : PlatformActions {
    private var retainedDocumentDelegate: NSObject? = null

    init {
        IosSharedNetworkMonitor.isUnmeteredConnected()
    }

    override fun isOnWifiNetwork(): Boolean = IosSharedNetworkMonitor.isUnmeteredConnected()

    override fun authenticateDeviceOwner(
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val context = LAContext()
        val canEvaluate = memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, errorPtr.ptr)
        }
        if (!canEvaluate) {
            onResult(false, "BIOMETRIC_UNAVAILABLE")
            return
        }
        val reason = promptSubtitle.ifBlank { promptTitle.ifBlank { "Desbloquear RosaFiesta" } }
        context.evaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthentication,
            localizedReason = reason,
        ) { success, _ ->
            dispatch_async(dispatch_get_main_queue()) {
                onResult(success, if (success) null else "BIOMETRIC_FAILED")
            }
        }
    }

    override fun defaultApiBaseUrl(): String {
        val envMap = NSProcessInfo.processInfo.environment
        val envValue = (envMap["ROSAFIESTA_API_URL"] as? String)?.trim().orEmpty()
        if (envValue.isNotEmpty()) {
            return ExternalUrlBuilder.normalizeBaseUrl(envValue)
        }
        val plistValue = (NSBundle.mainBundle.objectForInfoDictionaryKey("RosaFiestaApiUrl") as? String)
            ?.trim()
            .orEmpty()
        return ExternalUrlBuilder.normalizeBaseUrl(plistValue.ifEmpty { null })
    }

    override fun openExternalUrl(url: String): Boolean {
        val cleanUrl = url.trim()
        if (cleanUrl.isEmpty()) return false
        val nsUrl = NSURL.URLWithString(cleanUrl) ?: return false
        dispatch_async(dispatch_get_main_queue()) {
            UIApplication.sharedApplication.openURL(
                nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null,
            )
        }
        return true
    }

    override fun shareContent(
        title: String,
        text: String,
        fileName: String?,
        fileBytes: ByteArray?,
        mimeType: String,
    ): Boolean {
        val rootController = resolveActiveRootViewController() ?: return false
        val items = mutableListOf<Any>()
        if (text.isNotBlank()) {
            items.add(text.trim())
        } else if (title.isNotBlank()) {
            items.add(title.trim())
        }

        if (fileBytes != null) {
            if (fileBytes.isNotEmpty()) {
                val safeName = ExternalUrlBuilder.sanitizeFileName(fileName ?: "rosafiesta-compartido.pdf")
                val tempPath = "${NSTemporaryDirectory()}$safeName"
                val wroteTemp = fileBytes.toIosNsData().writeToFile(tempPath, atomically = true)
                if (wroteTemp) {
                    items.add(NSURL.fileURLWithPath(tempPath))
                }
            }
        }
        if (items.isEmpty()) return false

        dispatch_async(dispatch_get_main_queue()) {
            val activityController = UIActivityViewController(
                activityItems = items,
                applicationActivities = null,
            )
            activityController.popoverPresentationController?.sourceView = rootController.view
            rootController.presentViewController(activityController, animated = true, completion = null)
        }
        return true
    }

    override fun saveDocument(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String? {
        if (bytes.isEmpty()) return null
        val safeName = ExternalUrlBuilder.sanitizeFileName(fileName)
        val documentDirs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val baseDir = (documentDirs.firstOrNull() as? String) ?: NSTemporaryDirectory().trimEnd('/')
        val fullPath = "$baseDir/$safeName"
        val wroteOk = bytes.toIosNsData().writeToFile(fullPath, atomically = true)
        if (!wroteOk) return null

        val rootController = resolveActiveRootViewController()
        if (rootController != null) {
            val fileUrl = NSURL.fileURLWithPath(fullPath)
            dispatch_async(dispatch_get_main_queue()) {
                val shareController = UIActivityViewController(
                    activityItems = listOf(fileUrl),
                    applicationActivities = null,
                )
                shareController.popoverPresentationController?.sourceView = rootController.view
                rootController.presentViewController(shareController, animated = true, completion = null)
            }
        }
        return fullPath
    }

    override suspend fun pickMediaFile(allowedExtensions: List<String>): PickedMediaFile? {
        val customPicker = registeredIosMediaPicker
        if (customPicker != null) {
            return customPicker.invoke(allowedExtensions)
        }

        val rootController = resolveActiveRootViewController() ?: return null
        val documentTypes = mapExtensionsToUtiStrings(allowedExtensions)

        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                @Suppress("DEPRECATION")
                val pickerController = UIDocumentPickerViewController(
                    documentTypes = documentTypes,
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
                )
                val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                    override fun documentPicker(
                        controller: UIDocumentPickerViewController,
                        didPickDocumentsAtURLs: List<*>,
                    ) {
                        retainedDocumentDelegate = null
                        val firstUrl = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                        val pickedFile = firstUrl?.let { pickedUrl -> readNsUrlToPickedFile(pickedUrl) }
                        if (continuation.isActive) {
                            continuation.resume(pickedFile)
                        }
                    }

                    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                        retainedDocumentDelegate = null
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }
                retainedDocumentDelegate = delegate
                pickerController.delegate = delegate
                pickerController.popoverPresentationController?.sourceView = rootController.view
                rootController.presentViewController(pickerController, animated = true, completion = null)
            }
        }
    }

    private fun readNsUrlToPickedFile(url: NSURL): PickedMediaFile? {
        val scopedStarted = url.startAccessingSecurityScopedResource()
        try {
            val nsData = NSData.dataWithContentsOfURL(url) ?: return null
            val byteLength = nsData.length.toInt()
            val bytes = if (byteLength == 0) {
                ByteArray(0)
            } else {
                nsData.bytes?.readBytes(byteLength) ?: return null
            }
            val resolvedName = url.lastPathComponent ?: "adjunto.dat"
            return PickedMediaFile(
                fileName = resolvedName,
                mimeType = ExternalUrlBuilder.inferMimeType(resolvedName),
                sizeBytes = bytes.size.toLong(),
                bytes = bytes,
            )
        } finally {
            if (scopedStarted) {
                url.stopAccessingSecurityScopedResource()
            }
        }
    }

    private fun mapExtensionsToUtiStrings(extensions: List<String>): List<String> {
        val mapped = extensions.mapNotNull { ext ->
            when (ext.trim().trimStart('.').lowercase()) {
                "jpg", "jpeg" -> "public.jpeg"
                "png" -> "public.png"
                "webp" -> "org.webmproject.webp"
                "svg" -> "public.svg-image"
                "pdf" -> "com.adobe.pdf"
                else -> null
            }
        }.distinct()
        return mapped.ifEmpty { listOf("public.image", "com.adobe.pdf") }
    }

    private fun resolveActiveRootViewController(): UIViewController? {
        @Suppress("DEPRECATION")
        val windows = UIApplication.sharedApplication.windows
        for (windowCandidate in windows) {
            val uiWindow = windowCandidate as? UIWindow ?: continue
            if (uiWindow.isKeyWindow()) {
                return topPresentedController(uiWindow.rootViewController)
            }
        }
        @Suppress("DEPRECATION")
        val fallbackRoot = UIApplication.sharedApplication.keyWindow?.rootViewController
        return topPresentedController(fallbackRoot)
    }

    private fun topPresentedController(base: UIViewController?): UIViewController? {
        var current = base
        for (depth in 0 until 12) {
            val presented = current?.presentedViewController ?: break
            current = presented
        }
        return current
    }

    override suspend fun launchOAuthBrowserFlow(
        resolveAuthorizeUrl: suspend (redirectUri: String) -> String?,
    ): OAuthBrowserOutcome {
        val redirectUri = "rosafiesta://oauth/callback"
        val authorizeUrl = resolveAuthorizeUrl(redirectUri)
            ?: return OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_START_FAILED")
        val nsUrl = NSURL.URLWithString(authorizeUrl)
            ?: return OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_INVALID_URL")

        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val contextProvider = object : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
                    override fun presentationAnchorForWebAuthenticationSession(
                        session: ASWebAuthenticationSession,
                    ): ASPresentationAnchor {
                        @Suppress("DEPRECATION")
                        val windows = UIApplication.sharedApplication.windows
                        val keyWindow = windows.firstOrNull { win -> (win as? UIWindow)?.isKeyWindow() == true } as? UIWindow
                        return keyWindow ?: UIWindow()
                    }
                }
                val session = ASWebAuthenticationSession(
                    uRL = nsUrl,
                    callbackURLScheme = "rosafiesta",
                    completionHandler = { callbackUrl, error ->
                        retainedOAuthSession = null
                        retainedPresentationContext = null
                        if (!continuation.isActive) return@ASWebAuthenticationSession
                        if (error != null) {
                            continuation.resume(OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_CANCELLED"))
                            return@ASWebAuthenticationSession
                        }
                        val resolvedCallbackUrl = callbackUrl
                        if (resolvedCallbackUrl == null) {
                            continuation.resume(OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_CANCELLED"))
                            return@ASWebAuthenticationSession
                        }
                        continuation.resume(parseIosOAuthCallbackUrl(resolvedCallbackUrl, redirectUri))
                    },
                )
                retainedOAuthSession = session
                retainedPresentationContext = contextProvider
                session.presentationContextProvider = contextProvider
                session.prefersEphemeralWebBrowserSession = false
                if (!session.start()) {
                    retainedOAuthSession = null
                    retainedPresentationContext = null
                    if (continuation.isActive) {
                        continuation.resume(OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_BROWSER_UNAVAILABLE"))
                    }
                }
            }
        }
    }

    override fun registerPushNotifications(
        appVariant: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        pendingIosPushRegistrationCallback = onResult
        val existingToken = registeredIosApnsToken
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            if (!granted) {
                pendingIosPushRegistrationCallback = null
                onResult("ios", null)
                return@requestAuthorizationWithOptions
            }
            dispatch_async(dispatch_get_main_queue()) {
                UIApplication.sharedApplication.registerForRemoteNotifications()
            }
            if (!existingToken.isNullOrBlank()) {
                pendingIosPushRegistrationCallback = null
                onResult("ios", existingToken)
            }
        }
    }

    override fun showLocalNotification(title: String, body: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val content = UNMutableNotificationContent().apply {
            setTitle(cleanTitle)
            setBody(body.trim())
        }
        val identifier = NSUUID().UUIDString()
        val request = UNNotificationRequest.requestWithIdentifier(identifier, content = content, trigger = null)
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, withCompletionHandler = null)
    }

    private fun parseIosOAuthCallbackUrl(callbackUrl: NSURL, redirectUri: String): OAuthBrowserOutcome {
        val components = NSURLComponents.componentsWithURL(callbackUrl, resolvingAgainstBaseURL = false)
        var code: String? = null
        var state: String? = null
        var errorCode: String? = null
        val queryItems = components?.queryItems.orEmpty()
        for (rawItem in queryItems) {
            val queryItem = rawItem as? platform.Foundation.NSURLQueryItem ?: continue
            val itemName = queryItem.name
            val itemValue = queryItem.value
            when (itemName) {
                "code" -> code = itemValue
                "state" -> state = itemValue
                "error" -> errorCode = itemValue
            }
        }
        return OAuthBrowserOutcome(
            code = code,
            state = state,
            redirectUri = redirectUri,
            errorCode = errorCode,
        )
    }
}

private fun ByteArray.toIosNsData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

public actual fun createPlatformSoundPlayer(): SoundPlayer = IosSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage = IosKeychainSecureStorage()

public actual fun createPlatformVoiceInputMonitor(): VoiceInputMonitor = IosVoiceInputMonitor()

public actual fun createPlatformActions(): PlatformActions = IosPlatformActions()
