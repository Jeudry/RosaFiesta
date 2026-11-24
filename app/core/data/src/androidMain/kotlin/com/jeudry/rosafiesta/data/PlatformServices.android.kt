package com.jeudry.rosafiesta.data

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import com.jeudry.rosafiesta.domain.platform.ExternalPaymentCallbackBridge
import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import com.jeudry.rosafiesta.domain.platform.OAuthBrowserOutcome
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.security.KeyStore
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Volatile
private var registeredAndroidMediaPicker: (suspend (List<String>) -> PickedMediaFile?)? = null

@Volatile
private var registeredAndroidAudioPermissionRequester: (() -> Unit)? = null

@Volatile
private var registeredAndroidDeviceAuthActivity: Activity? = null

@Volatile
private var registeredAndroidCredentialLauncher: ((Intent, (Boolean) -> Unit) -> Unit)? = null

@Volatile
private var pendingAndroidOAuthCallback: CompletableDeferred<OAuthBrowserOutcome>? = null

@Volatile
private var registeredAndroidPushTokenProvider: ((appVariant: String, onResult: (String?) -> Unit) -> Unit)? = null

private const val ANDROID_OAUTH_REDIRECT_URI = "rosafiesta://oauth/callback"
private const val ANDROID_PAYMENT_REDIRECT_PREFIX = "rosafiesta://payment"
private const val ANDROID_API_URL_METADATA_KEY = "com.jeudry.rosafiesta.API_BASE_URL"
private const val ANDROID_NOTIFICATION_CHANNEL_ID = "rosafiesta_alerts"
private val androidNotificationIdCounter = AtomicInteger(2100)

/**
 * Registers an optional host push token SDK bridge (for example FCM in signed builds).
 */
public fun registerAndroidPushTokenProvider(
    provider: ((appVariant: String, onResult: (String?) -> Unit) -> Unit)?,
) {
    registeredAndroidPushTokenProvider = provider
}

/**
 * Delivers an incoming deep link URI (`rosafiesta://oauth/callback` or `rosafiesta://payment/...`)
 * from Activity `onCreate` or `onNewIntent`.
 */
public fun dispatchAndroidIncomingUri(uriString: String?) {
    val cleanUri = uriString?.trim().orEmpty()
    if (cleanUri.isEmpty()) return
    if (cleanUri.startsWith(ANDROID_OAUTH_REDIRECT_URI)) {
        dispatchAndroidOAuthRedirectUri(cleanUri)
        return
    }
    if (cleanUri.startsWith(ANDROID_PAYMENT_REDIRECT_PREFIX)) {
        ExternalPaymentCallbackBridge.dispatchPaymentDeepLink(cleanUri)
    }
}

/**
 * Delivers an incoming `rosafiesta://oauth/callback?...` deep link URI from Activity `onCreate` or `onNewIntent`.
 */
public fun dispatchAndroidOAuthRedirectUri(uriString: String?) {
    val cleanUri = uriString?.trim().orEmpty()
    if (!cleanUri.startsWith(ANDROID_OAUTH_REDIRECT_URI)) return
    val parsed = runCatching { Uri.parse(cleanUri) }.getOrNull() ?: return
    val deferred = pendingAndroidOAuthCallback ?: return
    pendingAndroidOAuthCallback = null
    deferred.complete(
        OAuthBrowserOutcome(
            code = parsed.getQueryParameter("code"),
            state = parsed.getQueryParameter("state"),
            redirectUri = ANDROID_OAUTH_REDIRECT_URI,
            errorCode = parsed.getQueryParameter("error"),
        ),
    )
}

/**
 * Registers an Activity-backed document/media picker bridge (such as `ActivityResultContracts.OpenDocument`)
 * so [PlatformActions.pickMediaFile] can open the system picker from Android UI hosts.
 */
public fun registerAndroidMediaPickerBridge(
    picker: (suspend (List<String>) -> PickedMediaFile?)?,
) {
    registeredAndroidMediaPicker = picker
}

/**
 * Registers the foreground Android [Activity] and optional legacy credential launcher for [PlatformActions.authenticateDeviceOwner].
 */
public fun registerAndroidDeviceAuthBridge(
    activity: Activity?,
    credentialLauncher: ((Intent, (Boolean) -> Unit) -> Unit)? = null,
) {
    registeredAndroidDeviceAuthActivity = activity
    registeredAndroidCredentialLauncher = credentialLauncher
}

/**
 * Registers the foreground Android [Activity] audio permission prompt callback so
 * [AndroidVoiceInputMonitor] can request `RECORD_AUDIO` interactively when missing.
 */
public fun registerAndroidAudioPermissionRequester(
    requester: (() -> Unit)?,
) {
    registeredAndroidAudioPermissionRequester = requester
}

/**
 * Reads a content [Uri] selected from the Android system file picker into a [PickedMediaFile].
 */
public fun readAndroidPickedUri(context: Context, uri: Uri): PickedMediaFile? = runCatching {
    val resolver = context.contentResolver
    var resolvedName = "archivo_adjunto"
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0) {
                val candidate = cursor.getString(nameIdx)?.trim()
                if (!candidate.isNullOrEmpty()) {
                    resolvedName = candidate
                }
            }
        }
    }
    val bytes = resolver.openInputStream(uri)?.use { stream -> stream.readBytes() } ?: return null
    val resolvedMime = resolver.getType(uri) ?: ExternalUrlBuilder.inferMimeType(resolvedName)
    PickedMediaFile(
        fileName = resolvedName,
        mimeType = resolvedMime,
        sizeBytes = bytes.size.toLong(),
        bytes = bytes,
    )
}.getOrNull()

internal class AndroidSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = AppAudioConfig.DEFAULT_VOLUME
    override var enabledCategories: Set<SoundEffectCategory> = AppAudioConfig.DEFAULT_CATEGORIES

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "rf-android-sfx").apply { isDaemon = true }
    }

    override fun play(effect: SoundEffect) {
        if (!canPlayEffect(effect)) return
        val wavBytes = PcmSoundSynthesizer.synthesizeWav(effect, soundVolume)
        if (wavBytes.size <= 44) return
        val pcmBytes = wavBytes.copyOfRange(44, wavBytes.size)
        val totalFrames = pcmBytes.size / 2
        if (totalFrames <= 0) return

        executor.execute {
            playStaticPcmAndRelease(pcmBytes, totalFrames, effect.durationMs)
        }
    }

    private fun playStaticPcmAndRelease(pcmBytes: ByteArray, totalFrames: Int, durationMs: Int) {
        var createdTrack: AudioTrack? = null
        val released = AtomicBoolean(false)
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(PcmSoundSynthesizer.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(pcmBytes.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            createdTrack = track

            track.setNotificationMarkerPosition(totalFrames)
            track.setPlaybackPositionUpdateListener(
                object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(audioTrack: AudioTrack?) {
                        releaseTrackOnce(track, released)
                    }

                    override fun onPeriodicNotification(audioTrack: AudioTrack?) = Unit
                },
                mainHandler,
            )

            val written = track.write(pcmBytes, 0, pcmBytes.size)
            if (written <= 0) {
                releaseTrackOnce(track, released)
                return
            }
            track.play()
            val fallbackDelayMs = durationMs.coerceAtLeast(50).toLong() + 250L
            executor.schedule(
                { releaseTrackOnce(track, released) },
                fallbackDelayMs,
                TimeUnit.MILLISECONDS,
            )
        } catch (_: Throwable) {
            if (createdTrack != null) {
                releaseTrackOnce(createdTrack, released)
            }
        }
    }

    private fun releaseTrackOnce(track: AudioTrack, released: AtomicBoolean) {
        if (!released.compareAndSet(false, true)) return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
        }
        runCatching {
            track.release()
        }
    }
}

internal class AndroidKeystoreSecureStorage(context: Context) : SecureStorage {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    override suspend fun get(key: String): String? {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return null
        val stored = preferences.getString(cleanKey, null) ?: return null
        return try {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            if (bytes.size <= IV_BYTES) {
                preferences.edit().remove(cleanKey).apply()
                return null
            }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateSecretKey()
            val gcmSpec = GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Throwable) {
            preferences.edit().remove(cleanKey).apply()
            null
        }
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        val plaintextBytes = value.trim().toByteArray(Charsets.UTF_8)
        val encryptedPayload = encryptWithHardwareKeystore(plaintextBytes)
        val encoded = Base64.encodeToString(encryptedPayload, Base64.NO_WRAP)
        preferences.edit().putString(cleanKey, encoded).apply()
    }

    override suspend fun remove(key: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        preferences.edit().remove(cleanKey).apply()
    }

    private fun encryptWithHardwareKeystore(plaintextBytes: ByteArray): ByteArray = try {
        encryptOnce(plaintextBytes, getOrCreateSecretKey())
    } catch (_: KeyPermanentlyInvalidatedException) {
        deleteInvalidatedSecretKey()
        encryptOnce(plaintextBytes, getOrCreateSecretKey())
    }

    private fun encryptOnce(plaintextBytes: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ivBytes = cipher.iv
        val ciphertextBytes = cipher.doFinal(plaintextBytes)
        val output = ByteArray(ivBytes.size + ciphertextBytes.size)
        ivBytes.copyInto(output, destinationOffset = 0)
        ciphertextBytes.copyInto(output, destinationOffset = ivBytes.size)
        return output
    }

    private fun deleteInvalidatedSecretKey() {
        runCatching {
            val store = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
            if (store.containsAlias(KEY_ALIAS)) {
                store.deleteEntry(KEY_ALIAS)
            }
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        val existingKey = store.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_BITS)
            .build()
        generator.init(keySpec)
        return generator.generateKey()
    }

    private companion object {
        const val PREFS_FILE = "rf_secure_storage"
        const val KEYSTORE_NAME = "AndroidKeyStore"
        const val KEY_ALIAS = "rosafiesta_session_aes_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BITS = 256
        const val TAG_BITS = 128
        const val IV_BYTES = 12
    }
}

internal class AndroidVoiceInputMonitor(
    private val contextProvider: () -> Context?,
) : VoiceInputMonitor {
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

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeRecognizer: SpeechRecognizer? = null

    override fun startListening(onTranscriptResult: (String) -> Unit) {
        mainHandler.post {
            startSpeechRecognizerOnMainThread(onTranscriptResult)
        }
    }

    override fun stopListening() {
        mainHandler.post {
            teardownRecognizer(stopOnly = true)
            _isListening.value = false
            _isTranscribing.value = false
            _amplitudeLevel.value = 0f
        }
    }

    private fun startSpeechRecognizerOnMainThread(onTranscriptResult: (String) -> Unit) {
        val context = contextProvider()
        if (context == null) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Android application context is unavailable."
            _isListening.value = false
            return
        }

        val permissionState = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            registeredAndroidAudioPermissionRequester?.invoke()
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Permiso de micrófono requerido. Acéptalo e intenta de nuevo."
            _isListening.value = false
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "Speech recognition service is unavailable on this Android device."
            _isListening.value = false
            return
        }

        teardownRecognizer(stopOnly = false)
        _errorMessage.value = null
        _recognizedTranscript.value = ""
        _capturedWavBytes.value = null
        _hasHardwareMicrophone.value = true
        _isListening.value = true

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
        activeRecognizer = recognizer
        recognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _errorMessage.value = null
                }

                override fun onBeginningOfSpeech() {
                    _isTranscribing.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.02f, 1.0f)
                    _amplitudeLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    _amplitudeLevel.value = 0f
                }

                override fun onError(errorCode: Int) {
                    _errorMessage.value = describeSpeechError(errorCode)
                    _isListening.value = false
                    _isTranscribing.value = false
                    _amplitudeLevel.value = 0f
                    teardownRecognizer(stopOnly = false)
                }

                override fun onResults(results: Bundle?) {
                    val text = extractTopRecognitionCandidate(results)
                    _isListening.value = false
                    _isTranscribing.value = false
                    _amplitudeLevel.value = 0f
                    if (text.isNotEmpty()) {
                        _recognizedTranscript.value = text
                        onTranscriptResult(text)
                    }
                    teardownRecognizer(stopOnly = false)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialText = extractTopRecognitionCandidate(partialResults)
                    if (partialText.isNotEmpty()) {
                        _recognizedTranscript.value = partialText
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            },
        )

        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-DO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-DO")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        recognizer.startListening(recognitionIntent)
    }

    private fun extractTopRecognitionCandidate(bundle: Bundle?): String {
        val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return ""
        return matches.firstOrNull { candidate -> candidate.isNotBlank() }?.trim().orEmpty()
    }

    private fun teardownRecognizer(stopOnly: Boolean) {
        val recognizer = activeRecognizer ?: return
        if (stopOnly) {
            runCatching { recognizer.stopListening() }
        }
        runCatching { recognizer.cancel() }
        runCatching { recognizer.destroy() }
        activeRecognizer = null
    }

    private fun describeSpeechError(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Microphone audio capture error."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
        SpeechRecognizer.ERROR_NO_MATCH -> "No recognizable speech was detected."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is currently busy."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input was heard before timeout."
        else -> "Speech recognition failed with code $errorCode."
    }
}

internal class AndroidPlatformActions(
    private val contextProvider: () -> Context?,
) : PlatformActions {
    override fun defaultApiBaseUrl(): String {
        val propOverride = System.getProperty("ROSAFIESTA_API_URL")?.trim().orEmpty()
        if (propOverride.isNotEmpty()) {
            return ExternalUrlBuilder.normalizeBaseUrl(propOverride)
        }
        val envOverride = System.getenv("ROSAFIESTA_API_URL")?.trim().orEmpty()
        if (envOverride.isNotEmpty()) {
            return ExternalUrlBuilder.normalizeBaseUrl(envOverride)
        }
        val manifestOverride = readManifestApiBaseUrl()
        if (manifestOverride.isNotEmpty()) {
            return ExternalUrlBuilder.normalizeBaseUrl(manifestOverride)
        }
        return if (isRunningOnAndroidEmulator()) {
            "http://10.0.2.2:8788"
        } else {
            "http://localhost:8788"
        }
    }

    private fun readManifestApiBaseUrl(): String {
        val context = contextProvider() ?: return ""
        return runCatching {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA,
                )
            }
            appInfo.metaData?.getString(ANDROID_API_URL_METADATA_KEY)?.trim().orEmpty()
        }.getOrDefault("")
    }

    override fun openExternalUrl(url: String): Boolean {
        val cleanUrl = url.trim()
        if (cleanUrl.isEmpty()) return false
        val context = contextProvider() ?: return false
        return runCatching {
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
            true
        }.getOrDefault(false)
    }

    override fun shareContent(
        title: String,
        text: String,
        fileName: String?,
        fileBytes: ByteArray?,
        mimeType: String,
    ): Boolean {
        val context = contextProvider() ?: return false
        return runCatching {
            var exportedLocation = ""
            var attachmentUri: Uri? = null
            var resolvedAttachmentMime = "text/plain"
            if (fileBytes != null) {
                if (fileBytes.isNotEmpty()) {
                    val safeName = ExternalUrlBuilder.sanitizeFileName(fileName ?: "rosafiesta-compartido.pdf")
                    resolvedAttachmentMime = mimeType.trim().ifEmpty { ExternalUrlBuilder.inferMimeType(safeName) }
                    attachmentUri = resolveShareableFileUri(context, safeName, fileBytes)
                    if (attachmentUri == null) {
                        exportedLocation = saveDocument(safeName, resolvedAttachmentMime, fileBytes).orEmpty()
                    }
                }
            }
            val bodyText = buildString {
                if (text.isNotBlank()) append(text.trim())
                if (exportedLocation.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append(exportedLocation)
                }
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                if (attachmentUri != null) {
                    type = resolvedAttachmentMime
                    putExtra(Intent.EXTRA_STREAM, attachmentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_SUBJECT, title.trim())
                putExtra(Intent.EXTRA_TEXT, bodyText.ifEmpty { title.trim() })
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, title.trim().ifEmpty { "Compartir" }).apply {
                if (attachmentUri != null) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }

    private fun resolveShareableFileUri(
        context: Context,
        safeName: String,
        fileBytes: ByteArray,
    ): Uri? = runCatching {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val exportFile = File(exportDir, safeName)
        exportFile.writeBytes(fileBytes)
        val fileProviderClass = Class.forName("androidx.core.content.FileProvider")
        val getUriForFile = fileProviderClass.getMethod(
            "getUriForFile",
            Context::class.java,
            String::class.java,
            File::class.java,
        )
        val authority = "${context.packageName}.fileprovider"
        getUriForFile.invoke(null, context, authority, exportFile) as? Uri
    }.getOrNull()

    override fun saveDocument(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String? {
        if (bytes.isEmpty()) return null
        val context = contextProvider() ?: return null
        val safeName = ExternalUrlBuilder.sanitizeFileName(fileName)
        val resolvedMime = mimeType.trim().ifEmpty { ExternalUrlBuilder.inferMimeType(safeName) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val savedMediaUri = saveIntoMediaStoreDownloads(context, safeName, resolvedMime, bytes)
            if (savedMediaUri != null) {
                return savedMediaUri
            }
        }

        return runCatching {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.cacheDir, "exports")
            if (!baseDir.exists()) {
                baseDir.mkdirs()
            }
            val outputFile = File(baseDir, safeName)
            outputFile.writeBytes(bytes)
            outputFile.absolutePath
        }.getOrNull()
    }

    override suspend fun pickMediaFile(allowedExtensions: List<String>): PickedMediaFile? {
        val bridge = registeredAndroidMediaPicker ?: return null
        return bridge.invoke(allowedExtensions)
    }

    override fun isOnWifiNetwork(): Boolean {
        val context = contextProvider() ?: return false
        val connectivity = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val activeNetwork = connectivity.activeNetwork ?: return false
        val capabilities = connectivity.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun authenticateDeviceOwner(
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val activity = registeredAndroidDeviceAuthActivity
        if (activity == null) {
            onResult(false, "BIOMETRIC_UNAVAILABLE")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticateWithBiometricPrompt(activity, promptTitle, promptSubtitle, onResult)
            return
        }
        authenticateWithKeyguardCredential(activity, promptTitle, promptSubtitle, onResult)
    }

    @Suppress("DEPRECATION")
    private fun authenticateWithBiometricPrompt(
        activity: Activity,
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val builder = BiometricPrompt.Builder(activity)
            .setTitle(promptTitle.ifBlank { "Desbloquear RosaFiesta" })
            .setSubtitle(promptSubtitle.ifBlank { "Confirma tu identidad para continuar" })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            builder.setAllowedAuthenticators(authenticators)
        } else {
            builder.setDeviceCredentialAllowed(true)
        }
        val prompt = builder.build()
        val signal = CancellationSignal()
        prompt.authenticate(
            signal,
            activity.mainExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    onResult(true, null)
                }

                override fun onAuthenticationFailed() {
                    onResult(false, "BIOMETRIC_FAILED")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    val isHardwareMissing = when (errorCode) {
                        BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT,
                        BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS,
                        BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                        -> true

                        else -> false
                    }
                    val reason = if (isHardwareMissing) "BIOMETRIC_UNAVAILABLE" else "BIOMETRIC_CANCELLED"
                    onResult(false, reason)
                }
            },
        )
    }

    @Suppress("DEPRECATION")
    private fun authenticateWithKeyguardCredential(
        activity: Activity,
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val keyguard = activity.getSystemService(KeyguardManager::class.java)
        val launcher = registeredAndroidCredentialLauncher
        val canPromptCredential = when {
            keyguard == null -> false
            !keyguard.isDeviceSecure -> false
            else -> launcher != null
        }
        if (!canPromptCredential) {
            onResult(false, "BIOMETRIC_UNAVAILABLE")
            return
        }
        val activeKeyguard = keyguard ?: return
        val activeLauncher = launcher ?: return
        val confirmIntent = activeKeyguard.createConfirmDeviceCredentialIntent(
            promptTitle.ifBlank { "Desbloquear RosaFiesta" },
            promptSubtitle.ifBlank { "Ingresa el PIN o patrón del dispositivo" },
        )
        if (confirmIntent == null) {
            onResult(false, "BIOMETRIC_UNAVAILABLE")
            return
        }
        activeLauncher(confirmIntent) { succeeded ->
            onResult(succeeded, if (succeeded) null else "BIOMETRIC_CANCELLED")
        }
    }

    private fun saveIntoMediaStoreDownloads(
        context: Context,
        safeName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String? = runCatching {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, safeName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        val wroteOk = resolver.openOutputStream(itemUri)?.use { outputStream ->
            outputStream.write(bytes)
            outputStream.flush()
            true
        } ?: false
        if (!wroteOk) {
            resolver.delete(itemUri, null, null)
            return null
        }
        val completedValues = ContentValues().apply {
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        resolver.update(itemUri, completedValues, null, null)
        itemUri.toString()
    }.getOrNull()

    private fun isRunningOnAndroidEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.orEmpty().lowercase()
        if (fingerprint.contains("generic")) return true
        if (fingerprint.contains("emulator")) return true
        val model = Build.MODEL.orEmpty().lowercase()
        if (model.contains("sdk")) return true
        if (model.contains("emulator")) return true
        val hardware = Build.HARDWARE.orEmpty().lowercase()
        if (hardware.contains("ranchu")) return true
        return hardware.contains("goldfish")
    }

    override suspend fun launchOAuthBrowserFlow(
        resolveAuthorizeUrl: suspend (redirectUri: String) -> String?,
    ): OAuthBrowserOutcome {
        pendingAndroidOAuthCallback?.cancel()
        val deferred = CompletableDeferred<OAuthBrowserOutcome>()
        pendingAndroidOAuthCallback = deferred
        val authorizeUrl = resolveAuthorizeUrl(ANDROID_OAUTH_REDIRECT_URI)
        if (authorizeUrl == null) {
            if (pendingAndroidOAuthCallback === deferred) {
                pendingAndroidOAuthCallback = null
            }
            return OAuthBrowserOutcome(redirectUri = ANDROID_OAUTH_REDIRECT_URI, errorCode = "OAUTH_START_FAILED")
        }
        val opened = openExternalUrl(authorizeUrl)
        if (!opened) {
            if (pendingAndroidOAuthCallback === deferred) {
                pendingAndroidOAuthCallback = null
            }
            return OAuthBrowserOutcome(redirectUri = ANDROID_OAUTH_REDIRECT_URI, errorCode = "OAUTH_BROWSER_UNAVAILABLE")
        }
        return try {
            withTimeoutOrNull(180_000L) { deferred.await() }
                ?: OAuthBrowserOutcome(redirectUri = ANDROID_OAUTH_REDIRECT_URI, errorCode = "OAUTH_TIMEOUT")
        } finally {
            if (pendingAndroidOAuthCallback === deferred) {
                pendingAndroidOAuthCallback = null
            }
        }
    }

    override fun registerPushNotifications(
        appVariant: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        val context = contextProvider()
        if (context != null) {
            ensureAndroidNotificationChannel(context)
        }
        val provider = registeredAndroidPushTokenProvider
        if (provider != null) {
            provider.invoke(appVariant) { token ->
                onResult("android", token)
            }
            return
        }
        tryReadOptionalFirebaseToken(onResult)
    }

    private fun tryReadOptionalFirebaseToken(
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        val started = runCatching {
            val messagingClass = Class.forName("com.google.firebase.messaging.FirebaseMessaging")
            val getInstance = messagingClass.getMethod("getInstance")
            val instance = getInstance.invoke(null) ?: return@runCatching false
            val tokenTask = messagingClass.getMethod("getToken").invoke(instance) ?: return@runCatching false
            val onCompleteClass = Class.forName("com.google.android.gms.tasks.OnCompleteListener")
            val addListener = tokenTask.javaClass.getMethod("addOnCompleteListener", onCompleteClass)
            val listener = java.lang.reflect.Proxy.newProxyInstance(
                onCompleteClass.classLoader,
                arrayOf(onCompleteClass),
            ) { _, method, args ->
                if (method.name == "onComplete") {
                    val completedTask = args?.firstOrNull()
                    val token = extractCompletedTaskResult(completedTask)
                    onResult("android", token)
                }
                null
            }
            addListener.invoke(tokenTask, listener)
            true
        }.getOrDefault(false)
        if (!started) {
            onResult("android", null)
        }
    }

    private fun extractCompletedTaskResult(task: Any?): String? {
        if (task == null) return null
        return runCatching {
            val isSuccessful = task.javaClass.getMethod("isSuccessful").invoke(task) as? Boolean ?: false
            if (!isSuccessful) return@runCatching null
            (task.javaClass.getMethod("getResult").invoke(task) as? String)?.trim()?.ifEmpty { null }
        }.getOrNull()
    }

    override fun showLocalNotification(title: String, body: String) {
        val context = contextProvider() ?: return
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        ensureAndroidNotificationChannel(context)
        val iconRes = context.applicationInfo.icon
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, ANDROID_NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
        val notification = builder
            .setSmallIcon(if (iconRes != 0) iconRes else android.R.drawable.ic_dialog_info)
            .setContentTitle(cleanTitle)
            .setContentText(body.trim())
            .setAutoCancel(true)
            .build()
        manager.notify(androidNotificationIdCounter.incrementAndGet(), notification)
    }

    private fun ensureAndroidNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(ANDROID_NOTIFICATION_CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            ANDROID_NOTIFICATION_CHANNEL_ID,
            "Alertas RosaFiesta",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recordatorios de pago, eventos y cotizaciones de RosaFiesta"
        }
        manager.createNotificationChannel(channel)
    }
}

public actual fun createPlatformSoundPlayer(): SoundPlayer = AndroidSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage {
    val context = currentAndroidContext() ?: return InMemorySecureStorage()
    return AndroidKeystoreSecureStorage(context)
}

public actual fun createPlatformVoiceInputMonitor(): VoiceInputMonitor = AndroidVoiceInputMonitor(contextProvider = ::currentAndroidContext)

public actual fun createPlatformActions(): PlatformActions = AndroidPlatformActions(contextProvider = ::currentAndroidContext)
