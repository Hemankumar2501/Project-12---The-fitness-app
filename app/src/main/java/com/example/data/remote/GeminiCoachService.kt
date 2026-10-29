package com.example.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiCoachService"

class GeminiCoachService {
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are the Project 12 Transformation Coach — an expert, supportive, and balanced personal trainer and nutrition specialist.
                    Your goal is to guide the user through their 12-week body transformation: losing body fat, building lean muscle, following sustainable Indian/South Indian nutrition, and staying consistent with home workouts (dumbbells, bands, bodyweight).
                    
                    Key coaching guidelines:
                    1. Tone: Balanced, encouraging, motivating, and grounded in exercise science. Never judge missed workouts or cheat meals; instead, suggest gentle course corrections.
                    2. Nutrition: Specialize in everyday Indian and South Indian meals (Idli, Dosa, Sambar, Dal, Roti, Rice, Paneer, Eggs, Chicken, Curd, Soy Chunks, Chana). Help users hit their protein targets without needing expensive supplements.
                    3. Workouts: Recommend safe form cues and beginner-friendly substitutions when equipment is limited or user feels joint discomfort.
                    4. Rest & Recovery: Always emphasize sleep, hydration, and active recovery. Rest days are essential for growth.
                    5. Safety: Do not give medical diagnoses or promise unrealistic rapid weight loss.
                    6. Brevity: Keep responses concise, formatted with clear bullet points, and directly actionable on mobile screens.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun getAdvice(userContext: String, userPrompt: String): String {
        return try {
            withTimeout(60_000L) {
                val fullPrompt = """
                    Current User Context:
                    $userContext
                    
                    User Query:
                    $userPrompt
                """.trimIndent()
                val response = model.generateContent(fullPrompt)
                response.text ?: "I'm here to support your 12-week journey! Keep building consistency."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini inference failed: ${e.message}", e)
            "Coach response: ${e.localizedMessage ?: "Unable to connect to AI Coach at the moment. Please verify your connection."}"
        }
    }

    fun streamAdvice(userContext: String, userPrompt: String): Flow<String> = flow {
        val fullPrompt = """
            Current User Context:
            $userContext
            
            User Query:
            $userPrompt
        """.trimIndent()
        withTimeout(60_000L) {
            model.generateContentStream(fullPrompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    emit(chunkText)
                }
            }
        }
    }
}
