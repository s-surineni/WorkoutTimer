package com.example.workouttimer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for workout history logging and statistics queries.
 */
@Dao
interface WorkoutHistoryDao {

    @Query("SELECT * FROM workout_history ORDER BY timestampMillis DESC")
    fun getAllHistory(): Flow<List<WorkoutHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistoryRecord(record: WorkoutHistoryEntity): Long

    @Query("DELETE FROM workout_history WHERE id = :id")
    fun deleteHistoryRecordById(id: String): Int

    @Query("DELETE FROM workout_history")
    fun clearAllHistory(): Int
}
