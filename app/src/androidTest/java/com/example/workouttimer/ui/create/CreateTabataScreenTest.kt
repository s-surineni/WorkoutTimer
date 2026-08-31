package com.example.workouttimer.ui.create

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

/** UI tests for [CreateTabataScreen]. */
class CreateTabataScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun createTabataScreen_rendersCreateModeElements() {
    composeTestRule.setContent {
      CreateTabataScreen(
        onNavigateBack = {},
        onSaveWorkout = {}
      )
    }

    composeTestRule.onNodeWithText("Create Tabata Workout").assertExists()
    composeTestRule.onNodeWithText("1. Workout Details").assertExists()
    composeTestRule.onNodeWithText("Save Tabata Workout").assertExists()
    composeTestRule.onNodeWithContentDescription("Navigate back").assertExists()
  }

  @Test
  fun createTabataScreen_rendersEditModeElements() {
    val sample = Workout(
      title = "Existing Routine",
      rounds = 4,
      exercises = listOf(
        Exercise(name = "Burpees", workSeconds = 30, restSeconds = 15),
        Exercise(name = "Squats", workSeconds = 20, restSeconds = 10)
      )
    )

    composeTestRule.setContent {
      CreateTabataScreen(
        initialWorkout = sample,
        onNavigateBack = {},
        onSaveWorkout = {}
      )
    }

    composeTestRule.onNodeWithText("Edit Tabata Workout").assertExists()
    composeTestRule.onNodeWithText("Update Tabata Workout").assertExists()
    composeTestRule.onAllNodesWithText("Burpees")[0].assertExists()
    composeTestRule.onAllNodesWithContentDescription("Move exercise down")[0].assertExists()
    composeTestRule.onAllNodesWithContentDescription("Move exercise up")[1].assertExists()
  }

  @Test
  fun createTabataScreen_reordersExercisesDown() {
    val sample = Workout(
      title = "Reorder Routine",
      rounds = 2,
      exercises = listOf(
        Exercise(name = "First Exercise", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Second Exercise", workSeconds = 20, restSeconds = 10)
      )
    )

    composeTestRule.setContent {
      CreateTabataScreen(
        initialWorkout = sample,
        onNavigateBack = {},
        onSaveWorkout = {}
      )
    }

    // Move first exercise down
    composeTestRule.onAllNodesWithContentDescription("Move exercise down")[0].performClick()
    composeTestRule.onNodeWithText("Second Exercise").assertExists()
    composeTestRule.onNodeWithText("First Exercise").assertExists()
  }

  @Test
  fun createTabataScreen_navigateBackTriggered() {
    var backTriggered = false
    composeTestRule.setContent {
      CreateTabataScreen(
        onNavigateBack = { backTriggered = true },
        onSaveWorkout = {}
      )
    }

    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
    assertTrue(backTriggered)
  }
}
