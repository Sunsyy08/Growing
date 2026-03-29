package com.project.growing.ui.navigation

sealed class Screen(val route: String) {

    data object Login       : Screen("login")
    data object SignUp      : Screen("signup")
    data object Home        : Screen("home")
    data object Record      : Screen("record")
    data object Chat        : Screen("chat")
    data object My          : Screen("my")

    data object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: String) = "plant_detail/$plantId"
        const val ARG_PLANT_ID = "plantId"
    }
}