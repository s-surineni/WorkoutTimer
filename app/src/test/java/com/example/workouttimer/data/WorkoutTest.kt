package com.example.workouttimer.data

import junit.framework.TestCase.assertEquals
import org.junit.Test

class WorkoutTest {

  @Test
  fun totalDurationSeconds_singleRound_computesCorrectly() {
    val workout = Workout(
      title = "Quick Tabata",
      rounds = 1,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Plank", workSeconds = 20, restSeconds = 10)
      )
    )
    // (20 + 10 + 20 + 10) * 1 + 0 = 60s
    assertEquals(60, workout.totalDurationSeconds)
    assertEquals("1:00", workout.formattedTotalDuration())
  }

  @Test
  fun totalDurationSeconds_multipleRounds_includesInterRoundRest() {
    val workout = Workout(
      title = "2 Round Tabata",
      rounds = 2,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Squats", workSeconds = 20, restSeconds = 10)
      )
    )
    // (30 * 2) + 30 = 90s
    assertEquals(90, workout.totalDurationSeconds)
    assertEquals("1:30", workout.formattedTotalDuration())
  }

  @Test
  fun formattedTotalDuration_hoursFormatting() {
    val workout = Workout(
      title = "Marathon Workout",
      rounds = 100,
      restBetweenRoundsSeconds = 60,
      exercises = listOf(
        Exercise(name = "Endurance Run", workSeconds = 40, restSeconds = 20)
      )
    )
    // 100 * 60 + 99 * 60 = 6000 + 5940 = 11940s = 3 hours, 19 mins, 0 secs
    assertEquals(11940, workout.totalDurationSeconds)
    assertEquals("3:19:00", workout.formattedTotalDuration())
  }
}

