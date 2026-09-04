package com.example.workouttimer.data

import junit.framework.TestCase.assertEquals
import org.junit.Test

class WorkoutTest {

  @Test
  fun totalDurationSeconds_singleRound_computesCorrectlyWithoutLastExerciseRest() {
    val workout = Workout(
      title = "Quick Tabata",
      rounds = 1,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Plank", workSeconds = 20, restSeconds = 10)
      )
    )
    // Ex 1 (20s work + 10s rest) + Ex 2 (20s work + 0s rest) = 50s
    assertEquals(50, workout.totalDurationSeconds)
    assertEquals("0:50", workout.formattedTotalDuration())
  }

  @Test
  fun totalDurationSeconds_withWarmupAndCooldown_computesCorrectly() {
    val workout = Workout(
      title = "Complete Tabata",
      rounds = 1,
      warmupSeconds = 30,
      cooldownSeconds = 30,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Plank", workSeconds = 20, restSeconds = 10)
      )
    )
    // 30s warmup + 50s exercises + 30s cooldown = 110s
    assertEquals(110, workout.totalDurationSeconds)
    assertEquals("1:50", workout.formattedTotalDuration())
  }

  @Test
  fun totalDurationSeconds_multipleRounds_includesInterRoundRestWithoutLastExerciseRest() {
    val workout = Workout(
      title = "2 Round Tabata",
      rounds = 2,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Squats", workSeconds = 20, restSeconds = 10)
      )
    )
    // Round 1 (20s work + 30s inter-round rest) + Round 2 (20s work) = 70s
    assertEquals(70, workout.totalDurationSeconds)
    assertEquals("1:10", workout.formattedTotalDuration())
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
    // 100 * 40 + 99 * 60 = 4000 + 5940 = 9940s = 2 hours, 45 mins, 40 secs
    assertEquals(9940, workout.totalDurationSeconds)
    assertEquals("2:45:40", workout.formattedTotalDuration())
  }
}
