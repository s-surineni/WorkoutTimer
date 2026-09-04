package com.example.workouttimer.ui.main

import com.example.workouttimer.data.DataRepository
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
    val sampleWorkout = Workout(
      id = "1",
      title = "HIIT 1",
      rounds = 2,
      warmupSeconds = 30,
      cooldownSeconds = 30,
      exercises = listOf(Exercise(name = "Squats", workSeconds = 20, restSeconds = 10))
    )
    val repository = FakeWorkoutRepository(
      initial = listOf(sampleWorkout)
    )
    val viewModel = MainScreenViewModel(repository)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect {}
    }
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is MainScreenUiState.Success)
    val success = state as MainScreenUiState.Success
    assertEquals(1, success.workouts.size)
    assertEquals("HIIT 1", success.workouts.first().title)
    assertEquals(30, success.workouts.first().warmupSeconds)
    assertEquals(30, success.workouts.first().cooldownSeconds)
  }

  @Test
  fun uiState_handlesErrorFromRepository() = runTest {
    val repository = ErrorWorkoutRepository(RuntimeException("Database connection failed"))
    val viewModel = MainScreenViewModel(repository)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect {}
    }
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is MainScreenUiState.Error)
    assertEquals("Database connection failed", (state as MainScreenUiState.Error).throwable.message)
  }

  @Test
  fun addWorkout_addsMultiExerciseWorkoutToRepository() = runTest {
    val repository = FakeWorkoutRepository(initial = emptyList())
    val viewModel = MainScreenViewModel(repository)

    val newWorkout = Workout(
      title = "Full Body Tabata",
      rounds = 3,
      restBetweenRoundsSeconds = 45,
      warmupSeconds = 30,
      cooldownSeconds = 30,
      exercises = listOf(
        Exercise(name = "Burpees", workSeconds = 20, restSeconds = 10),
        Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
      )
    )

    viewModel.addWorkout(newWorkout)

    val workouts = repository.workouts.first()
    assertEquals(1, workouts.size)
    val saved = workouts.first()
    assertEquals("Full Body Tabata", saved.title)
    assertEquals(3, saved.rounds)
    assertEquals(2, saved.exercises.size)
    assertEquals(30, saved.warmupSeconds)
    assertEquals(30, saved.cooldownSeconds)
  }

  @Test
  fun updateWorkout_updatesExistingWorkoutInRepository() = runTest {
    val initialWorkout = Workout(
      id = "w1",
      title = "Initial HIIT",
      rounds = 1,
      exercises = listOf(Exercise(name = "Burpees", workSeconds = 20, restSeconds = 10))
    )
    val repository = FakeWorkoutRepository(initial = listOf(initialWorkout))
    val viewModel = MainScreenViewModel(repository)

    val updatedWorkout = initialWorkout.copy(title = "Updated HIIT", rounds = 4)
    viewModel.updateWorkout(updatedWorkout)

    val workouts = repository.workouts.first()
    assertEquals(1, workouts.size)
    assertEquals("Updated HIIT", workouts.first().title)
    assertEquals(4, workouts.first().rounds)
  }

  @Test
  fun getWorkoutById_returnsMatchingWorkoutWhenStateIsSuccess() = runTest {
    val workout = Workout(id = "target123", title = "Target", exercises = emptyList())
    val repository = FakeWorkoutRepository(initial = listOf(workout))
    val viewModel = MainScreenViewModel(repository)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect {}
    }
    advanceUntilIdle()

    val found = viewModel.getWorkoutById("target123")
    assertNotNull(found)
    assertEquals("Target", found?.title)

    val notFound = viewModel.getWorkoutById("nonexistent")
    assertNull(notFound)
  }

  @Test
  fun removeWorkout_removesWorkoutFromRepositoryAndStopsActiveIfMatching() = runTest {
    val workout = Workout(
      id = "123",
      title = "Quick Tabata",
      rounds = 1,
      exercises = listOf(Exercise(name = "Lunges", workSeconds = 30, restSeconds = 15))
    )
    val repository = FakeWorkoutRepository(initial = listOf(workout))
    val viewModel = MainScreenViewModel(repository)

    viewModel.startWorkout(workout)
    assertNotNull(viewModel.activeWorkout.value)

    viewModel.removeWorkout("123")

    val workouts = repository.workouts.first()
    assertTrue(workouts.isEmpty())
    assertNull(viewModel.activeWorkout.value)
  }

  @Test
  fun activeWorkout_startAndStopManagesState() = runTest {
    val repository = FakeWorkoutRepository()
    val viewModel = MainScreenViewModel(repository)

    val workout = Workout(
      id = "abc",
      title = "Core",
      rounds = 1,
      exercises = listOf(Exercise(name = "Plank", workSeconds = 30, restSeconds = 10))
    )

    assertNull(viewModel.activeWorkout.value)

    viewModel.startWorkout(workout)
    assertEquals(workout, viewModel.activeWorkout.value)

    viewModel.stopWorkout()
    assertNull(viewModel.activeWorkout.value)
  }
}

private class LoadingWorkoutRepository : DataRepository {
  private val _workouts = MutableSharedFlow<List<Workout>>()
  override val workouts: Flow<List<Workout>> = _workouts.asSharedFlow()

  override fun addWorkout(workout: Workout) {}
  override fun updateWorkout(workout: Workout) {}
  override fun removeWorkout(id: String) {}
}

private class ErrorWorkoutRepository(val exception: Throwable) : DataRepository {
  override val workouts: Flow<List<Workout>> = flow { throw exception }

  override fun addWorkout(workout: Workout) {}
  override fun updateWorkout(workout: Workout) {}
  override fun removeWorkout(id: String) {}
}

private class FakeWorkoutRepository(
  initial: List<Workout> = emptyList()
) : DataRepository {
  private val _workouts = MutableStateFlow(initial)
  override val workouts: Flow<List<Workout>> = _workouts.asStateFlow()

  override fun addWorkout(workout: Workout) {
    _workouts.update { it + workout }
  }

  override fun updateWorkout(workout: Workout) {
    _workouts.update { current -> current.map { if (it.id == workout.id) workout else it } }
  }

  override fun removeWorkout(id: String) {
    _workouts.update { current -> current.filterNot { it.id == id } }
  }
}
