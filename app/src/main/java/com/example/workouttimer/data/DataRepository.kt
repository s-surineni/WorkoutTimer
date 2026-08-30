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
    Workout(name = "Jumping Jacks", workoutSeconds = 30, cooldownSeconds = 10),
    Workout(name = "Push Ups", workoutSeconds = 45, cooldownSeconds = 15),
    Workout(name = "Plank", workoutSeconds = 60, cooldownSeconds = 20),
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
