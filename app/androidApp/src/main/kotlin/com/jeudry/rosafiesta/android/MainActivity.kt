package com.jeudry.rosafiesta.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.jeudry.rosafiesta.PersonalApp
import com.jeudry.rosafiesta.data.initAndroidDriverContext
import com.jeudry.rosafiesta.data.readAndroidPickedUri
import com.jeudry.rosafiesta.data.registerAndroidMediaPickerBridge
import com.jeudry.rosafiesta.domain.platform.ExternalUrlBuilder
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android entrypoint for the Personal RosaFiesta client application.
 */
class MainActivity : ComponentActivity() {
    private var pendingMediaContinuation: Continuation<PickedMediaFile?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAndroidDriverContext(applicationContext, databaseName = "rosafiesta-personal.db")
        val documentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { selectedUri ->
            val continuation = pendingMediaContinuation
            pendingMediaContinuation = null
            if (continuation != null) {
                val picked = if (selectedUri != null) {
                    readAndroidPickedUri(applicationContext, selectedUri)
                } else {
                    null
                }
                continuation.resume(picked)
            }
        }
        registerAndroidMediaPickerBridge { allowedExtensions ->
            val mimeTypes = mapAllowedExtensionsToMimeTypes(allowedExtensions)
            suspendCoroutine { continuation ->
                pendingMediaContinuation = continuation
                documentLauncher.launch(mimeTypes)
            }
        }
        setContent {
            PersonalApp(initialScreenCode = "p00")
        }
    }

    override fun onDestroy() {
        registerAndroidMediaPickerBridge(null)
        super.onDestroy()
    }

    private fun mapAllowedExtensionsToMimeTypes(extensions: List<String>): Array<String> {
        val mapped = extensions
            .map { ext -> ExternalUrlBuilder.inferMimeType("sample.${ext.trim().lowercase()}", "") }
            .filter { mime -> mime.isNotEmpty() }
            .distinct()
        return if (mapped.isEmpty()) arrayOf("*/*") else mapped.toTypedArray()
    }
}
