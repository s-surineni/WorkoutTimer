package com.example.workouttimer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room database for the Workout Timer application.
 */
@Database(
    entities = [WorkoutEntity::class, WorkoutHistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_timer.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val defaultWorkouts = defaultTabataPresets.map { it.toEntity() }
                    database.workoutDao().insertAll(defaultWorkouts)
                }
            }
        }
    }
}

val defaultTabataPresets = listOf(
    Workout(
        id = "default_classic_tabata",
        title = "Classic Tabata",
        rounds = 2,
        restBetweenRoundsSeconds = 30,
        exercises = listOf(
            Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Bodyweight Squats", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Plank Hold", workSeconds = 20, restSeconds = 10)
        )
    ),
    Workout(
        id = "default_core_hiit",
        title = "Core HIIT Burner",
        rounds = 2,
        restBetweenRoundsSeconds = 30,
        exercises = listOf(
            Exercise(name = "Bicycle Crunches", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Mountain Climbers", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Russian Twists", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Plank Hold", workSeconds = 20, restSeconds = 10)
        )
    ),
    Workout(
        id = "default_cardio_blast",
        title = "Cardio Blast",
        rounds = 3,
        restBetweenRoundsSeconds = 45,
        exercises = listOf(
            Exercise(name = "Burpees", workSeconds = 20, restSeconds = 10),
            Exercise(name = "High Knees", workSeconds = 20, restSeconds = 10),
            Exercise(name = "Jump Squats", workSeconds = 20, restSeconds = 10)
        )
    )
)
