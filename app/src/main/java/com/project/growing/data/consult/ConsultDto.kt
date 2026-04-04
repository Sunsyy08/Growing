package com.project.growing.data.consult

// ────────── Request ──────────

data class ConsultRequest(
    val expert  : String,
    val message : String,
)

// ────────── Response ──────────

data class ConsultResponse(
    val expert : String? = null,
    val answer : String? = null,
)

// ────────── 로컬 저장용 상담 기록 ──────────

data class ConsultRecord(
    val id         : String,
    val expert     : String,
    val message    : String,
    val answer     : String,
    val imageUri   : String? = null,  // 로컬 이미지 URI
    val createdAt  : Long = System.currentTimeMillis(),
)