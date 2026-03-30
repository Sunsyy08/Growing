package com.project.growing.data.auth

import com.project.growing.network.RetrofitClient
import com.project.growing.util.Result

class AuthRepository {

    private val api: AuthApi = RetrofitClient.create()

    // ── 로그인 ─────────────────────────────────────────────────
    suspend fun login(
        email    : String,
        password : String,
    ): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("응답 데이터가 없습니다.")
            } else {
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    // ── 회원가입 ───────────────────────────────────────────────
    suspend fun signUp(
        name     : String,
        email    : String,
        password : String,
    ): Result<SignUpResponse> {
        return try {
            val response = api.signUp(SignUpRequest(name, email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Error("응답 데이터가 없습니다.")
            } else {
                Result.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    // ── HTTP 상태코드 → 사용자 메시지 ─────────────────────────
    private fun parseError(code: Int): String = when (code) {
        400  -> "입력 정보를 확인해주세요."
        401  -> "이메일 또는 비밀번호가 올바르지 않습니다."
        404  -> "존재하지 않는 계정입니다."
        409  -> "이미 사용 중인 이메일입니다."
        500  -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        else -> "오류가 발생했습니다. ($code)"
    }
}