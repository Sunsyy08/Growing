package com.project.growing.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.growing.data.local.UserPreferences
import com.project.growing.data.plant.PlantRepository
import com.project.growing.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddPlantUiState(
    val isLoading     : Boolean = false,
    val isSuccess     : Boolean = false,
    val errorMessage  : String? = null,
)

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository      = PlantRepository(application.applicationContext)
    private val userPreferences = UserPreferences(application.applicationContext)

    private val _uiState = MutableStateFlow(AddPlantUiState())
    val uiState: StateFlow<AddPlantUiState> = _uiState.asStateFlow()

    fun registerPlant(
        plantKind     : String,
        plantLocation : String,
        potSize       : String,
        waterCycle    : String,
        imageUri      : Uri?,
    ) {
        if (imageUri == null) {
            _uiState.update { it.copy(errorMessage = "사진을 선택해주세요.") }
            return
        }

        viewModelScope.launch {
            // ── DataStore에서 userId 가져오기 ──────────────────
            val userIdStr = userPreferences.userId.first()
            val userId    = userIdStr?.toIntOrNull()

            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "로그인 정보가 없습니다.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.registerPlant(
                userId        = userId,
                plantKind     = plantKind,
                plantLocation = plantLocation,
                potSize       = potSize,
                waterCycle    = waterCycle,
                imageUri      = imageUri,
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Result.Error   -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun resetState() {
        _uiState.update { AddPlantUiState() }
    }
}