package com.example.workouttimer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Interface defining the reactive data source for Workout routines.
 */
interface DataRepository {
  val workouts: Flow<List<Workout>>

  fun addWorkout(workout: Workout)

  fun updateWorkout(workout: Workout)

  fun removeWorkout(id: String)
}

/**
 * Default in-memory implementation of [DataRepository], pre-populated with default Tabata routines.
 */
class DefaultDataRepository(initialWorkouts: List<Workout> = defaultTabataWorkouts) : DataRepository {
  private val _workouts = MutableStateFlow(initialWorkouts)
  override val workouts: Flow<List<Workout>> = _workouts.asStateFlow()

  override fun addWorkout(workout: Workout) {
    _workouts.update { current -> current + workout }
  }

  override fun updateWorkout(workout: Workout) {
    _workouts.update { current ->
      current.map { if (it.id == workout.id) workout else it }
    }
  }

  override fun removeWorkout(id: String) {
    _workouts.update { current -> current.filterNot { it.id == id } }
  }
}

private val defaultTabataWorkouts = listOf(
  Workout(
    title = "Classic Tabata",
    rounds = 2,
    restBetweenRoundsSeconds = 30,
    exercises = listOf(
      Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Bodyweight Squats", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Plank Hold", workSeconds = 20, restSeconds = 10)
    )
  ),
  Workout(
    title = "Core HIIT Burner",
    rounds = 2,
    restBetweenRoundsSeconds = 30,
    exercises = listOf(
      Exercise(name = "Bicycle Crunches", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Mountain Climbers", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Russian Twists", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Plank Hold", workSeconds = 20, restSeconds = 10)
    )
  ),
  Workout(
    title = "Cardio Blast",
    rounds = 3,
    restBetweenRoundsSeconds = 45,
    exercises = listOf(
      Exercise(name = "Burpees", workSeconds = 20, restSeconds = 10),
      Exercise(name = "High Knees", workSeconds = 20, restSeconds = 10),
      Exercise(name = "Jump Squats", workSeconds = 20, restSeconds = 10)
    )
  )
)
