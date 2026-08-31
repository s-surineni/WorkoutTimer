package com.example.workouttimer

import android.app.Application
import com.example.workouttimer.data.DataRepository
import com.example.workouttimer.data.RoomDataRepository
import com.example.workouttimer.data.local.AppDatabase

class WorkoutTimerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: DataRepository by lazy { RoomDataRepository(database.workoutDao()) }
}
