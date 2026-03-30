package com.project.growing.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.growing.data.auth.AuthRepository
import com.project.growing.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email             : String  = "",
    val password          : String  = "",
    val emailError        : String? = null,
    val passwordError     : String? = null,
    val isPasswordVisible : Boolean = false,
    val isLoading         : Boolean = false,
    val loginSuccess      : Boolean = false,
    val errorMessage      : String? = null,
)

data class SignUpUiState(
    val name              : String  = "",
    val email             : String  = "",
    val password          : String  = "",
    val nameError         : String? = null,
    val emailError        : String? = null,
    val passwordError     : String? = null,
    val isPasswordVisible : Boolean = false,
    val isLoading         : Boolean = false,
    val signUpSuccess     : Boolean = false,
    val errorMessage      : String? = null,
)

// ViewModel → AndroidViewModel 로 변경 (Context 사용)
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application.applicationContext)

    private val _loginState  = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    // ── 저장된 userId 노출 ─────────────────────────────────────
    val userId   = repository.userId    // Flow<String?>

    // ══════════════════════════════════════════════════════════
    // 로그인
    // ══════════════════════════════════════════════════════════

    fun onLoginEmailChange(value: String) {
        _loginState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onLoginPasswordVisibilityToggle() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginSubmit() {
        val state         = _loginState.value
        val emailError    = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _loginState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.login(state.email.trim(), state.password)) {
                is Result.Success -> _loginState.update { it.copy(isLoading = false, loginSuccess = true) }
                is Result.Error   -> _loginState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // 회원가입
    // ══════════════════════════════════════════════════════════

    fun onSignUpNameChange(value: String) {
        _signUpState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onSignUpEmailChange(value: String) {
        _signUpState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onSignUpPasswordChange(value: String) {
        _signUpState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onSignUpPasswordVisibilityToggle() {
        _signUpState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSignUpSubmit() {
        val state         = _signUpState.value
        val nameError     = if (state.name.isBlank()) "이름을 입력해주세요." else null
        val emailError    = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (nameError != null || emailError != null || passwordError != null) {
            _signUpState.update {
                it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _signUpState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.signUp(
                name     = state.name.trim(),
                email    = state.email.trim(),
                password = state.password,
            )) {
                is Result.Success -> _signUpState.update { it.copy(isLoading = false, signUpSuccess = true) }
                is Result.Error   -> _signUpState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    // ── 로그아웃 ───────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // ── 공통 유효성 검사 ───────────────────────────────────────
    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "이메일을 입력해주세요."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "올바른 이메일 형식이 아닙니다."
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank()  -> "비밀번호를 입력해주세요."
        password.length < 6 -> "비밀번호는 6자 이상이어야 합니다."
        else -> null
    }
}