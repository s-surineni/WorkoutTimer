package com.example.workouttimer.ui.main

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.workouttimer.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    composeTestRule.setContent {
      MainScreenContent(
        workouts = FAKE_DATA,
        onAddClick = {},
        onEditClick = {},
        onStartWorkout = {},
        onDeleteWorkout = {},
        onShareWorkout = {},
        onExportAllWorkouts = {},
        onImportWorkouts = {}
      )
    }
  }

  @Test
  fun workoutRoutines_exist() {
    FAKE_DATA.forEach {
      composeTestRule.onNodeWithText(it.title).assertExists()
    }
  }

  @Test
  fun addTabataButton_exists() {
    composeTestRule.onNodeWithContentDescription("Add Tabata Workout").assertExists()
  }

  @Test
  fun editRoutineButtons_exist() {
    composeTestRule.onAllNodesWithContentDescription("Edit Routine")[0].assertExists()
  }

  @Test
  fun shareRoutineButtons_exist() {
    composeTestRule.onAllNodesWithContentDescription("Share Routine")[0].assertExists()
  }

  @Test
  fun exportAllRoutinesButton_exists() {
    composeTestRule.onNodeWithContentDescription("Export All Routines").assertExists()
  }

  @Test
  fun importRoutineButton_opensImportDialogWithTabs() {
    composeTestRule.onNodeWithContentDescription("Import Routine").performClick()
    composeTestRule.onNodeWithText("Import Workouts").assertExists()
    composeTestRule.onNodeWithText("File / Paste").assertExists()
    composeTestRule.onNodeWithText("Preset Library").assertExists()

    // Test switching to Preset Library tab
    composeTestRule.onNodeWithText("Preset Library").performClick()
    composeTestRule.onNodeWithText("Full Body HIIT Ignition").assertExists()
    composeTestRule.onNodeWithText("Cancel").assertExists()
  }

  @Test
  fun deleteRoutine_showsConfirmationDialog() {
    composeTestRule.onAllNodesWithContentDescription("Delete Routine")[0].performClick()
    composeTestRule.onNodeWithText("Delete Workout Routine?").assertExists()
    composeTestRule.onNodeWithText("Are you sure you want to delete \"Push & Core Tabata\"? This action cannot be undone.").assertExists()
    composeTestRule.onNodeWithText("Cancel").assertExists()
    composeTestRule.onNodeWithText("Delete").assertExists()
  }
}

private val FAKE_DATA = listOf(
  Workout(
    id = "1",
    title = "Push & Core Tabata",
    rounds = 2,
    warmupSeconds = 30,
    cooldownSeconds = 30,
    exercises = listOf(
      Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Plank", workSeconds = 20, restSeconds = 10)
    )
  ),
  Workout(
    id = "2",
    title = "Leg Burner",
    rounds = 3,
    exercises = listOf(
      Exercise(name = "Squats", workSeconds = 30, restSeconds = 15)
    )
  )
)
