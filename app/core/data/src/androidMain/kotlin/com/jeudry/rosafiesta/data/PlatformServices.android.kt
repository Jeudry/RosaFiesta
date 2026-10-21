package com.jeudry.rosafiesta.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.PcmSoundSynthesizer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class AndroidSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true
    override var soundVolume: Float = 0.75f

    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "rf-android-sfx").apply { isDaemon = true }
    }

    override fun play(effect: SoundEffect) {
        if (!isEnabled || effect == SoundEffect.NONE || soundVolume <= 0.01f) return
        val wavBytes = PcmSoundSynthesizer.synthesizeWav(effect, soundVolume)
        if (wavBytes.size <= 44) return
        val pcmBytes = wavBytes.copyOfRange(44, wavBytes.size)
        executor.execute {
            runCatching {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(22_050)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(pcmBytes.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(pcmBytes, 0, pcmBytes.size)
                track.play()
            }
        }
    }
}

internal class AndroidKeystoreSecureStorage(context: Context) : SecureStorage {
    private val preferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    override suspend fun get(key: String): String? {
        val stored = preferences.getString(key.trim(), null) ?: return null
        return runCatching {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
            String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
        }.getOrNull()
    }

    override suspend fun put(key: String, value: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) return
        runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.iv + cipher.doFinal(value.trim().toByteArray(Charsets.UTF_8))
            preferences.edit().putString(cleanKey, Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
        }
    }

    override suspend fun remove(key: String) {
        preferences.edit().remove(key.trim()).apply()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .build(),
        )
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

public actual fun createPlatformSoundPlayer(): SoundPlayer = AndroidSoundPlayer()

public actual fun createPlatformSecureStorage(): SecureStorage {
    val context = currentAndroidContext() ?: return InMemorySecureStorage()
    return AndroidKeystoreSecureStorage(context)
}
