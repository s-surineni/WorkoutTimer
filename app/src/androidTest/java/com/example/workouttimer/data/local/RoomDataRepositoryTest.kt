package com.example.workouttimer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.RoomDataRepository
import com.example.workouttimer.data.Workout
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RoomDataRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var workoutDao: WorkoutDao
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: RoomDataRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workoutDao = database.workoutDao()
        repository = RoomDataRepository(
            workoutDao = workoutDao,
            ioDispatcher = testDispatcher,
            coroutineScope = testScope
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveWorkout_withWarmupAndCooldown() = testScope.runTest {
        val workout = Workout(
            id = "test_1",
            title = "Morning Tabata",
            rounds = 3,
            restBetweenRoundsSeconds = 30,
            warmupSeconds = 45,
            cooldownSeconds = 60,
            exercises = listOf(
                Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
                Exercise(name = "Squats", workSeconds = 20, restSeconds = 10)
            )
        )

        repository.addWorkout(workout)
        advanceUntilIdle()

        val list = repository.workouts.first()
        assertEquals(1, list.size)
        val loaded = list.first()
        assertEquals("Morning Tabata", loaded.title)
        assertEquals(3, loaded.rounds)
        assertEquals(45, loaded.warmupSeconds)
        assertEquals(60, loaded.cooldownSeconds)
        assertEquals(2, loaded.exercises.size)
        assertEquals("Push Ups", loaded.exercises[0].name)
    }

    @Test
    fun updateWorkout_updatesInDatabase() = testScope.runTest {
        val workout = Workout(
            id = "test_2",
            title = "Original Title",
            rounds = 1,
            warmupSeconds = 0,
            cooldownSeconds = 0,
            exercises = listOf(Exercise(name = "Plank", workSeconds = 30, restSeconds = 10))
        )

        repository.addWorkout(workout)
        advanceUntilIdle()

        val updated = workout.copy(
            title = "Edited Title",
            rounds = 4,
            warmupSeconds = 30,
            cooldownSeconds = 30
        )
        repository.updateWorkout(updated)
        advanceUntilIdle()

        val list = repository.workouts.first()
        assertEquals(1, list.size)
        assertEquals("Edited Title", list.first().title)
        assertEquals(4, list.first().rounds)
        assertEquals(30, list.first().warmupSeconds)
        assertEquals(30, list.first().cooldownSeconds)
    }

    @Test
    fun deleteWorkout_removesFromDatabase() = testScope.runTest {
        val workout = Workout(
            id = "test_3",
            title = "To Delete",
            exercises = emptyList()
        )

        repository.addWorkout(workout)
        advanceUntilIdle()

        assertEquals(1, repository.workouts.first().size)

        repository.removeWorkout("test_3")
        advanceUntilIdle()

        assertTrue(repository.workouts.first().isEmpty())
    }
}
