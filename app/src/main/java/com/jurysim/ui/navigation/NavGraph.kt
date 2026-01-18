package com.jurysim.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jurysim.data.local.JurySimDatabase
import com.jurysim.data.repository.CaseHistoryRepository
import com.jurysim.data.repository.OllamaRepository
import com.jurysim.data.repository.PreferencesRepository
import com.jurysim.ui.screens.history.HistoryScreen
import com.jurysim.ui.screens.history.HistoryViewModel
import com.jurysim.ui.screens.home.HomeScreen
import com.jurysim.ui.screens.profile.JurorProfileScreen
import com.jurysim.ui.screens.profile.JurorProfileViewModel
import com.jurysim.ui.screens.settings.SettingsScreen
import com.jurysim.ui.screens.settings.SettingsViewModel
import com.jurysim.ui.screens.setup.ModelSelectionScreen
import com.jurysim.ui.screens.setup.ModelSelectionViewModel
import com.jurysim.ui.screens.setup.SetupScreen
import com.jurysim.ui.screens.setup.SetupViewModel
import com.jurysim.ui.screens.simulation.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Setup : Screen("setup")
    object ModelSelection : Screen("model_selection")
    object Settings : Screen("settings")
    object JurorProfile : Screen("juror_profile")
    object History : Screen("history")
    object Intro : Screen("intro")
    object VoirDire : Screen("voir_dire")
    object JurySelected : Screen("jury_selected")
    object JuryDismissed : Screen("jury_dismissed")
    object Trial : Screen("trial")
    object Deliberation : Screen("deliberation")
    object Verdict : Screen("verdict")
}

@Composable
fun JurySimNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    val context = LocalContext.current

    // Create repositories - in a real app, these would be injected via dependency injection
    val ollamaRepository = remember { OllamaRepository() }
    val preferencesRepository = remember { PreferencesRepository(context) }
    val database = remember { JurySimDatabase.getDatabase(context) }
    val caseHistoryRepository = remember { CaseHistoryRepository(database.caseDao()) }

    // Create ViewModels
    val setupViewModel = remember { SetupViewModel(ollamaRepository, preferencesRepository) }
    val modelSelectionViewModel = remember { ModelSelectionViewModel(ollamaRepository, preferencesRepository) }
    val settingsViewModel = remember { SettingsViewModel(ollamaRepository, preferencesRepository) }
    val jurorProfileViewModel = remember { JurorProfileViewModel(preferencesRepository) }
    val simulationViewModel = remember { SimulationViewModel(ollamaRepository, preferencesRepository, caseHistoryRepository) }
    val historyViewModel = remember { HistoryViewModel(caseHistoryRepository) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNewCase = {
                    navController.navigate(Screen.Setup.route)
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onJurorProfile = {
                    navController.navigate(Screen.JurorProfile.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.JurorProfile.route) {
            JurorProfileScreen(
                viewModel = jurorProfileViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Setup.route) {
            SetupScreen(
                viewModel = setupViewModel,
                onContinue = {
                    navController.navigate(Screen.ModelSelection.route)
                }
            )
        }

        composable(Screen.ModelSelection.route) {
            ModelSelectionScreen(
                viewModel = modelSelectionViewModel,
                onModelSelected = {
                    simulationViewModel.initialize()
                    navController.navigate(Screen.Intro.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Intro.route) {
            IntroScreen(
                viewModel = simulationViewModel,
                onContinue = {
                    simulationViewModel.startVoirDire()
                    navController.navigate(Screen.VoirDire.route)
                }
            )
        }

        composable(Screen.VoirDire.route) {
            VoirDireScreen(
                viewModel = simulationViewModel,
                onSelected = {
                    navController.navigate(Screen.JurySelected.route)
                },
                onDismissed = {
                    navController.navigate(Screen.JuryDismissed.route)
                }
            )
        }

        composable(Screen.JurySelected.route) {
            JurySelectionResultScreen(
                reason = simulationViewModel.state.value.juryAcceptanceReason,
                onContinue = {
                    // Generate AI jurors when jury is selected
                    simulationViewModel.generateAIJurors()
                    simulationViewModel.startOpeningStatements()
                    navController.navigate(Screen.Trial.route)
                }
            )
        }

        composable(Screen.JuryDismissed.route) {
            JuryDismissedScreen(
                reason = simulationViewModel.state.value.juryDismissalReason,
                onRetry = {
                    simulationViewModel.reset()
                    navController.navigate(Screen.Intro.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Trial.route) {
            TrialScreen(
                viewModel = simulationViewModel,
                onTrialComplete = {
                    // Store trial messages for evidence review and AI notes
                    simulationViewModel.storeTrialMessages()
                    navController.navigate(Screen.Deliberation.route)
                }
            )
        }

        composable(Screen.Deliberation.route) {
            DeliberationScreen(
                viewModel = simulationViewModel,
                onVerdictReady = {
                    navController.navigate(Screen.Verdict.route)
                }
            )
        }

        composable(Screen.Verdict.route) {
            VerdictScreen(
                viewModel = simulationViewModel,
                onNewSimulation = {
                    simulationViewModel.reset()
                    navController.navigate(Screen.Intro.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                },
                onMainMenu = {
                    simulationViewModel.goToMainMenu()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
