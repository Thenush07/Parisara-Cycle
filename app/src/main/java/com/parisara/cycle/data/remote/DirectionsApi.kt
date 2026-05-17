package com.parisara.cycle.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "bicycling",
        @Query("avoid") avoid: String = "highways",
        @Query("key") apiKey: String
    ): DirectionsResponse
}

data class DirectionsResponse(
    val routes: List<RouteDto> = emptyList(),
    val status: String = ""
)

data class RouteDto(
    @SerializedName("overview_polyline") val overviewPolyline: PolylineDto? = null,
    val legs: List<LegDto> = emptyList()
)

data class PolylineDto(val points: String = "")

data class LegDto(
    val distance: TextValueDto? = null,
    val duration: TextValueDto? = null
)

data class TextValueDto(val value: Long = 0, val text: String = "")
