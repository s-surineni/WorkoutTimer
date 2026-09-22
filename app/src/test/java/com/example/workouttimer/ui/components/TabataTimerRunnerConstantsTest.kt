package com.example.workouttimer.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [TabataTimerRunnerConstants].
 */
class TabataTimerRunnerConstantsTest {

    @Test
    fun testPhaseTotalDurationLabel() {
        assertEquals("/ 20s", TabataTimerRunnerConstants.phaseTotalDurationLabel(20))
        assertEquals("/ 45s", TabataTimerRunnerConstants.phaseTotalDurationLabel(45))
    }

    @Test
    fun testIntervalSegmentDescription() {
        val completedDesc = TabataTimerRunnerConstants.intervalSegmentDescription(
            index = 0,
            name = "Push Ups",
            isCompleted = true,
            isActive = false
        )
        assertEquals("Exercise 1: Push Ups (Completed)", completedDesc)

        val activeDesc = TabataTimerRunnerConstants.intervalSegmentDescription(
            index = 1,
            name = "Squats",
            isCompleted = false,
            isActive = true
        )
        assertEquals("Exercise 2: Squats (Current Interval)", activeDesc)

        val upcomingDesc = TabataTimerRunnerConstants.intervalSegmentDescription(
            index = 2,
            name = "Plank",
            isCompleted = false,
            isActive = false
        )
        assertEquals("Exercise 3: Plank (Upcoming)", upcomingDesc)
    }

    @Test
    fun testUpNextFormatters() {
        assertEquals("Up Next: Round 2 • Burpees", TabataTimerRunnerConstants.upNextRound(2, "Burpees"))
        assertEquals("Up Next: Round Rest (30s)", TabataTimerRunnerConstants.upNextRoundRest(30))
        assertEquals("Up Next: Cool-Down (60s)", TabataTimerRunnerConstants.upNextCoolDown(60))
        assertEquals("Up Next: Rest (10s)", TabataTimerRunnerConstants.upNextRest(10))
        assertEquals("Up Next: Jumping Jacks (20s)", TabataTimerRunnerConstants.upNextExercise("Jumping Jacks", 20))
    }

    @Test
    fun testSubtitles() {
        assertEquals("Round 1 of 4 • Getting Ready", TabataTimerRunnerConstants.subtitleGettingReady(4))
        assertEquals(
            "Round 2 of 4 • Exercise 3 of 5",
            TabataTimerRunnerConstants.subtitleRoundProgress(2, 4, 2, 5)
        )
    }

    @Test
    fun testConstantsValues() {
        assertEquals("pulseScale", TabataTimerRunnerConstants.ANIM_LABEL_PULSE_SCALE)
        assertEquals("Interval sequence timeline", TabataTimerRunnerConstants.CD_INTERVAL_TIMELINE)
        assertEquals("GET READY", TabataTimerRunnerConstants.LABEL_GET_READY)
        assertEquals("WORK", TabataTimerRunnerConstants.LABEL_WORK)
        assertEquals("REST", TabataTimerRunnerConstants.LABEL_REST)
        assertEquals("Rest & Recover", TabataTimerRunnerConstants.TITLE_REST_RECOVER)
        assertEquals("COMING UP NEXT", TabataTimerRunnerConstants.LABEL_COMING_UP_NEXT)
    }
}

