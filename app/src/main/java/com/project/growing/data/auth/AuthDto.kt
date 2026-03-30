package com.project.growing.data.auth

// ────────── Request ──────────

data class LoginRequest(
    val email    : String,
    val password : String,
)

data class SignUpRequest(
    val name     : String,
    val email    : String,
    val password : String,
)

// ────────── Response ──────────

data class LoginResponse(
    val message  : String?  = null,
    val user     : UserDto? = null,  // ← nullable
    val user_id  : String?  = null,  // ← user 없이 바로 올 수도 있음
    val id       : String?  = null,
)

data class SignUpResponse(
    val message  : String?  = null,
    val user     : UserDto? = null,
    val user_id  : String?  = null,
    val id       : String?  = null,
)

data class UserDto(
    val user_id  : String? = null,
    val id       : String? = null,
    val name     : String? = null,
    val email    : String? = null,
)