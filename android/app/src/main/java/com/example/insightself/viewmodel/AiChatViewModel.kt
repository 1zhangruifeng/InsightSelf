package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.AiChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 聊天消息数据类
 * @param isUser true表示用户消息，false表示AI消息
 * @param content 消息内容
 * @param timestamp 时间戳
 */
data class ChatMessage(
    val isUser: Boolean,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI 聊天 ViewModel
 * 管理聊天消息和与后端的通信
 */
class AiChatViewModel(
    private val sessionDataStore: UserSessionDataStore,
    private val apiService: ApiService = RetrofitClient.apiService
) : ViewModel() {

    // 消息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentSessionId: String? = null

    /**
     * 加载初始欢迎消息
     */
    fun loadInitialMessage(isChinese: Boolean) {
        if (_messages.value.isEmpty()) {
            val welcomeMessage = if (isChinese) {
                "您好！我是 InsightSelf AI 助手。\n\n有什么我可以帮助您的吗？"
            } else {
                "Hello! I'm InsightSelf AI Assistant.\n\nHow can I help you?"
            }
            _messages.value = listOf(
                ChatMessage(
                    isUser = false,
                    content = welcomeMessage
                )
            )
        }
    }

    /**
     * 发送用户消息并获取AI回复（使用后端大模型）
     */
    fun sendMessage(content: String, isChinese: Boolean) {
        if (content.isBlank()) return

        // 添加用户消息
        val userMessage = ChatMessage(isUser = true, content = content)
        _messages.value = _messages.value + userMessage

        // 开始加载
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userId = sessionDataStore.getCurrentUserId()

                if (userId == null) {
                    val errorMessage = if (isChinese) {
                        "会话已过期，请重新登录后继续对话。"
                    } else {
                        "Session expired. Please log in again."
                    }
                    _messages.value = _messages.value + ChatMessage(isUser = false, content = errorMessage)
                    return@launch
                }

                // 调用后端 AI API
                val request = AiChatRequest(userId, content, currentSessionId)
                val response = apiService.aiChat(request)

                if (response.success && response.data != null) {
                    currentSessionId = response.data.sessionId
                    val aiReply = response.data.reply
                    _messages.value = _messages.value + ChatMessage(isUser = false, content = aiReply)
                } else {
                    val errorMessage = if (isChinese) {
                        "抱歉，AI 服务暂时不可用。请稍后再试。"
                    } else {
                        "Sorry, AI service is temporarily unavailable. Please try again later."
                    }
                    _messages.value = _messages.value + ChatMessage(isUser = false, content = errorMessage)
                }

            } catch (e: Exception) {
                val errorMessage = if (isChinese) {
                    "网络连接异常，请检查网络后重试。"
                } else {
                    "Network error. Please check your connection and try again."
                }
                _messages.value = _messages.value + ChatMessage(isUser = false, content = errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清空所有消息
     */
    fun clearMessages() {
        _messages.value = emptyList()
        currentSessionId = null
    }

    companion object {
        /**
         * 创建 ViewModel Factory
         */
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AiChatViewModel(sessionDataStore) as T
                }
            }
        }
    }
}
