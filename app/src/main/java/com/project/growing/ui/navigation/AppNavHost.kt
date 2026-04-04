package com.project.growing.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.screen.*
import com.project.growing.viewmodel.AuthViewModel
import com.project.growing.viewmodel.ConsultViewModel
import com.project.growing.viewmodel.PlantViewModel

private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Record.route,
    Screen.Chat.route,
    Screen.My.route,
)

private fun routeToTab(route: String?): BottomNavTab = when (route) {
    Screen.Home.route   -> BottomNavTab.HOME
    Screen.Record.route -> BottomNavTab.RECORD
    Screen.Chat.route   -> BottomNavTab.CHAT
    Screen.My.route     -> BottomNavTab.MY
    else                -> BottomNavTab.HOME
}

private fun tabToRoute(tab: BottomNavTab): String = when (tab) {
    BottomNavTab.HOME   -> Screen.Home.route
    BottomNavTab.RECORD -> Screen.Record.route
    BottomNavTab.CHAT   -> Screen.Chat.route
    BottomNavTab.MY     -> Screen.My.route
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute     = currentBackStack?.destination?.route
    val showBottomBar    = currentRoute in bottomBarRoutes
    val selectedTab      = routeToTab(currentRoute)

    // ── ViewModel 최상위에서 하나만 생성 → 화면 간 공유 ─────────
    val consultViewModel : ConsultViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { tab ->
                        navController.navigate(tabToRoute(tab)) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                )
            }
        }
    ) { innerPadding ->
        AnimatedNavHost(
            navController      = navController,
            startDestination   = Screen.Login.route,
            modifier           = Modifier.padding(innerPadding),
            enterTransition    = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(320)) + fadeIn(tween(320)) },
            exitTransition     = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(320)) + fadeOut(tween(320)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(320)) + fadeIn(tween(320)) },
            popExitTransition  = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(320)) + fadeOut(tween(320)) },
        ) {

            // ── 로그인 ────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess     = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    },
                )
            }

            // ── 회원가입 ──────────────────────────────────────────
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack  = { navController.popBackStack() },
                )
            }

            // ── 홈 ────────────────────────────────────────────────
            composable(Screen.Home.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val plantViewModel: PlantViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
                        context.applicationContext as android.app.Application
                    )
                )
                HomeScreen(
                    plantViewModel = plantViewModel,
                    onPlantClick   = { plantId ->
                        navController.navigate(Screen.PlantDetail.createRoute(plantId))
                    },
                    onAddPlant     = {
                        navController.navigate(Screen.AddPlant.route)
                    },
                )
            }

            // ── 기록 ──────────────────────────────────────────────
            composable(Screen.Record.route) {
                RecordScreen()
            }

            // ── 상담 ──────────────────────────────────────────────
            composable(Screen.Chat.route) {
                ConsultScreen(
                    consultViewModel = consultViewModel,  // ← 공유
                    onWriteQuestion  = {
                        navController.navigate(Screen.WriteQuestion.route)
                    },
                    onQuestionClick  = { },
                )
            }

            // ── 마이 ──────────────────────────────────────────────
            composable(Screen.My.route) {
                val authViewModel: AuthViewModel = viewModel()
                MyPageScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // ── 식물 상세 ─────────────────────────────────────────
            composable(
                route     = Screen.PlantDetail.route,
                arguments = listOf(
                    navArgument(Screen.PlantDetail.ARG_PLANT_ID) {
                        type = NavType.StringType
                    }
                ),
            ) { backStackEntry ->
                val plantId = backStackEntry.arguments
                    ?.getString(Screen.PlantDetail.ARG_PLANT_ID)
                    ?.toIntOrNull() ?: 0

                val context = androidx.compose.ui.platform.LocalContext.current
                val plantViewModel: PlantViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
                        context.applicationContext as android.app.Application
                    )
                )

                PlantDetailScreen(
                    plantId        = plantId,
                    plantViewModel = plantViewModel,
                    onBack         = { navController.popBackStack() },
                    onAiAnalysis   = { navController.navigate(Screen.AiAnalysis.route) },
                    onAskExpert    = { },
                )
            }

            // ── 식물 등록 ─────────────────────────────────────────
            composable(Screen.AddPlant.route) {
                AddPlantScreen(
                    onBack   = { navController.popBackStack() },
                    onSubmit = { navController.popBackStack() },
                )
            }

            // ── AI 분석 ───────────────────────────────────────────
            composable(Screen.AiAnalysis.route) {
                AiAnalysisScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            // ── 질문 작성 ─────────────────────────────────────────
            composable(Screen.WriteQuestion.route) {
                WriteQuestionScreen(
                    consultViewModel = consultViewModel,  // ← 공유
                    onBack           = { navController.popBackStack() },
                    onSubmit         = { navController.popBackStack() },
                )
            }
        }
    }
}