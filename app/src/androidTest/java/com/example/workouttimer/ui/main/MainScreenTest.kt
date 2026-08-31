package com.example.workouttimer.ui.main

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
        onDeleteWorkout = {}
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
}

private val FAKE_DATA = listOf(
  Workout(
    id = "1",
    title = "Push & Core Tabata",
    rounds = 2,
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
