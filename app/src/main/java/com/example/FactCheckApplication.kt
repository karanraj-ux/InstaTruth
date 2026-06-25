package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.UserPreferencesRepository
import com.example.data.dataStore
import com.example.data.database.AppDatabase
import com.example.data.database.FactCheckRepository

class FactCheckApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var database: AppDatabase
    lateinit var factCheckRepository: FactCheckRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "fact_check_db"
        ).build()
        factCheckRepository = FactCheckRepository(
            database.factCheckDao(),
            database.roomSessionDao(),
            database.chatDao()
        )
    }
}
