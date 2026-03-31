    package com.project.growing.data.plant

    import android.content.Context
    import android.net.Uri
    import com.project.growing.network.RetrofitClient
    import com.project.growing.util.Result
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import okhttp3.MultipartBody
    import okhttp3.RequestBody
    import okhttp3.RequestBody.Companion.toRequestBody
    import java.io.InputStream

    class PlantRepository(private val context: Context) {

        private val api = RetrofitClient.create<PlantApi>()

        suspend fun registerPlant(
            userId        : Int,
            plantKind     : String,
            plantLocation : String,
            potSize       : String,
            waterCycle    : String,
            imageUri      : Uri,
        ): Result<PlantResponse> {
            return try {
                // ── form-data 텍스트 필드 생성 ─────────────────────────
                fun String.toFormBody(): RequestBody =
                    toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

                val userIdBody        = userId.toString().toFormBody()
                val plantKindBody     = plantKind.toFormBody()
                val plantLocationBody = plantLocation.toFormBody()
                val potSizeBody       = potSize.toFormBody()
                val waterCycleBody    = waterCycle.toFormBody()

                android.util.Log.d("PlantRepo", "=== 식물 등록 요청 ===")
                android.util.Log.d("PlantRepo", "user_id       : $userId")
                android.util.Log.d("PlantRepo", "plant_kind    : $plantKind")
                android.util.Log.d("PlantRepo", "plant_location: $plantLocation")
                android.util.Log.d("PlantRepo", "pot_size      : $potSize")
                android.util.Log.d("PlantRepo", "water_cycle   : $waterCycle")
                android.util.Log.d("PlantRepo", "imageUri      : $imageUri")

                // ── 이미지 → MultipartBody.Part ──────────────────────
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return Result.Error("이미지를 읽을 수 없습니다.")

                val imageBytes = inputStream.readBytes()
                android.util.Log.d("PlantRepo", "image 크기: ${imageBytes.size} bytes")

                val imageBody  = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart  = MultipartBody.Part.createFormData(
                    name     = "image",
                    filename = "plant_${System.currentTimeMillis()}.jpg",
                    body     = imageBody,
                )

                val response = api.registerPlant(
                    userId        = userId,
                    plantKind     = plantKind,
                    plantLocation = plantLocation,
                    potSize       = potSize,
                    waterCycle    = waterCycle,
                    image         = imagePart,
                )

                android.util.Log.d("PlantRepo", "응답 코드: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("PlantRepo", "응답 바디: $body")
                    if (body != null) Result.Success(body)
                    else Result.Error("응답 데이터가 없습니다.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("PlantRepo", "에러 코드: ${response.code()}")
                    android.util.Log.e("PlantRepo", "에러 바디: $errorBody")
                    Result.Error(parsePlantError(response.code()))
                }
            } catch (e: Exception) {
                android.util.Log.e("PlantRepo", "예외 발생: ${e.message}")
                Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }

        private fun parsePlantError(code: Int): String = when (code) {
            400  -> "입력 정보를 확인해주세요."
            401  -> "인증이 필요합니다."
            422 -> "입력값 형식이 올바르지 않습니다."
            500  -> "서버 오류가 발생했습니다."
            else -> "오류가 발생했습니다. ($code)"
        }
    }