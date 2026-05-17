package com.parisara.cycle.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/** Generative AI safety tip enhancement via Gemini REST API. */
interface GeminiApi {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiContent(val parts: List<GeminiPart>)

data class GeminiPart(val text: String)

data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

data class GeminiCandidate(val content: GeminiContent? = null)
