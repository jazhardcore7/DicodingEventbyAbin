package com.event.abin.data

import androidx.lifecycle.LiveData
import com.event.abin.data.local.entity.EventEntity
import com.event.abin.data.local.room.EventDao
import com.event.abin.data.remote.response.Event
import com.event.abin.data.remote.response.ListEventsItem
import com.event.abin.data.remote.retrofit.ApiService

class EventRepository private constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    suspend fun getUpcomingEvent(): Result<List<ListEventsItem>> {
        return try {
            val response = apiService.getAllActiveEvent()
            if (response.isSuccessful) {
                Result.success(response.body()?.listEvents ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load data from API, Status code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getCompletedEvent(): Result<List<ListEventsItem>> {
        return try {
            val response = apiService.getAllCompletedEvent()
            if (response.isSuccessful) {
                Result.success(response.body()?.listEvents ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load data from API, Status code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetailEvent(id: Int): Result<Event> {
        return try {
            val response = apiService.getEventDetail(id)
            if (response.isSuccessful) {
                val event = response.body()?.event ?: throw Exception("Event not found")
                Result.success(event)
            } else {
                Result.failure(Exception("Failed to load data from API, Status code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchEvent(keyword: String): Result<List<ListEventsItem>> {
        return try {
            val response = apiService.searchEvent(keyword)
            if (response.isSuccessful) {
                Result.success(response.body()?.listEvents ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load data from API, Status code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertEvent(event: EventEntity): Boolean {
        return try {
            eventDao.insertEvents(event)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteEvent(event: EventEntity): Boolean {
        return try {
            eventDao.deleteEvent(event)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getEvent(): LiveData<List<EventEntity>> {
        return eventDao.getEvents()
    }

    fun getEventById(eventId: Int): LiveData<EventEntity> {
        return eventDao.getEventById(eventId)
    }

    companion object {
        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(apiService: ApiService, eventDao: EventDao): EventRepository =
            instance ?: synchronized(this) {
                instance ?: EventRepository(apiService, eventDao)
            }.also { instance = it }
    }
}