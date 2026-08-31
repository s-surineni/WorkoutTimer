package com.example.workouttimer.data

import com.example.workouttimer.data.local.WorkoutDao
import com.example.workouttimer.data.local.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Persistent SQLite/Room-backed implementation of [DataRepository].
 */
class RoomDataRepository(
    private val workoutDao: WorkoutDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val coroutineScope: CoroutineScope = CoroutineScope(ioDispatcher)
) : DataRepository {

    override val workouts: Flow<List<Workout>> = workoutDao.getAllWorkouts().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun addWorkout(workout: Workout) {
        coroutineScope.launch(ioDispatcher) {
            workoutDao.insertWorkout(workout.toEntity())
        }
    }

    override fun updateWorkout(workout: Workout) {
        coroutineScope.launch(ioDispatcher) {
            workoutDao.updateWorkout(workout.toEntity())
        }
    }

    override fun removeWorkout(id: String) {
        coroutineScope.launch(ioDispatcher) {
            workoutDao.deleteWorkoutById(id)
        }
    }
}
