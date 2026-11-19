package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import com.jeudry.rosafiesta.domain.platform.OAuthBrowserOutcome
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.NoOpVoiceInputMonitor
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import com.jeudry.rosafiesta.domain.sound.WavPcmEncoder
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilenameFilter
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineEvent
import javax.sound.sampled.TargetDataLine

internal class DesktopSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = AppAudioConfig.DEFAULT_VOLUME
    override var enabledCategories: Set<SoundEffectCategory> = AppAudioConfig.DEFAULT_CATEGORIES
    public var lastPlayedEffect: SoundEffect = SoundEffect.NONE
        private set

    private val wavCache = ConcurrentHashMap<Long, ByteArray>()

    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-sfx").apply { isDaemon = true }
    }

    override fun play(effect: SoundEffect) {
        if (!canPlayEffect(effect)) return
        lastPlayedEffect = effect
        val quantizedVolume = (soundVolume.coerceIn(0f, 1f) * 100f).toInt()
        val cacheKey = (effect.ordinal.toLong() shl 8) or quantizedVolume.toLong()
        val wavBytes = wavCache.getOrPut(cacheKey) {
            PcmSoundSynthesizer.synthesizeWav(effect, quantizedVolume / 100f)
        }
        if (wavBytes.isEmpty()) return
        audioExecutor.execute {
            playWavBytes(wavBytes)
        }
    }

    private fun playWavBytes(wavBytes: ByteArray) {
        val byteStream = ByteArrayInputStream(wavBytes)
        runCatching {
            AudioSystem.getAudioInputStream(byteStream).use { audioStream ->
                val clip = AudioSystem.getClip()
                clip.addLineListener { lineEvent ->
                    if (lineEvent.type == LineEvent.Type.STOP) {
                        runCatching { clip.close() }
                    }
                }
                try {
                    clip.open(audioStream)
                    clip.start()
                } catch (openError: Throwable) {
                    runCatching { clip.close() }
                    throw openError
                }
            }
        }
    }
}

internal class DesktopSecureStorage(
    private val directory: File = File(System.getProperty("user.home"), ".rosafiesta/secure"),
) : SecureStorage {
    private val secureRandom = SecureRandom()
    private val keyLock = Any()

    @Volatile
    private var cachedAesKey: SecretKeySpec? = null

    init {
        ensureRestrictedDirectory(directory)
    }

    override suspend fun get(key: String): String? {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return null
        val targetFile = encryptedFileFor(cleanKey)
        if (!targetFile.exists()) return null

        return runCatching {
            val payload = targetFile.readBytes()
            if (payload.size <= GCM_IV_BYTES) return null
            val secretKey = resolveOrCreateAesKey()
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_BITS, payload, 0, GCM_IV_BYTES)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val ciphertextLength = payload.size - GCM_IV_BYTES
            val decryptedBytes = cipher.doFinal(payload, GCM_IV_BYTES, ciphertextLength)
            String(decryptedBytes, Charsets.UTF_8)
        }.getOrNull()
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        val cleanValue = value.trim()

        ensureRestrictedDirectory(directory)
        val secretKey = resolveOrCreateAesKey()
        val ivBytes = ByteArray(GCM_IV_BYTES)
        secureRandom.nextBytes(ivBytes)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_BITS, ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val plaintextBytes = cleanValue.toByteArray(Charsets.UTF_8)
        val ciphertextBytes = cipher.doFinal(plaintextBytes)

        val combined = ByteArray(ivBytes.size + ciphertextBytes.size)
        ivBytes.copyInto(combined, destinationOffset = 0)
        ciphertextBytes.copyInto(combined, destinationOffset = ivBytes.size)

        val targetFile = encryptedFileFor(cleanKey)
        targetFile.writeBytes(combined)
        applyOwnerOnlyFilePermissions(targetFile)

        val legacyPlainFile = legacyFileFor(cleanKey)
        if (legacyPlainFile.exists()) {
            runCatching { legacyPlainFile.delete() }
        }
    }

    override suspend fun remove(key: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        runCatching { encryptedFileFor(cleanKey).delete() }
        runCatching { legacyFileFor(cleanKey).delete() }
    }

    private fun resolveOrCreateAesKey(): SecretKeySpec {
        val existing = cachedAesKey
        if (existing != null) return existing
        synchronized(keyLock) {
            val doubleChecked = cachedAesKey
            if (doubleChecked != null) return doubleChecked
            val keyMaterialFile = File(directory, KEYSTORE_MATERIAL_FILE)
            val material = loadOrGenerateKeyMaterial(keyMaterialFile)
            val derivedKey = derivePbkdf2AesKey(material.passphraseChars, material.saltBytes)
            cachedAesKey = derivedKey
            return derivedKey
        }
    }

    private fun loadOrGenerateKeyMaterial(keyFile: File): KeyDerivationMaterial {
        if (keyFile.exists()) {
            val loaded = runCatching { parseStoredKeyMaterial(keyFile.readText(Charsets.UTF_8)) }.getOrNull()
            if (loaded != null) {
                return loaded
            }
        }
        ensureRestrictedDirectory(directory)
        val randomSecretBytes = ByteArray(32)
        val saltBytes = ByteArray(PBKDF2_SALT_BYTES)
        secureRandom.nextBytes(randomSecretBytes)
        secureRandom.nextBytes(saltBytes)
        val encodedSecret = Base64.getEncoder().withoutPadding().encodeToString(randomSecretBytes)
        val encodedSalt = Base64.getEncoder().withoutPadding().encodeToString(saltBytes)
        keyFile.writeText("$encodedSecret:$encodedSalt", Charsets.UTF_8)
        applyOwnerOnlyFilePermissions(keyFile)
        return KeyDerivationMaterial(
            passphraseChars = encodedSecret.toCharArray(),
            saltBytes = saltBytes,
        )
    }

    private fun parseStoredKeyMaterial(rawContent: String): KeyDerivationMaterial? {
        val parts = rawContent.trim().split(":")
        if (parts.size != 2) return null
        val encodedSecret = parts[0].trim()
        val encodedSalt = parts[1].trim()
        if (encodedSecret.length < 24) return null
        val saltBytes = Base64.getDecoder().decode(encodedSalt)
        if (saltBytes.size < PBKDF2_SALT_BYTES) return null
        return KeyDerivationMaterial(
            passphraseChars = encodedSecret.toCharArray(),
            saltBytes = saltBytes,
        )
    }

    private fun derivePbkdf2AesKey(passphraseChars: CharArray, saltBytes: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(passphraseChars, saltBytes, PBKDF2_ITERATIONS, AES_KEY_BITS)
        try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val rawKeyBytes = factory.generateSecret(keySpec).encoded
            return SecretKeySpec(rawKeyBytes, "AES")
        } finally {
            keySpec.clearPassword()
            passphraseChars.fill('\u0000')
        }
    }

    private fun ensureRestrictedDirectory(targetDir: File) {
        runCatching {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val posixPerms = PosixFilePermissions.fromString("rwx------")
            Files.setPosixFilePermissions(targetDir.toPath(), posixPerms)
        }.onFailure {
            runCatching {
                targetDir.setReadable(false, false)
                targetDir.setReadable(true, true)
                targetDir.setWritable(false, false)
                targetDir.setWritable(true, true)
                targetDir.setExecutable(false, false)
                targetDir.setExecutable(true, true)
            }
        }
    }

    private fun applyOwnerOnlyFilePermissions(file: File) {
        runCatching {
            val posixPerms = PosixFilePermissions.fromString("rw-------")
            Files.setPosixFilePermissions(file.toPath(), posixPerms)
        }.onFailure {
            runCatching {
                file.setReadable(false, false)
                file.setReadable(true, true)
                file.setWritable(false, false)
                file.setWritable(true, true)
            }
        }
    }

    private fun encryptedFileFor(key: String): File {
        val sanitized = sanitizeEntryName(key)
        return File(directory, "$sanitized.enc")
    }

    private fun legacyFileFor(key: String): File {
        val sanitized = sanitizeEntryName(key)
        return File(directory, sanitized)
    }

    private fun sanitizeEntryName(rawKey: String): String = rawKey.trim().replace(Regex("[^A-Za-z0-9_-]"), "_")

    private class KeyDerivationMaterial(
        val passphraseChars: CharArray,
        val saltBytes: ByteArray,
    )

    private companion object {
        const val KEYSTORE_MATERIAL_FILE = ".keystore.key"
        const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        const val PBKDF2_ITERATIONS = 120_000
        const val PBKDF2_SALT_BYTES = 16
        const val AES_KEY_BITS = 256
        const val GCM_TAG_BITS = 128
        const val GCM_IV_BYTES = 12
    }
}

internal class DesktopVoiceInputMonitor : VoiceInputMonitor {
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

    private val activeFlag = AtomicBoolean(false)

    @Volatile
    private var activeLine: TargetDataLine? = null

    private val captureExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-mic").apply { isDaemon = true }
    }

    override fun startListening(onTranscriptResult: (String) -> Unit) {
        if (!activeFlag.compareAndSet(false, true)) return
        _errorMessage.value = null
        _recognizedTranscript.value = ""
        _capturedWavBytes.value = null
        _isTranscribing.value = false
        _isListening.value = true
        captureExecutor.execute {
            runHardwareCaptureLoop()
        }
    }

    override fun stopListening() {
        activeFlag.set(false)
        _isListening.value = false
        _isTranscribing.value = false
        _amplitudeLevel.value = 0f
        runCatching {
            activeLine?.stop()
        }
    }

    private fun runHardwareCaptureLoop() {
        val format = AudioFormat(SAMPLE_RATE_HZ.toFloat(), 16, 1, true, false)
        val lineInfo = DataLine.Info(TargetDataLine::class.java, format)
        val line = openHardwareTargetLine(lineInfo, format)
        if (line == null) {
            _hasHardwareMicrophone.value = false
            _errorMessage.value = "No se detectó un micrófono disponible en este dispositivo."
            _amplitudeLevel.value = 0f
            _isListening.value = false
            activeFlag.set(false)
            return
        }

        activeLine = line
        _hasHardwareMicrophone.value = true

        val recordedPcm = ByteArrayOutputStream(64_000)
        val chunkBuffer = ByteArray(1024)
        var speechChunksDetected = 0
        var trailingSilentChunks = 0

        try {
            for (chunkIndex in 0 until MAX_CAPTURE_CHUNKS) {
                if (!activeFlag.get()) break
                val bytesRead = runCatching { line.read(chunkBuffer, 0, chunkBuffer.size) }.getOrDefault(0)
                if (bytesRead > 1) {
                    recordedPcm.write(chunkBuffer, 0, bytesRead)
                    val normalizedRms = WavPcmEncoder.computeNormalizedRms(chunkBuffer, bytesRead)
                    _amplitudeLevel.value = normalizedRms

                    val isSpeechActive = normalizedRms >= SPEECH_ACTIVITY_THRESHOLD
                    if (isSpeechActive) {
                        speechChunksDetected += 1
                        trailingSilentChunks = 0
                    } else {
                        val hasSpokenAlready = speechChunksDetected >= MIN_SPEECH_CHUNKS
                        if (hasSpokenAlready) {
                            trailingSilentChunks += 1
                            if (trailingSilentChunks >= SILENCE_CHUNKS_TO_AUTO_STOP) {
                                break
                            }
                        } else if (chunkIndex >= MAX_INITIAL_SILENCE_CHUNKS) {
                            break
                        }
                    }
                }
            }
        } finally {
            runCatching {
                line.stop()
                line.close()
            }
            activeLine = null
            activeFlag.set(false)
            _isListening.value = false
            _amplitudeLevel.value = 0f
        }

        val pcmBytes = recordedPcm.toByteArray()
        if (pcmBytes.isNotEmpty()) {
            val wavRecording = WavPcmEncoder.encodeMono16BitPcmToWav(pcmBytes, SAMPLE_RATE_HZ)
            _capturedWavBytes.value = wavRecording
        } else {
            _errorMessage.value = "No se captó audio del micrófono. Verifica los permisos o el volumen de entrada."
        }
    }

    private fun openHardwareTargetLine(info: DataLine.Info, format: AudioFormat): TargetDataLine? = runCatching {
        if (!AudioSystem.isLineSupported(info)) return null
        val line = AudioSystem.getLine(info) as? TargetDataLine ?: return null
        line.open(format, 2048)
        line.start()
        line
    }.getOrNull()

    private companion object {
        const val SAMPLE_RATE_HZ = 16_000
        const val SPEECH_ACTIVITY_THRESHOLD = 0.018f
        const val MIN_SPEECH_CHUNKS = 3
        const val SILENCE_CHUNKS_TO_AUTO_STOP = 32
        const val MAX_INITIAL_SILENCE_CHUNKS = 120
        const val MAX_CAPTURE_CHUNKS = 300
    }
}

internal class DesktopPlatformActions : PlatformActions {
    override fun defaultApiBaseUrl(): String {
        val propOverride = System.getProperty("ROSAFIESTA_API_URL")?.trim().orEmpty()
        if (propOverride.isNotEmpty()) {
            return ExternalUrlBuilder.normalizeBaseUrl(propOverride)
        }
        val envOverride = System.getenv("ROSAFIESTA_API_URL")?.trim().orEmpty()
        return ExternalUrlBuilder.normalizeBaseUrl(envOverride)
    }

    override fun openExternalUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        val parsedUri = runCatching { URI(trimmed) }.getOrNull() ?: return false
        val scheme = parsedUri.scheme?.lowercase().orEmpty()
        if (scheme.isEmpty()) return false

        val canUseAwtDesktop = !GraphicsEnvironment.isHeadless()
        if (canUseAwtDesktop) {
            val desktopSupported = Desktop.isDesktopSupported()
            if (desktopSupported) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    val browsed = runCatching {
                        desktop.browse(parsedUri)
                        true
                    }.getOrDefault(false)
                    if (browsed) return true
                }
            }
        }
        return launchSystemUrlOpener(trimmed)
    }

    override fun shareContent(
        title: String,
        text: String,
        fileName: String?,
        fileBytes: ByteArray?,
        mimeType: String,
    ): Boolean {
        val hasAttachment = fileBytes != null
        if (hasAttachment) {
            if (fileBytes.isNotEmpty()) {
                val targetName = fileName ?: "rosafiesta-compartido.pdf"
                val savedPath = saveDocument(targetName, mimeType, fileBytes) ?: return false
                revealOrOpenFileOnDesktop(File(savedPath))
                return true
            }
        }

        val combinedText = buildString {
            if (title.isNotBlank()) {
                append(title.trim())
            }
            if (text.isNotBlank()) {
                if (isNotEmpty()) append("\n")
                append(text.trim())
            }
        }
        if (combinedText.isEmpty()) return false
        if (GraphicsEnvironment.isHeadless()) return false
        return runCatching {
            val selection = StringSelection(combinedText)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
            true
        }.getOrDefault(false)
    }

    override fun saveDocument(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String? {
        if (bytes.isEmpty()) return null
        val safeName = ExternalUrlBuilder.sanitizeFileName(fileName)
        val downloadsDir = resolveDownloadsDirectory()
        val useDirectDownloads = isHeadlessOrAutomatedEnvironment()
        if (useDirectDownloads) {
            return writeBytesToDirectory(downloadsDir, safeName, bytes)
        }

        val chosenFile = runCatching {
            val dialog = FileDialog(null as Frame?, "Guardar documento RosaFiesta", FileDialog.SAVE)
            dialog.directory = downloadsDir.absolutePath
            dialog.file = safeName
            dialog.isVisible = true
            val selectedFile = dialog.file
            val selectedDir = dialog.directory
            dialog.dispose()
            if (selectedFile != null) {
                val resolvedParent = selectedDir?.let { dirPath -> File(dirPath) } ?: downloadsDir
                File(resolvedParent, ExternalUrlBuilder.sanitizeFileName(selectedFile))
            } else {
                null
            }
        }.getOrNull() ?: return null

        return runCatching {
            chosenFile.parentFile?.mkdirs()
            chosenFile.writeBytes(bytes)
            chosenFile.absolutePath
        }.getOrNull()
    }

    override suspend fun pickMediaFile(allowedExtensions: List<String>): PickedMediaFile? {
        if (isHeadlessOrAutomatedEnvironment()) return null
        val normalizedExtensions = allowedExtensions
            .map { ext -> ext.trim().trimStart('.').lowercase() }
            .filter { ext -> ext.isNotEmpty() }
            .toSet()

        return withContext(Dispatchers.IO) {
            runCatching {
                val dialog = FileDialog(null as Frame?, "Seleccionar archivo, imagen o SVG", FileDialog.LOAD)
                if (normalizedExtensions.isNotEmpty()) {
                    dialog.filenameFilter = FilenameFilter { _, candidateName ->
                        val ext = candidateName.substringAfterLast('.', "").lowercase()
                        normalizedExtensions.contains(ext)
                    }
                }
                dialog.isVisible = true
                val chosenName = dialog.file
                val chosenDir = dialog.directory
                dialog.dispose()
                if (chosenName == null) return@runCatching null
                val parent = chosenDir?.let { path -> File(path) } ?: File(System.getProperty("user.home"))
                val targetFile = File(parent, chosenName)
                if (!targetFile.isFile) return@runCatching null
                val fileBytes = targetFile.readBytes()
                PickedMediaFile(
                    fileName = targetFile.name,
                    mimeType = ExternalUrlBuilder.inferMimeType(targetFile.name),
                    sizeBytes = fileBytes.size.toLong(),
                    bytes = fileBytes,
                )
            }.getOrNull()
        }
    }

    override fun isOnWifiNetwork(): Boolean {
        return runCatching {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return@runCatching false
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (!iface.isUp) continue
                if (iface.isLoopback) continue
                if (iface.isVirtual) continue
                val hasAddresses = iface.inetAddresses.hasMoreElements()
                if (hasAddresses) return@runCatching true
            }
            false
        }.getOrDefault(false)
    }

    private val ownerAuthExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-desktop-owner-auth").apply { isDaemon = true }
    }

    override fun authenticateDeviceOwner(
        promptTitle: String,
        promptSubtitle: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        if (isHeadlessOrAutomatedEnvironment()) {
            onResult(false, "DEVICE_AUTH_UNAVAILABLE")
            return
        }
        val osName = System.getProperty("os.name").orEmpty().lowercase()
        val safePrompt = promptTitle.ifBlank { "Desbloquear RosaFiesta" }.replace("\"", "'").replace("'", " ")
        val command = buildNativeAuthCommand(osName, safePrompt)
        if (command == null) {
            onResult(false, "DEVICE_AUTH_UNAVAILABLE")
            return
        }
        ownerAuthExecutor.execute {
            val outcome = runCatching {
                val process = ProcessBuilder(command).redirectErrorStream(true).start()
                val completed = process.waitFor(45, TimeUnit.SECONDS)
                if (!completed) {
                    process.destroyForcibly()
                    false to "DEVICE_AUTH_TIMEOUT"
                } else {
                    val outputText = process.inputStream.readBytes().toString(StandardCharsets.UTF_8).trim()
                    evaluateNativeAuthOutcome(osName, process.exitValue(), outputText)
                }
            }.getOrElse { false to "DEVICE_AUTH_UNAVAILABLE" }
            onResult(outcome.first, outcome.second)
        }
    }

    private fun buildNativeAuthCommand(osName: String, safePrompt: String): List<String>? = when {
        osName.contains("mac") -> listOf(
            "osascript",
            "-e",
            """do shell script "true" with administrator privileges with prompt "$safePrompt"""",
        )

        osName.contains("linux") -> listOf("pkexec", "true")

        osName.contains("win") -> buildWindowsHelloCommand(safePrompt)

        else -> null
    }

    private fun buildWindowsHelloCommand(safePrompt: String): List<String> {
        val script =
            """
            Add-Type -AssemblyName System.Runtime.WindowsRuntime
            ${'$'}op = [Windows.Security.Credentials.UI.UserConsentVerifier,Windows.Security.Credentials.UI,ContentType=WindowsRuntime]::RequestVerificationAsync('$safePrompt')
            ${'$'}asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { ${'$'}_.Name -eq 'AsTask' -and ${'$'}_.GetParameters().Count -eq 1 -and ${'$'}_.IsGenericMethod })[0].MakeGenericMethod([Windows.Security.Credentials.UI.UserConsentVerificationResult])
            ${'$'}task = ${'$'}asTask.Invoke(${'$'}null, @(${'$'}op))
            ${'$'}task.Wait()
            Write-Output ${'$'}task.Result.ToString()
            """.trimIndent()
        return listOf("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script)
    }

    private fun evaluateNativeAuthOutcome(osName: String, exitCode: Int, outputText: String): Pair<Boolean, String?> {
        if (!osName.contains("win")) {
            return if (exitCode == 0) true to null else false to "DEVICE_AUTH_CANCELLED"
        }
        if (exitCode != 0) {
            return false to "DEVICE_AUTH_UNAVAILABLE"
        }
        return when {
            outputText.equals("Verified", ignoreCase = true) -> true to null
            outputText.equals("Canceled", ignoreCase = true) -> false to "DEVICE_AUTH_CANCELLED"
            else -> false to "DEVICE_AUTH_UNAVAILABLE"
        }
    }

    override suspend fun launchOAuthBrowserFlow(
        resolveAuthorizeUrl: suspend (redirectUri: String) -> String?,
    ): OAuthBrowserOutcome {
        if (isRunningInAutomatedTest()) {
            return OAuthBrowserOutcome(errorCode = "OAUTH_BROWSER_UNAVAILABLE")
        }
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<OAuthBrowserOutcome>()
            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            val callbackExecutor = Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "rf-oauth-loopback").apply { isDaemon = true }
            }
            server.executor = callbackExecutor
            val redirectUri = "http://127.0.0.1:${server.address.port}/callback"
            server.createContext("/callback") { exchange ->
                val params = extractLoopbackCallbackParameters(exchange)
                val htmlResponse = "<html><body style='font-family:sans-serif;padding:32px;background:#FFF7F9;color:#470E1A'>" +
                    "<h2>Autenticación RosaFiesta completada</h2>" +
                    "<p>Puedes cerrar esta pestaña y volver a la aplicación.</p></body></html>"
                val bodyBytes = htmlResponse.toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                exchange.sendResponseHeaders(200, bodyBytes.size.toLong())
                exchange.responseBody.use { stream -> stream.write(bodyBytes) }
                deferred.complete(
                    OAuthBrowserOutcome(
                        code = params["code"],
                        state = params["state"],
                        redirectUri = redirectUri,
                        errorCode = params["error"],
                    ),
                )
            }
            server.start()
            try {
                val authorizeUrl = resolveAuthorizeUrl(redirectUri)
                if (authorizeUrl == null) {
                    return@withContext OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_START_FAILED")
                }
                val opened = openExternalUrl(authorizeUrl)
                if (!opened) {
                    return@withContext OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_BROWSER_UNAVAILABLE")
                }
                withTimeoutOrNull(180_000L) { deferred.await() }
                    ?: OAuthBrowserOutcome(redirectUri = redirectUri, errorCode = "OAUTH_TIMEOUT")
            } finally {
                server.stop(0)
                callbackExecutor.shutdownNow()
            }
        }
    }

    override fun registerPushNotifications(
        appVariant: String,
        onResult: (platform: String, token: String?) -> Unit,
    ) {
        onResult("desktop", null)
    }

    override fun showLocalNotification(title: String, body: String) {
        if (isHeadlessOrAutomatedEnvironment()) return
        val cleanTitle = title.trim()
        val cleanBody = body.trim()
        if (cleanTitle.isEmpty()) return
        ownerAuthExecutor.execute {
            val osName = System.getProperty("os.name").orEmpty().lowercase()
            if (osName.contains("mac")) {
                val escapedBody = cleanBody.replace("\"", "'")
                val escapedTitle = cleanTitle.replace("\"", "'")
                val process = ProcessBuilder(
                    "osascript",
                    "-e",
                    """display notification "$escapedBody" with title "$escapedTitle"""",
                ).start()
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                }
                return@execute
            }
            if (SystemTray.isSupported()) {
                runCatching {
                    val tray = SystemTray.getSystemTray()
                    val iconImage: Image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                    val existing = tray.trayIcons.firstOrNull()
                    if (existing != null) {
                        existing.displayMessage(cleanTitle, cleanBody, TrayIcon.MessageType.INFO)
                    } else {
                        val icon = TrayIcon(iconImage, "RosaFiesta").apply { isImageAutoSize = true }
                        tray.add(icon)
                        icon.displayMessage(cleanTitle, cleanBody, TrayIcon.MessageType.INFO)
                    }
                }
                return@execute
            }
            if (osName.contains("linux")) {
                val process = ProcessBuilder("notify-send", cleanTitle, cleanBody).start()
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                }
            }
        }
    }

    private fun extractLoopbackCallbackParameters(exchange: com.sun.net.httpserver.HttpExchange): Map<String, String> {
        if (exchange.requestMethod.equals("POST", ignoreCase = true)) {
            val rawBody = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
            val formParams = parseQueryParameters(rawBody)
            if (formParams.isNotEmpty()) {
                return formParams
            }
        }
        return parseQueryParameters(exchange.requestURI.rawQuery.orEmpty())
    }

    private fun parseQueryParameters(rawQuery: String): Map<String, String> {
        if (rawQuery.isBlank()) return emptyMap()
        val result = linkedMapOf<String, String>()
        for (pair in rawQuery.split("&")) {
            val idx = pair.indexOf('=')
            if (idx <= 0) continue
            val key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8)
            val value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
            result[key] = value
        }
        return result
    }

    private fun isHeadlessOrAutomatedEnvironment(): Boolean {
        if (GraphicsEnvironment.isHeadless()) return true
        return isRunningInAutomatedTest()
    }

    private fun resolveDownloadsDirectory(): File {
        val homePath = System.getProperty("user.home").orEmpty().ifEmpty { "." }
        val downloads = File(homePath, "Downloads")
        if (!downloads.exists()) {
            runCatching { downloads.mkdirs() }
        }
        return if (downloads.exists()) downloads else File(homePath)
    }

    private fun writeBytesToDirectory(directory: File, safeFileName: String, bytes: ByteArray): String? = runCatching {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val target = File(directory, safeFileName)
        target.writeBytes(bytes)
        target.absolutePath
    }.getOrNull()

    private fun revealOrOpenFileOnDesktop(file: File) {
        if (isHeadlessOrAutomatedEnvironment()) return
        if (!Desktop.isDesktopSupported()) return
        runCatching {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file)
            }
        }
    }

    private fun launchSystemUrlOpener(url: String): Boolean {
        if (isRunningInAutomatedTest()) return false
        val osName = System.getProperty("os.name").orEmpty().lowercase()
        val command = if (osName.contains("mac")) {
            listOf("open", url)
        } else if (osName.contains("win")) {
            listOf("rundll32", "url.dll,FileProtocolHandler", url)
        } else {
            listOf("xdg-open", url)
        }
        return runCatching {
            ProcessBuilder(command).start()
            true
        }.getOrDefault(false)
    }
}

public actual fun createPlatformSoundPlayer(): SoundPlayer = DesktopSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage {
    if (isRunningInAutomatedTest()) {
        val tempDir = Files.createTempDirectory("rosafiesta-test-secure").toFile().apply { deleteOnExit() }
        return DesktopSecureStorage(directory = tempDir)
    }
    val secureDir = File(resolveDesktopAppDataDirectory(), "secure")
    return DesktopSecureStorage(directory = secureDir)
}

public actual fun createPlatformVoiceInputMonitor(): VoiceInputMonitor {
    if (isRunningInAutomatedTest()) {
        return NoOpVoiceInputMonitor()
    }
    return DesktopVoiceInputMonitor()
}

public actual fun createPlatformActions(): PlatformActions = DesktopPlatformActions()
