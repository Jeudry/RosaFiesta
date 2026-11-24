package com.jeudry.rosafiesta.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.jeudry.rosafiesta.PersonalApp
import com.jeudry.rosafiesta.data.dispatchAndroidIncomingUri
import com.jeudry.rosafiesta.data.initAndroidDriverContext
import com.jeudry.rosafiesta.data.readAndroidPickedUri
import com.jeudry.rosafiesta.data.registerAndroidAudioPermissionRequester
import com.jeudry.rosafiesta.data.registerAndroidDeviceAuthBridge
import com.jeudry.rosafiesta.data.registerAndroidMediaPickerBridge
import com.jeudry.rosafiesta.domain.platform.AppLifecycleBridge
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
    private var pendingCredentialCallback: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAndroidDriverContext(applicationContext, databaseName = "rosafiesta-personal.db")
        dispatchAndroidIncomingUri(intent?.dataString ?: intent?.getStringExtra("url"))
        requestNotificationsPermissionIfNeeded()
        val credentialLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = pendingCredentialCallback
            pendingCredentialCallback = null
            callback?.invoke(result.resultCode == Activity.RESULT_OK)
        }
        registerAndroidDeviceAuthBridge(
            activity = this,
            credentialLauncher = { intent, callback ->
                pendingCredentialCallback = callback
                credentialLauncher.launch(intent)
            },
        )
        registerAndroidAudioPermissionRequester {
            val isGranted = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 4003)
            }
        }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchAndroidIncomingUri(intent.dataString ?: intent.getStringExtra("url"))
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val isGranted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4001)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            AppLifecycleBridge.dispatchAppBackgrounded()
        }
    }

    override fun onDestroy() {
        registerAndroidAudioPermissionRequester(null)
        registerAndroidMediaPickerBridge(null)
        registerAndroidDeviceAuthBridge(null, null)
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
