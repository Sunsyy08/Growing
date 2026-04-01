package com.project.growing.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.growing.data.local.UserPreferences
import com.project.growing.data.plant.PlantDetailResponse
import com.project.growing.data.plant.PlantRepository
import com.project.growing.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── 홈 화면 식물 카드 데이터 ──────────────────────────────────
data class PlantCardData(
    val plantId   : Int,
    val plantName : String?,   // ← 등록 시 받은 이름
    val plantKind : String?,
    val imageUrl  : String?,
    val score     : Int?,
    val status    : String?,   // "좋음" | "보통" | "나쁨"
)

// ── 식물 등록 UI 상태 ─────────────────────────────────────────
data class AddPlantUiState(
    val isLoading    : Boolean = false,
    val isSuccess    : Boolean = false,
    val errorMessage : String? = null,
)

// ── 홈 화면 UI 상태 ───────────────────────────────────────────
data class HomeUiState(
    val isLoading    : Boolean             = false,
    val plants       : List<PlantCardData> = emptyList(),
    val errorMessage : String?             = null,
)

// ── 상세 UI 상태 ──────────────────────────────────────────────
data class PlantDetailUiState(
    val isLoading    : Boolean              = false,
    val detail       : PlantDetailResponse? = null,
    val errorMessage : String?              = null,
)

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository      = PlantRepository(application.applicationContext)
    private val userPreferences = UserPreferences(application.applicationContext)

    // ── 등록된 식물 이름 임시 저장 (plant_id → plant_name) ────
    private val plantNameCache = mutableMapOf<Int, String>()

    private val _addState  = MutableStateFlow(AddPlantUiState())
    val uiState: StateFlow<AddPlantUiState> = _addState.asStateFlow()

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _detailState = MutableStateFlow(PlantDetailUiState())
    val detailState: StateFlow<PlantDetailUiState> = _detailState.asStateFlow()

    // ══════════════════════════════════════════════════════════
    // 홈 화면 데이터 로드
    // ══════════════════════════════════════════════════════════
    fun loadHomePlants() {
        viewModelScope.launch {
            android.util.Log.d("PlantVM", "loadHomePlants 호출됨")

            val userId = userPreferences.userId.first()?.toIntOrNull()
            android.util.Log.d("PlantVM", "userId: $userId")
            if (userId == null) return@launch

            _homeState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val idsResult = repository.getPlantIds(userId)) {
                is Result.Success -> {
                    // ── List<Pair<Int, String?>> 로 받음 ──────────
                    val pairs: List<Pair<Int, String?>> = idsResult.data
                    android.util.Log.d("PlantVM", "식물 목록: $pairs")

                    if (pairs.isEmpty()) {
                        _homeState.update { it.copy(isLoading = false, plants = emptyList()) }
                        return@launch
                    }

                    val plantCards = pairs.map { pair ->
                        async {
                            val plantId   = pair.first
                            val plantKind = pair.second

                            val scoreResult = repository.getPlantScore(plantId)
                            val imageUrl    = repository.getPlantImageUrl(plantId)

                            val score  = when (scoreResult) {
                                is Result.Success -> scoreResult.data.score?.toInt()
                                else              -> null
                            }
                            val status = when (scoreResult) {
                                is Result.Success -> scoreResult.data.status
                                else              -> null
                            }

                            android.util.Log.d("PlantVM", "plantId=$plantId kind=$plantKind score=$score status=$status")

                            PlantCardData(
                                plantId   = plantId,
                                plantName = plantNameCache[plantId],
                                plantKind = plantKind,
                                imageUrl  = imageUrl,
                                score     = score,
                                status    = status,
                            )
                        }
                    }.awaitAll()

                    android.util.Log.d("PlantVM", "식물 카드 목록: $plantCards")
                    _homeState.update { it.copy(isLoading = false, plants = plantCards) }
                }
                is Result.Error -> {
                    android.util.Log.e("PlantVM", "식물 ID 로드 실패: ${idsResult.message}")
                    _homeState.update { it.copy(isLoading = false, errorMessage = idsResult.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // 식물 등록
    // ══════════════════════════════════════════════════════════
    fun registerPlant(
        plantName     : String,  // ← 추가
        plantKind     : String,
        plantLocation : String,
        potSize       : String,
        waterCycle    : String,
        imageUri      : Uri?,
    ) {
        if (imageUri == null) {
            _addState.update { it.copy(errorMessage = "사진을 선택해주세요.") }
            return
        }

        viewModelScope.launch {
            val userId = userPreferences.userId.first()?.toIntOrNull()
            if (userId == null) {
                _addState.update { it.copy(errorMessage = "로그인 정보가 없습니다.") }
                return@launch
            }

            _addState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.registerPlant(
                userId        = userId,
                plantName     = plantName,
                plantKind     = plantKind,
                plantLocation = plantLocation,
                potSize       = potSize,
                waterCycle    = waterCycle,
                imageUri      = imageUri,
            )) {
                is Result.Success -> {
                    // ── 등록된 plant_id와 plant_name 캐시에 저장 ──
                    val plantId = result.data.plant_id
                        ?: result.data.plant?.plant_id
                    if (plantId != null) {
                        plantNameCache[plantId] = plantName
                        android.util.Log.d("PlantVM", "캐시 저장: $plantId → $plantName")
                    }
                    _addState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadHomePlants()
                }
                is Result.Error   -> {
                    _addState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadPlantDetail(plantId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, errorMessage = null) }

            // ── 상세 정보 + 점수 병렬 요청 ────────────────────────
            val detailDeferred = async { repository.getPlantDetail(plantId) }
            val scoreDeferred  = async { repository.getPlantScore(plantId) }

            val detailResult = detailDeferred.await()
            val scoreResult  = scoreDeferred.await()

            // get_score에서 점수 가져오기 (홈 화면과 동일)
            val score = when (scoreResult) {
                is Result.Success -> scoreResult.data.score?.toInt()
                else              -> null
            }

            when (detailResult) {
                is Result.Success -> {
                    // score를 get_score 결과로 덮어씌우기
                    val detail = detailResult.data.copy(
                        score = score?.toFloat() ?: detailResult.data.score
                    )
                    android.util.Log.d("PlantVM", "상세 데이터: $detail score: $score")
                    _detailState.update { it.copy(isLoading = false, detail = detail) }
                }
                is Result.Error -> {
                    android.util.Log.e("PlantVM", "상세 로드 실패: ${detailResult.message}")
                    _detailState.update { it.copy(isLoading = false, errorMessage = detailResult.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun resetDetailState() {
        _detailState.update { PlantDetailUiState() }
    }

    fun resetState() {
        _addState.update { AddPlantUiState() }
    }
}