package com.example.workouttimer.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttimer.data.DataRepository
import com.example.workouttimer.data.Workout
import com.example.workouttimer.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainScreenViewModel(private val dataRepository: DataRepository) : ViewModel() {
  val uiState: StateFlow<MainScreenUiState> =
    dataRepository.workouts
      .map<List<Workout>, MainScreenUiState>(::Success)
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

  fun addWorkout(name: String, workoutSeconds: Int, cooldownSeconds: Int) {
    dataRepository.addWorkout(
      Workout(
        name = name,
        workoutSeconds = workoutSeconds,
        cooldownSeconds = cooldownSeconds
      )
    )
  }

  fun removeWorkout(id: String) {
    dataRepository.removeWorkout(id)
  }
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: List<Workout>) : MainScreenUiState
}
