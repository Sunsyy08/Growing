package com.project.growing.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.growing.data.local.UserPreferences
import com.project.growing.data.plant.GraphPointDto
import com.project.growing.data.plant.PlantAnalysisResponse
import com.project.growing.data.plant.PlantDetailResponse
import com.project.growing.data.plant.PlantRepository
import com.project.growing.data.plant.ProfileResponse
import com.project.growing.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlantCardData(
    val plantId   : Int,
    val plantName : String?,
    val plantKind : String?,
    val imageUrl  : String?,
    val score     : Int?,
    val status    : String?,
)

data class AddPlantUiState(
    val isLoading    : Boolean = false,
    val isSuccess    : Boolean = false,
    val errorMessage : String? = null,
)

data class HomeUiState(
    val isLoading    : Boolean             = false,
    val plants       : List<PlantCardData> = emptyList(),
    val errorMessage : String?             = null,
)

data class PlantDetailUiState(
    val isLoading    : Boolean              = false,
    val detail       : PlantDetailResponse? = null,
    val errorMessage : String?              = null,
)
// ── 업데이트 UI 상태 추가 ─────────────────────────────────────
data class UpdatePlantUiState(
    val isLoading    : Boolean = false,
    val isSuccess    : Boolean = false,
    val errorMessage : String? = null,
)

// ── AI 분석 UI 상태 ───────────────────────────────────────────
data class AiAnalysisUiState(
    val isLoading    : Boolean                = false,
    val analysis     : PlantAnalysisResponse? = null,
    val errorMessage : String?                = null,
)

// ── 기록 화면 UI 상태 ─────────────────────────────────────────
data class RecordUiState(
    val isLoading    : Boolean          = false,
    val graphPoints  : List<GraphPointDto> = emptyList(),
    val errorMessage : String?          = null,
)

// ── 최근 기록 UI 상태 ─────────────────────────────────────────
data class RecentRecordUiState(
    val isLoading  : Boolean             = false,
    val imageUrls  : List<String>        = emptyList(),  // 실제 이미지 URL
    val score      : Int?                = null,
    val status     : String?             = null,
    val plantName  : String?             = null,
    val plantKind  : String?             = null,
    val errorMessage: String?            = null,
)

// ── 프로필 UI 상태 ────────────────────────────────────────────
data class ProfileUiState(
    val isLoading    : Boolean         = false,
    val profile      : ProfileResponse? = null,
    val errorMessage : String?         = null,
)

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository      = PlantRepository(application.applicationContext)
    private val userPreferences = UserPreferences(application.applicationContext)

    private val _addState    = MutableStateFlow(AddPlantUiState())
    val uiState: StateFlow<AddPlantUiState> = _addState.asStateFlow()

    private val _homeState   = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _detailState = MutableStateFlow(PlantDetailUiState())
    val detailState: StateFlow<PlantDetailUiState> = _detailState.asStateFlow()

    private val _updateState = MutableStateFlow(UpdatePlantUiState())
    val updateState: StateFlow<UpdatePlantUiState> = _updateState.asStateFlow()

    private val _analysisState = MutableStateFlow(AiAnalysisUiState())
    val analysisState: StateFlow<AiAnalysisUiState> = _analysisState.asStateFlow()

    private val _recordState = MutableStateFlow(RecordUiState())
    val recordState: StateFlow<RecordUiState> = _recordState.asStateFlow()

    private val _recentState = MutableStateFlow(RecentRecordUiState())
    val recentState: StateFlow<RecentRecordUiState> = _recentState.asStateFlow()

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

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
                    val plantIds: List<Int> = idsResult.data
                    android.util.Log.d("PlantVM", "식물 ID 목록: $plantIds")

                    if (plantIds.isEmpty()) {
                        _homeState.update { it.copy(isLoading = false, plants = emptyList()) }
                        return@launch
                    }

                    val plantCards = plantIds.map { plantId ->
                        async {
                            val scoreResult = repository.getPlantScore(plantId)
                            val imageUrl    = repository.getPlantImageUrl(plantId)

                            val score     = when (scoreResult) {
                                is Result.Success -> scoreResult.data.score?.toInt()
                                else              -> null
                            }
                            val status    = when (scoreResult) {
                                is Result.Success -> scoreResult.data.status
                                else              -> null
                            }
                            // ── get_score에서 종류, 이름 받아옴 ──────
                            val plantKind = when (scoreResult) {
                                is Result.Success -> scoreResult.data.kind
                                else              -> null
                            }
                            val plantName = when (scoreResult) {
                                is Result.Success -> scoreResult.data.name
                                else              -> null
                            }

                            android.util.Log.d("PlantVM", "plantId=$plantId name=$plantName kind=$plantKind score=$score status=$status")

                            PlantCardData(
                                plantId   = plantId,
                                plantName = plantName,
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
        plantName     : String,
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
                    android.util.Log.d("PlantVM", "등록 성공: ${result.data}")
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

    // ══════════════════════════════════════════════════════════
    // 식물 상세
    // ══════════════════════════════════════════════════════════
    fun loadPlantDetail(plantId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, errorMessage = null) }

            val detailDeferred = async { repository.getPlantDetail(plantId) }
            val scoreDeferred  = async { repository.getPlantScore(plantId) }

            val detailResult = detailDeferred.await()
            val scoreResult  = scoreDeferred.await()

            val score = when (scoreResult) {
                is Result.Success -> scoreResult.data.score?.toInt()
                else              -> null
            }

            when (detailResult) {
                is Result.Success -> {
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

    fun updatePlant(
        plantId   : Int,
        plantKind : String,
        imageUri  : Uri,
    ) {
        viewModelScope.launch {
            _updateState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.updatePlant(
                plantId   = plantId,
                plantKind = plantKind,
                imageUri  = imageUri,
            )) {
                is Result.Success -> {
                    android.util.Log.d("PlantVM", "업데이트 성공: ${result.data}")
                    _updateState.update { it.copy(isLoading = false, isSuccess = true) }
                    // ── 상세 화면 데이터 갱신 ────────────────────
                    loadPlantDetail(plantId)
                }
                is Result.Error -> {
                    android.util.Log.e("PlantVM", "업데이트 실패: ${result.message}")
                    _updateState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadAiAnalysis(plantId: Int) {
        viewModelScope.launch {
            _analysisState.update { it.copy(isLoading = true, errorMessage = null) }

            // ── analyze_plant + get_score 병렬 요청 ───────────────
            val analysisDeferred = async { repository.analyzePlant(plantId) }
            val scoreDeferred    = async { repository.getPlantScore(plantId) }

            val analysisResult = analysisDeferred.await()
            val scoreResult    = scoreDeferred.await()

            // ── get_score에서 점수 가져오기 (홈/디테일과 동일) ────
            val score = when (scoreResult) {
                is Result.Success -> scoreResult.data.score?.toInt()
                else              -> null
            }

            when (analysisResult) {
                is Result.Success -> {
                    // analyze_plant 결과에 get_score 점수 덮어씌우기
                    val analysis = analysisResult.data.copy(
                        score = score ?: analysisResult.data.score
                    )
                    android.util.Log.d("PlantVM", "AI 분석 성공: $analysis (점수: $score)")
                    _analysisState.update { it.copy(isLoading = false, analysis = analysis) }
                }
                is Result.Error -> {
                    android.util.Log.e("PlantVM", "AI 분석 실패: ${analysisResult.message}")
                    _analysisState.update { it.copy(isLoading = false, errorMessage = analysisResult.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadGraph(plantId: Int) {
        viewModelScope.launch {
            _recordState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.drawGraph(plantId)) {
                is Result.Success -> {
                    android.util.Log.d("PlantVM", "그래프 데이터: ${result.data}")
                    _recordState.update { it.copy(isLoading = false, graphPoints = result.data) }
                }
                is Result.Error -> {
                    android.util.Log.e("PlantVM", "그래프 로드 실패: ${result.message}")
                    _recordState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadRecentRecords() {
        viewModelScope.launch {
            val userId = userPreferences.userId.first()?.toIntOrNull()
                ?: return@launch

            _recentState.update { it.copy(isLoading = true, errorMessage = null) }

            // ── 이미지 목록 + 점수 병렬 요청 ─────────────────────
            val imageDeferred = async { repository.getRecentImageUrls(userId) }
            val scoreDeferred = async { repository.getRecentScore(userId) }

            val imageResult = imageDeferred.await()
            val scoreResult = scoreDeferred.await()

            val imageUrls = when (imageResult) {
                is Result.Success -> imageResult.data.mapNotNull { it.image_url }
                    .map { repository.getRecentImageUrl(it) }
                else -> emptyList()
            }

            val score     = when (scoreResult) { is Result.Success -> scoreResult.data.score?.toInt() else -> null }
            val status    = when (scoreResult) { is Result.Success -> scoreResult.data.status else -> null }
            val plantName = when (scoreResult) { is Result.Success -> scoreResult.data.name else -> null }
            val plantKind = when (scoreResult) { is Result.Success -> scoreResult.data.kind else -> null }

            android.util.Log.d("PlantVM", "최근기록 imageUrls=$imageUrls score=$score status=$status")

            _recentState.update {
                it.copy(
                    isLoading  = false,
                    imageUrls  = imageUrls,
                    score      = score,
                    status     = status,
                    plantName  = plantName,
                    plantKind  = plantKind,
                )
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = userPreferences.userId.first()?.toIntOrNull()
                ?: return@launch

            _profileState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getProfile(userId)) {
                is Result.Success -> {
                    android.util.Log.d("PlantVM", "프로필 로드 성공: ${result.data}")
                    _profileState.update { it.copy(isLoading = false, profile = result.data) }
                }
                is Result.Error -> {
                    android.util.Log.e("PlantVM", "프로필 로드 실패: ${result.message}")
                    _profileState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun resetUpdateState() {
        _updateState.update { UpdatePlantUiState() }
    }


    fun resetState() {
        _addState.update { AddPlantUiState() }
    }
}