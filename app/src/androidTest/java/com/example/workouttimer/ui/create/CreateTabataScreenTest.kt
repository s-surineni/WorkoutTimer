package com.example.workouttimer.ui.create

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

/** UI tests for tabbed [CreateTabataScreen]. */
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
    composeTestRule.onNodeWithText("Details").assertExists()
    composeTestRule.onNodeWithText("Exercises (2)").assertExists()
    composeTestRule.onNodeWithText("1. Workout Title & Rounds").assertExists()
    composeTestRule.onNodeWithText("🔥 Warm-Up Block").performScrollTo().assertExists()
    composeTestRule.onNodeWithText("❄️ Cool-Down Block").performScrollTo().assertExists()
    composeTestRule.onNodeWithText("Save").assertExists()
    composeTestRule.onNodeWithContentDescription("Navigate back").assertExists()
  }

  @Test
  fun createTabataScreen_switchesToExercisesTab() {
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

    // Switch to Exercises tab
    composeTestRule.onNodeWithText("Exercises (2)").performClick()

    composeTestRule.onNodeWithText("Exercise Sequence (2)").assertExists()
    composeTestRule.onNodeWithText("Add Exercise").performScrollTo().assertExists()
    composeTestRule.onAllNodesWithText("Burpees")[0].performScrollTo().assertExists()
    composeTestRule.onAllNodesWithContentDescription("Move exercise down")[0].performScrollTo().assertExists()
    composeTestRule.onAllNodesWithContentDescription("Move exercise up")[1].performScrollTo().assertExists()
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

    // Switch to Exercises tab
    composeTestRule.onNodeWithText("Exercises (2)").performClick()

    // Move first exercise down
    composeTestRule.onAllNodesWithContentDescription("Move exercise down")[0].performScrollTo().performClick()
    composeTestRule.onNodeWithText("Second Exercise").performScrollTo().assertExists()
    composeTestRule.onNodeWithText("First Exercise").performScrollTo().assertExists()
  }

  @Test
  fun createTabataScreen_addExerciseDialog_addsNewExerciseToList() {
    composeTestRule.setContent {
      CreateTabataScreen(
        onNavigateBack = {},
        onSaveWorkout = {}
      )
    }

    // Switch to Exercises tab
    composeTestRule.onNodeWithText("Exercises (2)").performClick()

    // Click "+ Add Exercise"
    composeTestRule.onNodeWithText("Add Exercise").performScrollTo().performClick()

    // Dialog elements exist
    composeTestRule.onNodeWithText("Add to Routine").assertExists()
    composeTestRule.onNodeWithText("Burpees").performClick()
    composeTestRule.onNodeWithText("40s").performClick()

    // Confirm add in dialog
    composeTestRule.onNodeWithText("Add to Routine").performClick()

    // Exercise now in sequence list
    composeTestRule.onAllNodesWithText("Burpees")[0].performScrollTo().assertExists()
  }

  @Test
  fun createTabataScreen_editExerciseDialog_updatesExercise() {
    val sample = Workout(
      title = "Custom Routine",
      rounds = 2,
      exercises = listOf(
        Exercise(name = "Old Name", workSeconds = 20, restSeconds = 10)
      )
    )

    composeTestRule.setContent {
      CreateTabataScreen(
        initialWorkout = sample,
        onNavigateBack = {},
        onSaveWorkout = {}
      )
    }

    // Switch to Exercises tab
    composeTestRule.onNodeWithText("Exercises (1)").performClick()

    // Click Edit icon on first exercise
    composeTestRule.onNodeWithContentDescription("Edit exercise").performScrollTo().performClick()
    composeTestRule.onNodeWithText("Edit Exercise").assertExists()

    // Change name
    composeTestRule.onNodeWithText("Squats").performClick()
    composeTestRule.onNodeWithText("Save Changes").performClick()

    // Updated exercise is displayed
    composeTestRule.onAllNodesWithText("Squats")[0].performScrollTo().assertExists()
  }

  @Test
  fun createTabataScreen_navigateBack_autoSavesWhenFieldsAreFilled() {
    var savedWorkout: Workout? = null
    val sample = Workout(
      title = "Routine To Save",
      rounds = 3,
      warmupSeconds = 45,
      cooldownSeconds = 60,
      exercises = listOf(
        Exercise(name = "Burpees", workSeconds = 30, restSeconds = 15)
      )
    )

    composeTestRule.setContent {
      CreateTabataScreen(
        initialWorkout = sample,
        onNavigateBack = {},
        onSaveWorkout = { savedWorkout = it }
      )
    }

    // Press Back Arrow
    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

    assertNotNull(savedWorkout)
    assertEquals("Routine To Save", savedWorkout?.title)
    assertEquals(3, savedWorkout?.rounds)
    assertEquals(45, savedWorkout?.warmupSeconds)
    assertEquals(60, savedWorkout?.cooldownSeconds)
  }

  @Test
  fun createTabataScreen_navigateBack_navigatesBackWhenFieldsAreInvalid() {
    var backTriggered = false
    var savedWorkout: Workout? = null

    composeTestRule.setContent {
      CreateTabataScreen(
        onNavigateBack = { backTriggered = true },
        onSaveWorkout = { savedWorkout = it }
      )
    }

    // Empty title by default in create mode -> should not save, should navigate back
    composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

    assertTrue(backTriggered)
    assertEquals(null, savedWorkout)
  }
}
