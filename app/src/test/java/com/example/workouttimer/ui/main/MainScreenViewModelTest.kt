package com.example.workouttimer.ui.main

import com.example.workouttimer.data.DataRepository
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun uiState_initiallyLoading() = runTest {
    val repository = LoadingWorkoutRepository()
    val viewModel = MainScreenViewModel(repository)
    assertEquals(MainScreenUiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun uiState_loadsWorkoutsSuccessfully() = runTest {
    val repository = FakeWorkoutRepository(
      initial = listOf(Workout(id = "1", name = "Squats", workoutSeconds = 30, cooldownSeconds = 10))
    )
    val viewModel = MainScreenViewModel(repository)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect {}
    }
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is MainScreenUiState.Success)
    assertEquals(1, (state as MainScreenUiState.Success).data.size)
    assertEquals("Squats", state.data.first().name)
  }

  @Test
  fun addWorkout_addsWorkoutToRepository() = runTest {
    val repository = FakeWorkoutRepository(initial = emptyList())
    val viewModel = MainScreenViewModel(repository)

    viewModel.addWorkout(name = "Burpees", workoutSeconds = 40, cooldownSeconds = 20)

    val workouts = repository.workouts.first()
    assertEquals(1, workouts.size)
    assertEquals("Burpees", workouts.first().name)
    assertEquals(40, workouts.first().workoutSeconds)
    assertEquals(20, workouts.first().cooldownSeconds)
  }

  @Test
  fun removeWorkout_removesWorkoutFromRepository() = runTest {
    val workout = Workout(id = "123", name = "Lunge", workoutSeconds = 30, cooldownSeconds = 10)
    val repository = FakeWorkoutRepository(initial = listOf(workout))
    val viewModel = MainScreenViewModel(repository)

    viewModel.removeWorkout("123")

    val workouts = repository.workouts.first()
    assertTrue(workouts.isEmpty())
  }
}

private class LoadingWorkoutRepository : DataRepository {
  private val _workouts = MutableSharedFlow<List<Workout>>()
  override val workouts: Flow<List<Workout>> = _workouts.asSharedFlow()

  override fun addWorkout(workout: Workout) {}
  override fun removeWorkout(id: String) {}
}

private class FakeWorkoutRepository(initial: List<Workout> = emptyList()) : DataRepository {
  private val _workouts = MutableStateFlow(initial)
  override val workouts: Flow<List<Workout>> = _workouts.asStateFlow()

  override fun addWorkout(workout: Workout) {
    _workouts.update { it + workout }
  }

  override fun removeWorkout(id: String) {
    _workouts.update { current -> current.filterNot { it.id == id } }
  }
}
