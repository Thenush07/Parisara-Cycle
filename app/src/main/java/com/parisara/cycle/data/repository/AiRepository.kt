package com.parisara.cycle.data.repository

import com.parisara.cycle.data.remote.GeminiApi
import com.parisara.cycle.data.remote.GeminiContent
import com.parisara.cycle.data.remote.GeminiPart
import com.parisara.cycle.data.remote.GeminiRequest

/** Generative AI for personalized safety tips and eco motivation messages. */
class AiRepository(
    private val geminiApi: GeminiApi,
    private val apiKey: String
) {
    suspend fun generateSafetyAdvice(context: String): Result<String> = runCatching {
        if (apiKey.isBlank()) {
            return@runCatching "Ride predictably, stay visible, and always wear your helmet. Share your live location with trusted buddies for safer commutes."
        }
        val prompt = "You are a bicycle safety coach for Indian urban commuters. " +
            "Give 3 short bullet tips (max 80 words total) about: $context"
        val response = geminiApi.generateContent(
            apiKey = apiKey,
            request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )
        )
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Stay alert, use hand signals, and keep both hands ready to brake."
    }

    suspend fun generateEcoMessage(co2Grams: Double, distanceKm: Double): Result<String> =
        runCatching {
            if (apiKey.isBlank()) {
                return@runCatching when {
                    co2Grams >= 1000 -> "Amazing! You prevented ${"%.1f".format(co2Grams / 1000)} kg of carbon emissions."
                    co2Grams > 0 -> "You saved ${co2Grams.toInt()}g CO₂ today — every pedal counts!"
                    else -> "Start a ride today and make Bengaluru greener!"
                }
            }
            val prompt = "Write one motivational eco-friendly sentence (max 20 words) for a cyclist who saved ${co2Grams.toInt()}g CO2 riding ${"%.1f".format(distanceKm)} km."
            val response = geminiApi.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Great ride! You're helping the planet one kilometer at a time."
        }
}
