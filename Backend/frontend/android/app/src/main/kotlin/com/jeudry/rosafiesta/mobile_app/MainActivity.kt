package com.jeudry.rosafiesta.mobile_app

import android.content.Intent
import android.net.Uri
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.rosafiesta/deeplink"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getInitialRoute" -> {
                    result.success(getHashPathFromIntent(intent))
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Pass the new intent's deep link to Flutter
        val hashPath = getHashPathFromIntent(intent)
        if (hashPath != null) {
            MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL)
                .invokeMethod("onDeepLink", hashPath)
        }
    }

    private fun getHashPathFromIntent(intent: Intent?): String? {
        if (intent == null) return null

        val data: Uri? = intent.data
        if (data == null) return null

        // Custom scheme: rosafiesta://path -> /#/path
        // Universal link: https://rosafiesta.com/path -> /#/path
        val path = data.path ?: return null
        return if (path.isNotEmpty()) "/#$path" else null
    }
}
