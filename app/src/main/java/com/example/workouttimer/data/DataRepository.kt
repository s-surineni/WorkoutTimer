package com.example.workouttimer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface DataRepository {
  val workouts: Flow<List<Workout>>
  fun addWorkout(workout: Workout)
  fun removeWorkout(id: String)
}

class DefaultDataRepository(
  initialWorkouts: List<Workout> = listOf(
    Workout(
      title = "Classic Tabata",
      rounds = 2,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Bodyweight Squats", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Plank", workSeconds = 20, restSeconds = 10),
      )
    ),
    Workout(
      title = "Core HIIT Burner",
      rounds = 2,
      restBetweenRoundsSeconds = 30,
      exercises = listOf(
        Exercise(name = "Bicycle Crunches", workSeconds = 30, restSeconds = 15),
        Exercise(name = "Mountain Climbers", workSeconds = 30, restSeconds = 15),
        Exercise(name = "Russian Twists", workSeconds = 30, restSeconds = 15),
        Exercise(name = "Plank Hold", workSeconds = 30, restSeconds = 15),
      )
    ),
    Workout(
      title = "Cardio Blast",
      rounds = 3,
      restBetweenRoundsSeconds = 40,
      exercises = listOf(
        Exercise(name = "Burpees", workSeconds = 20, restSeconds = 10),
        Exercise(name = "High Knees", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Jump Squats", workSeconds = 20, restSeconds = 10),
      )
    ),
  )
) : DataRepository {
  private val _workouts = MutableStateFlow(initialWorkouts)
  override val workouts: Flow<List<Workout>> = _workouts.asStateFlow()

  override fun addWorkout(workout: Workout) {
    _workouts.update { it + workout }
  }

  override fun removeWorkout(id: String) {
    _workouts.update { current -> current.filterNot { it.id == id } }
  }
}
