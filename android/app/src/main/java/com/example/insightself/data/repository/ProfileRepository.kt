package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.ChangePasswordRequest
import com.example.insightself.data.model.ProfileDto
import com.google.gson.Gson
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

class ProfileRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) : ProfileGateway {

    override suspend fun getProfile(userId: Long): ApiResponse<ProfileDto> {
        return try {
            println("=== [ProfileRepository] Getting profile for userId: $userId")
            val response = apiService.getProfile(userId)
            println("=== [ProfileRepository] Get profile response: success=${response.success}, message=${response.message}")
            response
        } catch (e: HttpException) {
            // Startup routing needs these status codes to remain structural, not localized strings.
            if (e.code() == 401 || e.code() == 404) {
                throw e
            }
            println("=== [ProfileRepository] HTTP error: ${e.code()}, message: ${e.message()}")
            val errorMessage = parseErrorMessage(e)
            ApiResponse(success = false, message = errorMessage, data = null)
        } catch (e: IOException) {
            println("=== [ProfileRepository] Network error: ${e.message}")
            ApiResponse(success = false, message = "网络连接失败，请检查网络后重试", data = null)
        } catch (e: Exception) {
            println("=== [ProfileRepository] Unexpected error: ${e.message}")
            e.printStackTrace()
            ApiResponse(success = false, message = "发生未知错误，请稍后重试", data = null)
        }
    }

    suspend fun updateProfile(userId: Long, profile: ProfileDto): ApiResponse<ProfileDto> {
        return try {
            println("=== [ProfileRepository] Updating profile for userId: $userId")
            println("=== [ProfileRepository] Profile data: $profile")
            val response = apiService.updateProfile(userId, profile)
            println("=== [ProfileRepository] Update response: success=${response.success}, message=${response.message}")
            response
        } catch (e: HttpException) {
            println("=== [ProfileRepository] HTTP error: ${e.code()}, message: ${e.message()}")
            val errorMessage = parseErrorMessage(e)
            ApiResponse(success = false, message = errorMessage, data = null)
        } catch (e: IOException) {
            println("=== [ProfileRepository] Network error: ${e.message}")
            ApiResponse(success = false, message = "网络连接失败，请检查网络后重试", data = null)
        } catch (e: Exception) {
            println("=== [ProfileRepository] Unexpected error: ${e.message}")
            e.printStackTrace()
            ApiResponse(success = false, message = "发生未知错误，请稍后重试", data = null)
        }
    }

    suspend fun createProfile(userId: Long, profile: ProfileDto): ApiResponse<ProfileDto> {
        return try {
            println("=== [ProfileRepository] Creating profile for userId: $userId")
            println("=== [ProfileRepository] Profile data: $profile")
            val response = apiService.createProfile(userId, profile)
            println("=== [ProfileRepository] Create response: success=${response.success}, message=${response.message}")
            response
        } catch (e: HttpException) {
            println("=== [ProfileRepository] HTTP error: ${e.code()}, message: ${e.message()}")
            val errorMessage = parseErrorMessage(e)
            ApiResponse(success = false, message = errorMessage, data = null)
        } catch (e: IOException) {
            println("=== [ProfileRepository] Network error: ${e.message}")
            ApiResponse(success = false, message = "网络连接失败，请检查网络后重试", data = null)
        } catch (e: Exception) {
            println("=== [ProfileRepository] Unexpected error: ${e.message}")
            e.printStackTrace()
            ApiResponse(success = false, message = "发生未知错误，请稍后重试", data = null)
        }
    }

    suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): ApiResponse<Boolean> {
        return try {
            println("=== [ProfileRepository] Changing password for userId: $userId")
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val response = apiService.changePassword(userId, request)
            println("=== [ProfileRepository] Change password response: success=${response.success}, message=${response.message}")
            response
        } catch (e: HttpException) {
            println("=== [ProfileRepository] HTTP error: ${e.code()}, message: ${e.message()}")
            val errorMessage = parseErrorMessage(e)
            println("=== [ProfileRepository] Parsed error message: $errorMessage")
            ApiResponse(success = false, message = errorMessage, data = null)
        } catch (e: IOException) {
            println("=== [ProfileRepository] Network error: ${e.message}")
            ApiResponse(success = false, message = "网络连接失败，请检查网络后重试", data = null)
        } catch (e: Exception) {
            println("=== [ProfileRepository] Unexpected error: ${e.message}")
            e.printStackTrace()
            ApiResponse(success = false, message = "发生未知错误，请稍后重试", data = null)
        }
    }

    // 解析错误消息的辅助函数
    private fun parseErrorMessage(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            println("=== [ProfileRepository] Raw error body: $errorBody")

            if (errorBody.isNullOrBlank()) {
                return when (e.code()) {
                    400 -> "请求参数错误"
                    401 -> "未授权，请重新登录"
                    403 -> "无权限访问"
                    404 -> "资源不存在"
                    500 -> "服务器内部错误"
                    else -> "服务器错误: ${e.code()}"
                }
            }

            // 尝试解析 JSON 错误响应
            try {
                val jsonElement = JsonParser.parseString(errorBody)
                val jsonObject = jsonElement.asJsonObject

                // 尝试获取 message 字段
                var message = ""
                if (jsonObject.has("message")) {
                    message = jsonObject.get("message").asString
                } else if (jsonObject.has("msg")) {
                    message = jsonObject.get("msg").asString
                } else if (jsonObject.has("error")) {
                    message = jsonObject.get("error").asString
                }

                if (message.isNotEmpty()) {
                    // 根据错误码和消息内容进行本地化
                    when {
                        message.contains("old password", ignoreCase = true) ||
                                message.contains("password incorrect", ignoreCase = true) -> {
                            return "原密码错误"
                        }
                        message.contains("user not found", ignoreCase = true) -> {
                            return "用户不存在"
                        }
                        message.contains("session expired", ignoreCase = true) -> {
                            return "会话已过期，请重新登录"
                        }
                        else -> return message
                    }
                }
            } catch (jsonError: Exception) {
                println("=== [ProfileRepository] JSON parsing error: ${jsonError.message}")
                // 如果不是 JSON，可能是纯文本错误
                when {
                    errorBody.contains("old password", ignoreCase = true) ||
                            errorBody.contains("password incorrect", ignoreCase = true) -> {
                        return "原密码错误"
                    }
                    errorBody.contains("user not found", ignoreCase = true) -> {
                        return "用户不存在"
                    }
                    else -> return errorBody.take(100)
                }
            }

            // 默认返回 HTTP 状态码对应的消息
            when (e.code()) {
                400 -> "原密码错误"
                401 -> "未授权，请重新登录"
                403 -> "无权限访问"
                404 -> "用户不存在"
                500 -> "服务器内部错误，请稍后重试"
                else -> "服务器错误: ${e.code()}"
            }
        } catch (parseError: Exception) {
            println("=== [ProfileRepository] Error parsing error message: ${parseError.message}")
            "密码修改失败，请稍后重试"
        }
    }
}
