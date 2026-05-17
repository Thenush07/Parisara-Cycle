package com.parisara.cycle.di

import android.content.Context
import com.parisara.cycle.BuildConfig
import com.parisara.cycle.data.remote.DirectionsApi
import com.parisara.cycle.data.remote.GeminiApi
import com.parisara.cycle.data.repository.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mapsRetrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geminiRetrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val directionsApi: DirectionsApi = mapsRetrofit.create(DirectionsApi::class.java)
    val geminiApi: GeminiApi = geminiRetrofit.create(GeminiApi::class.java)
    val mapsApiKey: String = BuildConfig.MAPS_API_KEY
    val geminiApiKey: String = BuildConfig.GEMINI_API_KEY

    val authRepository = AuthRepository()
    val userRepository = UserRepository()
    val rideRepository = RideRepository()
    val reportRepository = ReportRepository()
    val pitStopRepository = PitStopRepository()
    val buddyRepository = BuddyRepository()
    val notificationRepository = NotificationRepository()
    val routeRepository = RouteRepository(directionsApi, mapsApiKey)
    val aiRepository = AiRepository(geminiApi, geminiApiKey)
    val adminRepository = AdminRepository()
    val userPreferencesRepository = UserPreferencesRepository(context)
}
