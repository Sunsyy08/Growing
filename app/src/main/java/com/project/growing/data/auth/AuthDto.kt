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
    val message : String,
    val user    : UserDto,
)

data class SignUpResponse(
    val message : String,
    val user    : UserDto,
)

data class UserDto(
    val id    : String,
    val name  : String,
    val email : String,
)