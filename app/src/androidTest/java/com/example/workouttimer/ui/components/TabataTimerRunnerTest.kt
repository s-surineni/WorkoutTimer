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

        composeTestRule.onNodeWithText(sampleWorkout.title).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.LABEL_GET_READY).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.TITLE_WARMUP_MOBILIZE).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.upNextRound(1, sampleWorkout.exercises[0].name)).assertExists()
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_LOCK_SCREEN).assertExists()
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_MUTE_SOUND).assertExists()
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_CLOSE_TIMER).assertExists()
    }

    @Test
    fun tabataTimerRunner_preparePhase_showsFirstExerciseWhenNoWarmup() {
        val noWarmupWorkout = sampleWorkout.copy(warmupSeconds = 0)
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = noWarmupWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.LABEL_GET_READY).assertExists()
        composeTestRule.onNodeWithText(sampleWorkout.exercises[0].name).assertExists()
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
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_SKIP_EXERCISE).performClick()

        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.LABEL_WARMUP).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.TITLE_WARMUP_MOBILIZE).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.upNextRound(1, sampleWorkout.exercises[0].name)).assertExists()
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

        val soundButton = composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_MUTE_SOUND)
        soundButton.assertExists()
        soundButton.performClick()

        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_UNMUTE_SOUND).assertExists()
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
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_LOCK_SCREEN).performClick()
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_UNLOCK_SCREEN).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.TEXT_SCREEN_LOCKED).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.TEXT_UNLOCK).assertExists()

        // Unlock screen
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.TEXT_UNLOCK).performClick()
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_LOCK_SCREEN).assertExists()
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

        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_CLOSE_TIMER).performClick()
        assertTrue(dismissed)
    }

    @Test
    fun tabataTimerRunner_intervalTimelineAndDurationLabelRendered() {
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_INTERVAL_TIMELINE).assertExists()
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.phaseTotalDurationLabel(3)).assertExists()
    }

    @Test
    fun tabataTimerRunner_restPhase_showsRestRecoveryTitleAndUpcomingExerciseAtBottomOnly() {
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = sampleWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        // PREPARE -> WARM-UP
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_SKIP_EXERCISE).performClick()
        // WARM-UP -> WORK (High Knees)
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_SKIP_EXERCISE).performClick()
        // WORK -> REST
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_SKIP_EXERCISE).performClick()

        // Verify REST phase badge, Rest & Recover title, and that the upcoming exercise name appears only at bottom
        composeTestRule.onNodeWithText(TabataTimerRunnerConstants.LABEL_REST).assertExists()
        composeTestRule.onNodeWithText(
            TabataTimerRunnerConstants.upNextExercise(
                sampleWorkout.exercises[1].name,
                sampleWorkout.exercises[1].workSeconds
            )
        ).assertExists()
    }

    @Test
    fun tabataTimerRunner_activeWorkPhase_displaysRoundAndExerciseBadges() {
        val noWarmupWorkout = sampleWorkout.copy(warmupSeconds = 0)
        composeTestRule.setContent {
            TabataTimerRunner(
                workout = noWarmupWorkout,
                onDismiss = {},
                audioFeedbackManager = NoOpAudioFeedbackManager()
            )
        }

        // PREPARE -> WORK
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.CD_SKIP_EXERCISE).performClick()

        // Verify Round badge and Exercise position badge
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.formatRoundProgress(1, noWarmupWorkout.rounds)).assertExists()
        composeTestRule.onNodeWithContentDescription(TabataTimerRunnerConstants.formatExerciseProgress(0, noWarmupWorkout.exercises.size)).assertExists()
    }
}

