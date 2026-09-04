package com.example.workouttimer.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.workouttimer.audio.NoOpAudioFeedbackManager
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

/** UI tests for [TabataTimerRunner]. */
class TabataTimerRunnerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleWorkout = Workout(
        title = "HIIT Sprint",
        rounds = 2,
        restBetweenRoundsSeconds = 30,
        warmupSeconds = 30,
        cooldownSeconds = 30,
        exercises = listOf(
            Exercise(name = "High Knees", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10)
        )
    )

    @Test
    fun tabataTimerRunner_rendersInitialStateAndControls() {
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        composeTestRule.onNodeWithText("HIIT Sprint").assertExists()
        composeTestRule.onNodeWithText("GET READY").assertExists()
        composeTestRule.onNodeWithText("Up Next: Warm-Up (30s)").assertExists()
        composeTestRule.onNodeWithContentDescription("Lock Screen").assertExists()
        composeTestRule.onNodeWithContentDescription("Mute Sound").assertExists()
        composeTestRule.onNodeWithContentDescription("Close Timer").assertExists()
    }

    @Test
    fun tabataTimerRunner_transitionsToWarmupPhase() {
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        // Skip GET READY -> Moves to WARM-UP
        composeTestRule.onNodeWithContentDescription("Skip Exercise").performClick()

        composeTestRule.onNodeWithText("WARM-UP").assertExists()
        composeTestRule.onNodeWithText("Warm-Up & Mobilize").assertExists()
        composeTestRule.onNodeWithText("Up Next: Round 1 • High Knees").assertExists()
    }

    @Test
    fun tabataTimerRunner_soundToggleSwitchesState() {
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        val soundButton = composeTestRule.onNodeWithContentDescription("Mute Sound")
        soundButton.assertExists()
        soundButton.performClick()

        composeTestRule.onNodeWithContentDescription("Unmute Sound").assertExists()
    }

    @Test
    fun tabataTimerRunner_screenLock_disablesControlsAndUnlocksOnButton() {
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        // Lock screen
        composeTestRule.onNodeWithContentDescription("Lock Screen").performClick()
        composeTestRule.onNodeWithContentDescription("Unlock Screen").assertExists()
        composeTestRule.onNodeWithText("Screen Locked").assertExists()
        composeTestRule.onNodeWithText("Unlock").assertExists()

        // Unlock screen
        composeTestRule.onNodeWithText("Unlock").performClick()
        composeTestRule.onNodeWithContentDescription("Lock Screen").assertExists()
    }

    @Test
    fun tabataTimerRunner_dismissTriggeredOnClose() {
        var dismissed = false
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = { dismissed = true },
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        composeTestRule.onNodeWithContentDescription("Close Timer").performClick()
        assertTrue(dismissed)
    }
}
