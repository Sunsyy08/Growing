package com.project.growing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.growing.data.consult.ConsultRecord
import com.project.growing.data.consult.ConsultRepository
import com.project.growing.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// ── 상담 가능한 전문가 목록 ───────────────────────────────────
enum class ExpertType(val displayName: String, val emoji: String) {
    TSUNDERE ("츤데레 선인장",    "🌵"),
    FAIRY    ("숲의 요정",        "🧚"),
    SCIENTIST("아빠 친구 식물학자", "👨‍🔬"),
}

// ── 질문 작성 UI 상태 ─────────────────────────────────────────
data class WriteQuestionUiState(
    val selectedExpert : ExpertType? = null,
    val message        : String      = "",
    val imageUri       : String?     = null,  // 로컬 이미지 URI
    val isLoading      : Boolean     = false,
    val isSuccess      : Boolean     = false,
    val errorMessage   : String?     = null,
    val answer         : String?     = null,
)

class ConsultViewModel : ViewModel() {

    private val repository = ConsultRepository()

    private val _writeState = MutableStateFlow(WriteQuestionUiState())
    val writeState: StateFlow<WriteQuestionUiState> = _writeState.asStateFlow()

    // ── 상담 기록 (로컬 저장) ─────────────────────────────────
    private val _records = MutableStateFlow<List<ConsultRecord>>(emptyList())
    val records: StateFlow<List<ConsultRecord>> = _records.asStateFlow()

    // ── 입력 ──────────────────────────────────────────────────
    fun onExpertSelected(expert: ExpertType) {
        _writeState.update { it.copy(selectedExpert = expert, errorMessage = null) }
    }

    fun onMessageChange(message: String) {
        _writeState.update { it.copy(message = message, errorMessage = null) }
    }

    fun onImageCaptured(uri: String) {
        _writeState.update { it.copy(imageUri = uri) }
    }

    // ── 질문 제출 ─────────────────────────────────────────────
    fun submitQuestion() {
        val state = _writeState.value

        if (state.selectedExpert == null) {
            _writeState.update { it.copy(errorMessage = "상담사를 선택해주세요.") }
            return
        }
        if (state.message.isBlank()) {
            _writeState.update { it.copy(errorMessage = "질문을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _writeState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.consult(
                expert  = state.selectedExpert.displayName,
                message = state.message,
            )) {
                is Result.Success -> {
                    val answer = result.data.answer ?: ""

                    // ── 상담 기록 로컬 저장 ──────────────────
                    val record = ConsultRecord(
                        id       = UUID.randomUUID().toString(),
                        expert   = state.selectedExpert.displayName,
                        message  = state.message,
                        answer   = answer,
                        imageUri = state.imageUri,  // 이미지 URI 저장
                    )
                    _records.update { listOf(record) + it }

                    android.util.Log.d("ConsultVM", "상담 성공: $record")
                    _writeState.update {
                        it.copy(isLoading = false, isSuccess = true, answer = answer)
                    }
                }
                is Result.Error -> {
                    _writeState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun resetWriteState() {
        _writeState.update { WriteQuestionUiState() }
    }
}