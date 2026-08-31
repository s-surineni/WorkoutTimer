package com.example.workouttimer.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultDataRepositoryTest {

  @Test
  fun defaultRepository_initializesWithDefaultWorkouts() = runTest {
    val repository = DefaultDataRepository()
    val workouts = repository.workouts.first()
    assertTrue(workouts.isNotEmpty())
    assertEquals(3, workouts.size)
    assertEquals("Classic Tabata", workouts[0].title)
  }

  @Test
  fun defaultRepository_addWorkout_appendsWorkout() = runTest {
    val repository = DefaultDataRepository(initialWorkouts = emptyList())
    val customWorkout = Workout(
      title = "Custom HIIT",
      rounds = 1,
      exercises = listOf(Exercise(name = "Burpees", workSeconds = 30, restSeconds = 15))
    )

    repository.addWorkout(customWorkout)

    val list = repository.workouts.first()
    assertEquals(1, list.size)
    assertEquals("Custom HIIT", list.first().title)
  }

  @Test
  fun defaultRepository_removeWorkout_filtersOutMatchingId() = runTest {
    val workout = Workout(id = "w1", title = "Test", exercises = emptyList())
    val repository = DefaultDataRepository(initialWorkouts = listOf(workout))

    repository.removeWorkout("w1")

    val list = repository.workouts.first()
    assertTrue(list.isEmpty())
  }
}

