package com.jeudry.rosafiesta.domain.sound

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Multiplatform microphone level and speech capture monitor for RosaFiesta voice search and Rosa IA voice mode (`s35`).
 */
public interface VoiceInputMonitor {
    /** Whether microphone capture is currently active. */
    public val isListening: StateFlow<Boolean>

    /** Normalized RMS audio amplitude between `0.0f` and `1.0f` captured from the hardware microphone. */
    public val amplitudeLevel: StateFlow<Float>

    /** Whether a physical audio capture line was successfully opened on the current device. */
    public val hasHardwareMicrophone: StateFlow<Boolean>

    /** Starts listening to the platform microphone. */
    public fun startListening()

    /** Stops listening and releases the platform audio line. */
    public fun stopListening()
}

/**
 * Deterministic no-op [VoiceInputMonitor] used in unit tests and platforms without audio capture APIs.
 */
public class NoOpVoiceInputMonitor : VoiceInputMonitor {
    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _amplitudeLevel = MutableStateFlow(0f)
    override val amplitudeLevel: StateFlow<Float> = _amplitudeLevel.asStateFlow()

    private val _hasHardwareMicrophone = MutableStateFlow(false)
    override val hasHardwareMicrophone: StateFlow<Boolean> = _hasHardwareMicrophone.asStateFlow()

    override fun startListening() {
        _isListening.value = true
        _amplitudeLevel.value = 0.35f
    }

    override fun stopListening() {
        _isListening.value = false
        _amplitudeLevel.value = 0f
    }
}
