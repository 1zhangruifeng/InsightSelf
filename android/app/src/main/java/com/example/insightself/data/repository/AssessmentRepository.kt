package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.AssessmentQuestionDto
import com.example.insightself.data.model.AssessmentResultDto
import com.example.insightself.data.model.AssessmentSubmitRequest
import retrofit2.HttpException
import java.io.IOException

class AssessmentRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getQuestions(type: String, userId: Long): ApiResponse<List<AssessmentQuestionDto>> {
        return try {
            apiService.assessmentQuestionsWithLanguage(type, userId)
        } catch (e: HttpException) {
            ApiResponse(success = false, message = "Server error: ${e.code()}", data = null)
        } catch (e: IOException) {
            ApiResponse(success = false, message = "Network error: ${e.message}", data = null)
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Unexpected error: ${e.message}", data = null)
        }
    }

    suspend fun submitAssessment(type: String, request: AssessmentSubmitRequest): ApiResponse<AssessmentResultDto> {
        return try {
            apiService.submitAssessment(type, request)
        } catch (e: HttpException) {
            ApiResponse(success = false, message = "Server error: ${e.code()}", data = null)
        } catch (e: IOException) {
            ApiResponse(success = false, message = "Network error: ${e.message}", data = null)
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Unexpected error: ${e.message}", data = null)
        }
    }

    suspend fun getResults(userId: Long): ApiResponse<List<AssessmentResultDto>> {
        return try {
            apiService.assessmentResults(userId)
        } catch (e: HttpException) {
            ApiResponse(success = false, message = "Server error: ${e.code()}", data = null)
        } catch (e: IOException) {
            ApiResponse(success = false, message = "Network error: ${e.message}", data = null)
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Unexpected error: ${e.message}", data = null)
        }
    }
}