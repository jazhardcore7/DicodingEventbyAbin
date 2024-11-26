package com.event.abin.data.remote.retrofit

import com.event.abin.data.remote.response.DetailEventResponse
import com.event.abin.data.remote.response.EventResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    suspend fun getAllActiveEvent(
        @Query("active") active: Int = 1,
    ): Response<EventResponse>

    @GET("events")
    suspend fun getAllCompletedEvent(
        @Query("active") active: Int = 0,
    ): Response<EventResponse>

    @GET("events")
    suspend fun searchEvent(
        @Query("q") keyword: String,
        @Query("active") active: Int = -1,
    ): Response<EventResponse>

    @GET("events/{id}")
    suspend fun getEventDetail(
        @Path("id") eventId: Int
    ): Response<DetailEventResponse>
}