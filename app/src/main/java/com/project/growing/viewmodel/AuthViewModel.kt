package com.project.growing.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email             : String  = "",
    val password          : String  = "",
    val isPasswordVisible : Boolean = false,
    val emailError        : String? = null,
    val passwordError     : String? = null,
    val isLoading         : Boolean = false,
    val loginSuccess      : Boolean = false,
    val errorMessage      : String? = null,
)

data class SignUpUiState(
    val name              : String  = "",
    val email             : String  = "",
    val password          : String  = "",
    val isPasswordVisible : Boolean = false,
    val nameError         : String? = null,
    val emailError        : String? = null,
    val passwordError     : String? = null,
    val isLoading         : Boolean = false,
    val signUpSuccess     : Boolean = false,
    val errorMessage      : String? = null,
)

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onLoginEmailChange(value: String) =
        _loginState.update { it.copy(email = value, emailError = null) }

    fun onLoginPasswordChange(value: String) =
        _loginState.update { it.copy(password = value, passwordError = null) }

    fun onLoginPasswordVisibilityToggle() =
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onLoginSubmit() {
        val state = _loginState.value
        var emailError   : String? = null
        var passwordError: String? = null

        if (state.email.isBlank())           emailError    = "이메일을 입력해주세요"
        else if (!state.email.contains("@")) emailError    = "올바른 이메일 형식이 아닙니다"
        if (state.password.isBlank())        passwordError = "비밀번호를 입력해주세요"
        else if (state.password.length < 6)  passwordError = "비밀번호는 6자 이상이어야 합니다"

        if (emailError != null || passwordError != null) {
            _loginState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        // TODO: 백엔드 연결 시 Repository 호출
        _loginState.update { it.copy(loginSuccess = true) }
    }

    fun onLoginErrorDismiss() =
        _loginState.update { it.copy(errorMessage = null) }

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    fun onSignUpNameChange(value: String) =
        _signUpState.update { it.copy(name = value, nameError = null) }

    fun onSignUpEmailChange(value: String) =
        _signUpState.update { it.copy(email = value, emailError = null) }

    fun onSignUpPasswordChange(value: String) =
        _signUpState.update { it.copy(password = value, passwordError = null) }

    fun onSignUpPasswordVisibilityToggle() =
        _signUpState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onSignUpSubmit() {
        val state = _signUpState.value
        var nameError    : String? = null
        var emailError   : String? = null
        var passwordError: String? = null

        if (state.name.isBlank())            nameError     = "이름을 입력해주세요"
        if (state.email.isBlank())           emailError    = "이메일을 입력해주세요"
        else if (!state.email.contains("@")) emailError    = "올바른 이메일 형식이 아닙니다"
        if (state.password.isBlank())        passwordError = "비밀번호를 입력해주세요"
        else if (state.password.length < 6)  passwordError = "비밀번호는 6자 이상이어야 합니다"

        if (nameError != null || emailError != null || passwordError != null) {
            _signUpState.update {
                it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError)
            }
            return
        }
        // TODO: 백엔드 연결 시 Repository 호출
        _signUpState.update { it.copy(signUpSuccess = true) }
    }

    fun onSignUpErrorDismiss() =
        _signUpState.update { it.copy(errorMessage = null) }
}