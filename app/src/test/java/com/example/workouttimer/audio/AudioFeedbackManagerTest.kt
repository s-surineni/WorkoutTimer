package com.example.workouttimer.audio

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AudioFeedbackManagerTest {

    private class RecordingAudioFeedbackManager : AudioFeedbackManager {
        val events = mutableListOf<String>()
        var isReleased = false

        override fun playCountdownTick() {
            events.add("TICK")
        }

        override fun playWorkStart() {
            events.add("WORK")
        }

        override fun playRestStart() {
            events.add("REST")
        }

        override fun playWorkoutComplete() {
            events.add("COMPLETE")
        }

        override fun release() {
            isReleased = true
            events.add("RELEASE")
        }
    }

    @Test
    fun noOpAudioFeedbackManager_callsDoNotThrow() {
        val manager = NoOpAudioFeedbackManager()
        manager.playCountdownTick()
        manager.playWorkStart()
        manager.playRestStart()
        manager.playWorkoutComplete()
        manager.release()
    }

    @Test
    fun recordingAudioFeedbackManager_tracksAllPlaybackEvents() {
        val manager = RecordingAudioFeedbackManager()
        manager.playCountdownTick()
        manager.playWorkStart()
        manager.playRestStart()
        manager.playWorkoutComplete()
        manager.release()

        assertEquals(listOf("TICK", "WORK", "REST", "COMPLETE", "RELEASE"), manager.events)
        assertTrue(manager.isReleased)
    }
}

