package com.example.workouttimer

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.workouttimer.data.DefaultDataRepository
import com.example.workouttimer.ui.create.CreateTabataScreen
import com.example.workouttimer.ui.main.MainScreen
import com.example.workouttimer.ui.main.MainScreenViewModel

@Composable
fun MainNavigation(
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) }
) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onAddClick = { backStack.add(CreateTabata) },
            onEditClick = { workout -> backStack.add(EditTabata(workout.id)) },
            viewModel = viewModel,
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<CreateTabata> {
          CreateTabataScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onSaveWorkout = { workout ->
              viewModel.addWorkout(workout)
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<EditTabata> { key ->
          val workout = viewModel.getWorkoutById(key.workoutId)
          CreateTabataScreen(
            initialWorkout = workout,
            onNavigateBack = { backStack.removeLastOrNull() },
            onSaveWorkout = { updated ->
              viewModel.updateWorkout(updated)
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
      },
  )
}
