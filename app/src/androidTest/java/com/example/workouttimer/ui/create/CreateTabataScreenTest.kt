package com.example.workouttimer.ui.create

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

/** UI tests for [CreateTabataScreen]. */
class CreateTabataScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun createTabataScreen_rendersElements() {
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

