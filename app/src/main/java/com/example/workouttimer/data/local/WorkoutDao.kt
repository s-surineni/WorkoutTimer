package com.example.workouttimer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the workouts table in SQLite.
 */
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY rowid ASC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    fun getWorkoutById(id: String): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(workouts: List<WorkoutEntity>): List<Long>

    @Update
    fun updateWorkout(workout: WorkoutEntity): Int

    @Query("DELETE FROM workouts WHERE id = :id")
    fun deleteWorkoutById(id: String): Int

    @Query("DELETE FROM workouts")
    fun deleteAll(): Int
}
