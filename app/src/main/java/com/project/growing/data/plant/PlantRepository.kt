package com.project.growing.data.plant

import android.content.Context
import android.net.Uri
import com.project.growing.network.RetrofitClient
import com.project.growing.util.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class PlantRepository(private val context: Context) {

    private val api = RetrofitClient.create<PlantApi>()

    // ── get_plant_image는 FileResponse라 URL 직접 구성 ────────
    fun getPlantImageUrl(plantId: Int): String =
        "${RetrofitClient.BASE_URL}get_plant_image?plant_id=$plantId"

    // ── 식물 등록 ──────────────────────────────────────────────
    suspend fun registerPlant(
        userId        : Int,
        plantName     : String,
        plantKind     : String,
        plantLocation : String,
        potSize       : String,
        waterCycle    : String,
        imageUri      : Uri,
    ): Result<PlantResponse> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.Error("이미지를 읽을 수 없습니다.")

            val imageBytes = inputStream.readBytes()
            val imageBody  = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart  = MultipartBody.Part.createFormData(
                name     = "image",
                filename = "plant_${System.currentTimeMillis()}.jpg",
                body     = imageBody,
            )

            android.util.Log.d("PlantRepo", "등록 요청 → user_id=$userId name=$plantName kind=$plantKind")

            val response = api.registerPlant(
                userId        = userId,
                plantName     = plantName,
                plantKind     = plantKind,
                plantLocation = plantLocation,
                potSize       = potSize,
                waterCycle    = waterCycle,
                image         = imagePart,
            )

            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("PlantRepo", "등록 성공: $body")
                if (body != null) Result.Success(body)
                else Result.Error("응답 데이터가 없습니다.")
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("PlantRepo", "등록 실패: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepo", "등록 예외: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    // ── 유저의 식물 ID 목록 ────────────────────────────────────
    suspend fun getPlantIds(userId: Int): Result<List<Int>> {
        return try {
            val response = api.getPlantIds(userId)
            android.util.Log.d("PlantRepo", "식물ID 응답코드: ${response.code()}")
            android.util.Log.d("PlantRepo", "식물ID 바디: ${response.body()}")

            if (response.isSuccessful) {
                val ids = response.body()
                    ?.mapNotNull { it.plant_id }
                    ?: emptyList()
                android.util.Log.d("PlantRepo", "파싱된 IDs: $ids")
                Result.Success(ids)
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("PlantRepo", "식물ID 실패: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepo", "식물ID 예외: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    // ── 건강 점수 + 상태 ───────────────────────────────────────
    suspend fun getPlantScore(plantId: Int): Result<PlantScoreResponse> {
        return try {
            val response = api.getPlantScore(plantId)
            android.util.Log.d("PlantRepo", "점수 응답[$plantId]: ${response.code()} ${response.body()}")
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("PlantRepo", "점수 바디[$plantId]: $body")
                if (body != null) Result.Success(body)
                else Result.Error("점수 데이터가 없습니다.")
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("PlantRepo", "점수 실패[$plantId]: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepo", "점수 예외[$plantId]: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    suspend fun getPlantDetail(plantId: Int): Result<PlantDetailResponse> {
        return try {
            val response = api.getPlantDetail(plantId)
            android.util.Log.d("PlantRepo", "상세 응답[$plantId]: ${response.code()} ${response.body()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("데이터가 없습니다.")
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("PlantRepo", "상세 실패[$plantId]: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepo", "상세 예외[$plantId]: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    suspend fun updatePlant(
        plantId     : Int,
        plantKind   : String,   // "카사바" → "casava", "고무나무" → "rubber"
        imageUri    : Uri,
    ): Result<PlantResponse> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.Error("이미지를 읽을 수 없습니다.")

            val imageBytes = inputStream.readBytes()
            val imageBody  = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart  = MultipartBody.Part.createFormData(
                name     = "image",
                filename = "update_${System.currentTimeMillis()}.jpg",
                body     = imageBody,
            )

            // ── plant_kind → select_model 변환 ───────────────────
            val selectModel = when (plantKind) {
                "카사바"   -> "casava"
                "고무나무" -> "rubber"
                else       -> "casava"  // 기본값
            }

            android.util.Log.d("PlantRepo", "업데이트 요청 → plant_id=$plantId model=$selectModel")

            val response = api.updatePlant(
                plantId     = plantId,
                selectModel = selectModel,
                image       = imagePart,
            )

            android.util.Log.d("PlantRepo", "업데이트 응답: ${response.code()} ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("응답 데이터가 없습니다.")
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("PlantRepo", "업데이트 실패: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepo", "업데이트 예외: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    suspend fun analyzePlant(plantId: Int): Result<PlantAnalysisResponse> {
        return try {
            android.util.Log.d("PlantRepo", "AI 분석 요청 → plant_id=$plantId")
            val response = api.analyzePlant(plantId)
            android.util.Log.d("PlantRepo", "AI 분석 응답: ${response.code()} ${response.body()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("응답 데이터가 없습니다.")
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("PlantRepo", "AI 분석 실패: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepo", "AI 분석 예외: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    private fun parseError(code: Int): String = when (code) {
        400  -> "입력 정보를 확인해주세요."
        401  -> "인증이 필요합니다."
        404  -> "데이터를 찾을 수 없습니다."
        422  -> "입력값 형식이 올바르지 않습니다."
        500  -> "서버 오류가 발생했습니다."
        else -> "오류가 발생했습니다. ($code)"
    }
}