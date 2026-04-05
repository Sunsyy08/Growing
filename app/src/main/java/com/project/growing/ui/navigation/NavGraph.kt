package com.project.growing.ui.navigation

sealed class Screen(val route: String) {

    data object Login       : Screen("login")
    data object SignUp      : Screen("signup")
    data object Home        : Screen("home")
    data object Record      : Screen("record")
    data object Chat        : Screen("chat")
    data object My          : Screen("my")
    data object AddPlant    : Screen("add_plant")
    data object WriteQuestion : Screen("write_question")

    data object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: String) = "plant_detail/$plantId"
        const val ARG_PLANT_ID = "plantId"
    }

    data object AiAnalysis : Screen("ai_analysis/{plantId}") {
        fun createRoute(plantId: Int) = "ai_analysis/$plantId"
        const val ARG_PLANT_ID = "plantId"
    }
    data object ConsultDetail : Screen("consult_detail/{recordId}") {
        fun createRoute(recordId: String) = "consult_detail/$recordId"
        const val ARG_RECORD_ID = "recordId"
    }
}