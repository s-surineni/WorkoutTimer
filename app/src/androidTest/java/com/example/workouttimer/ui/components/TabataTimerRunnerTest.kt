package com.example.workouttimer.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleWorkout = Workout(
        title = "HIIT Sprint",
        rounds = 2,
        restBetweenRoundsSeconds = 30,
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
        composeTestRule.onNodeWithText("High Knees").assertExists()
        composeTestRule.onNodeWithContentDescription("Mute Sound").assertExists()
        composeTestRule.onNodeWithContentDescription("Close Timer").assertExists()
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

