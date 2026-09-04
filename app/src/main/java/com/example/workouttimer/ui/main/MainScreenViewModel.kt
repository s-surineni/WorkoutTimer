package com.example.workouttimer.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttimer.data.DataRepository
import com.example.workouttimer.data.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainScreenViewModel(private val dataRepository: DataRepository) : ViewModel() {
  val uiState: StateFlow<MainScreenUiState> =
    dataRepository.workouts
      .map { workouts -> MainScreenUiState.Success(workouts = workouts) as MainScreenUiState }
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

  private val _activeWorkout = MutableStateFlow<Workout?>(null)
  val activeWorkout: StateFlow<Workout?> = _activeWorkout.asStateFlow()

  fun addWorkout(workout: Workout) {
    dataRepository.addWorkout(workout)
  }

  fun updateWorkout(workout: Workout) {
    if (_activeWorkout.value?.id == workout.id) {
      _activeWorkout.value = workout
    }
    dataRepository.updateWorkout(workout)
  }

  fun removeWorkout(id: String) {
    if (_activeWorkout.value?.id == id) {
      _activeWorkout.value = null
    }
    dataRepository.removeWorkout(id)
  }

  fun getWorkoutById(id: String): Workout? {
    val current = uiState.value
    return if (current is MainScreenUiState.Success) {
      current.workouts.find { it.id == id }
    } else null
  }

  fun startWorkout(workout: Workout) {
    _activeWorkout.value = workout
  }

  fun stopWorkout() {
    _activeWorkout.value = null
  }
}

sealed interface MainScreenUiState {
  data object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(
    val workouts: List<Workout>
  ) : MainScreenUiState {
    val data: List<Workout> get() = workouts
  }
}
