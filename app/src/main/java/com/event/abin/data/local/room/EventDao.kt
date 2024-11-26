package com.event.abin.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.event.abin.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getEvents(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Int): LiveData<EventEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

}