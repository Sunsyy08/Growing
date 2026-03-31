package com.project.growing.data.plant

// ────────── Response ──────────

data class PlantResponse(
    val message   : String?  = null,
    val plant_id  : Int?     = null,
    val plant     : PlantDto? = null,
)

data class PlantDto(
    val plant_id       : Int?    = null,
    val user_id        : Int?    = null,
    val plant_kind     : String? = null,
    val plant_location : String? = null,
    val pot_size       : String? = null,
    val water_cycle    : String? = null,
    val image          : String? = null,
)