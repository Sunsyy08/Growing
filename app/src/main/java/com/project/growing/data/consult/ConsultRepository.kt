package com.project.growing.data.consult

import com.project.growing.network.RetrofitClient
import com.project.growing.util.Result

class ConsultRepository {

    private val api = RetrofitClient.create<ConsultApi>()

    // ── AI 상담 요청 ───────────────────────────────────────────
    suspend fun consult(
        expert  : String,
        message : String,
    ): Result<ConsultResponse> {
        return try {
            android.util.Log.d("ConsultRepo", "상담 요청 → expert=$expert message=$message")
            val response = api.consult(ConsultRequest(expert, message))
            android.util.Log.d("ConsultRepo", "상담 응답: ${response.code()} ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("응답 데이터가 없습니다.")
            } else {
                val err = response.errorBody()?.string()
                android.util.Log.e("ConsultRepo", "상담 실패: ${response.code()} $err")
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            android.util.Log.e("ConsultRepo", "상담 예외: ${e.message}")
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    private fun parseError(code: Int): String = when (code) {
        400  -> "존재하지 않는 상담사입니다."
        500  -> "서버 오류가 발생했습니다."
        else -> "오류가 발생했습니다. ($code)"
    }
}