package com.event.abin.di

import android.content.Context
import com.event.abin.data.EventRepository
import com.event.abin.data.local.room.EventDatabase
import com.event.abin.data.remote.retrofit.ApiConfig
import com.event.abin.ui.setting.SettingPreferences
import com.event.abin.ui.setting.dataStore


object Injection {
    fun provideRepository(context: Context): EventRepository {
        val apiService = ApiConfig.getApiService()
        val database = EventDatabase.getInstance(context)
        val dao = database.eventDao()
        return EventRepository.getInstance(apiService, dao)
    }

    fun providePreferences(context: Context): SettingPreferences {
        return SettingPreferences.getInstance(context.dataStore)
    }
}