package com.example.workouttimer.audio

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Manages audio feedback and sound cues during workouts.
 */
interface AudioFeedbackManager {
    fun playCountdownTick()
    fun playWorkStart()
    fun playRestStart()
    fun playWorkoutComplete()
    fun release()
}

/**
 * Native Android [ToneGenerator] implementation of [AudioFeedbackManager].
 * Produces low-latency audio tones without needing bundled media assets.
 */
class ToneAudioFeedbackManager : AudioFeedbackManager {
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)
    } catch (_: Throwable) {
        null
    }

    override fun playCountdownTick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        } catch (_: Throwable) {}
    }

    override fun playWorkStart() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
        } catch (_: Throwable) {}
    }

    override fun playRestStart() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (_: Throwable) {}
    }

    override fun playWorkoutComplete() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500)
        } catch (_: Throwable) {}
    }

    override fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Throwable) {}
    }
}

/**
 * No-op implementation for Compose previews and unit tests.
 */
class NoOpAudioFeedbackManager : AudioFeedbackManager {
    override fun playCountdownTick() {}
    override fun playWorkStart() {}
    override fun playRestStart() {}
    override fun playWorkoutComplete() {}
    override fun release() {}
}

