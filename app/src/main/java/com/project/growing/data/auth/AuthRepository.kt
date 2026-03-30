package com.project.growing.data.auth

import android.content.Context
import com.project.growing.data.local.UserPreferences
import com.project.growing.network.RetrofitClient
import com.project.growing.util.Result

class AuthRepository(context: Context) {

    private val api             = RetrofitClient.create<AuthApi>()
    private val userPreferences = UserPreferences(context)

    // ── 로그인 ─────────────────────────────────────────────────
    suspend fun login(
        email    : String,
        password : String,
    ): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // ── 가능한 모든 경우 대응 ──────────────────
                    val userId = body.user?.user_id
                        ?: body.user?.id
                        ?: body.user_id
                        ?: body.id
                        ?: ""

                    android.util.Log.d("AuthRepo", "로그인 응답 전체: $body")
                    android.util.Log.d("AuthRepo", "저장할 userId: $userId")

                    if (userId.isNotEmpty()) {
                        userPreferences.saveUserId(userId)
                    }
                    Result.Success(body)
                } else {
                    Result.Error("응답 데이터가 없습니다.")
                }
            } else {
                Result.Error(parseLoginError(response.code()))
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
                if (body != null) {
                    // ── 가능한 모든 경우 대응 ──────────────────
                    val userId = body.user?.user_id
                        ?: body.user?.id
                        ?: body.user_id
                        ?: body.id
                        ?: ""

                    android.util.Log.d("AuthRepo", "회원가입 응답 전체: $body")
                    android.util.Log.d("AuthRepo", "저장할 userId: $userId")

                    if (userId.isNotEmpty()) {
                        userPreferences.saveUserId(userId)
                    }
                    Result.Success(body)
                } else {
                    Result.Error("응답 데이터가 없습니다.")
                }
            } else {
                Result.Error(parseSignUpError(response.code()))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }

    // ── 로그아웃 ───────────────────────────────────────────────
    suspend fun logout() {
        userPreferences.clear()
    }

    // ── userId Flow 노출 ───────────────────────────────────────
    val userId = userPreferences.userId

    private fun parseLoginError(code: Int): String = when (code) {
        400  -> "입력 정보를 확인해주세요."
        401  -> "이메일 또는 비밀번호가 올바르지 않습니다."
        404  -> "존재하지 않는 계정입니다."
        500  -> "서버 오류가 발생했습니다."
        else -> "오류가 발생했습니다. ($code)"
    }

    private fun parseSignUpError(code: Int): String = when (code) {
        400  -> "입력 정보를 확인해주세요."
        404  -> "API 경로를 찾을 수 없습니다."
        409  -> "이미 사용 중인 이메일입니다."
        500  -> "서버 오류가 발생했습니다."
        else -> "오류가 발생했습니다. ($code)"
    }
}