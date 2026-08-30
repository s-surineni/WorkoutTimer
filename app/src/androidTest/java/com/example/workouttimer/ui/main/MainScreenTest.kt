package com.example.workouttimer.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.workouttimer.data.Workout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.workouttimer.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      MainScreenContent(
        workouts = FAKE_DATA,
        onAddClick = {},
        onDeleteWorkout = {}
      )
    }
  }

  @Test
  fun workoutItems_exist() {
    FAKE_DATA.forEach {
      composeTestRule.onNodeWithText(it.name).assertExists()
    }
  }

  @Test
  fun addWorkoutButton_exists() {
    composeTestRule.onNodeWithContentDescription("Add Workout").assertExists()
  }
}

private val FAKE_DATA = listOf(
  Workout(id = "1", name = "Push Ups", workoutSeconds = 30, cooldownSeconds = 10),
  Workout(id = "2", name = "Squats", workoutSeconds = 45, cooldownSeconds = 15)
)
