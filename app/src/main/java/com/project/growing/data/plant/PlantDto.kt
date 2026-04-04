package com.project.growing.data.plant

import com.google.gson.annotations.SerializedName

// ────────── 식물 등록 ──────────

data class PlantResponse(
    val message    : String?   = null,
    val plant_id   : Int?      = null,
    val plant_name : String?   = null,
    val plant      : PlantDto? = null,
    val filename   : String?   = null,  // 서버가 filename으로 줌
)

data class PlantDto(
    val plant_id       : Int?    = null,
    val user_id        : Int?    = null,
    val plant_name     : String? = null,
    val plant_kind     : String? = null,
    val plant_location : String? = null,
    val pot_size       : String? = null,
    val water_cycle    : String? = null,
    val image          : String? = null,
)

// ────────── 식물 ID 목록 ──────────

data class PlantIdItemDto(
    val plant_id : Int? = null,
    val plant_kind : String? = null,
)

// ────────── 건강 점수 ──────────

data class PlantScoreResponse(
    @SerializedName("점수") val score  : Float?  = null,
    @SerializedName("상태") val status : String? = null,
    @SerializedName("종류") val kind   : String? = null,
    @SerializedName("이름") val name   : String? = null,
)

data class PlantDetailResponse(
    val image        : String? = null,
    val plant_kind   : String? = null,
    val plant_location: String? = null,
    val pot_size     : String? = null,
    val water_cycle  : String? = null,
    val sunlight     : String? = null,
    val score        : Float?  = null,
    val analysis_ai  : String? = null,
)