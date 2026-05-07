package com.jurysim.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jurysim.JurySimApp
import com.jurysim.data.local.JurySimDatabase
import com.jurysim.data.repository.CaseHistoryRepository
import com.jurysim.data.repository.PreferencesRepository
import com.jurysim.ui.ads.InterstitialAdController
import com.jurysim.ui.screens.history.HistoryScreen
import com.jurysim.ui.screens.history.HistoryViewModel
import com.jurysim.ui.screens.home.HomeScreen
import com.jurysim.ui.screens.onboarding.OnboardingScreen
import com.jurysim.ui.screens.onboarding.OnboardingViewModel
import com.jurysim.ui.screens.profile.JurorProfileScreen
import com.jurysim.ui.screens.profile.JurorProfileViewModel
import com.jurysim.ui.screens.settings.SettingsScreen
import com.jurysim.ui.screens.settings.SettingsViewModel
import com.jurysim.ui.screens.simulation.*

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
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
    startDestination: String = Screen.Onboarding.route,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as JurySimApp

    // Application-scoped singletons
    val modelManager = app.modelManager
    val modelDownloader = app.modelDownloader
    val llmEngineProvider = app.llmEngineProvider
    val llmEngine = remember { llmEngineProvider.get() }
    val preferencesRepository = remember { PreferencesRepository(context) }
    val database = remember { JurySimDatabase.getDatabase(context) }
    val caseHistoryRepository = remember { CaseHistoryRepository(database.caseDao()) }

    // ViewModels
    val settingsViewModel = remember {
        SettingsViewModel(modelManager, llmEngineProvider, preferencesRepository)
    }
    val jurorProfileViewModel = remember { JurorProfileViewModel(preferencesRepository) }
    val simulationViewModel = remember {
        SimulationViewModel(llmEngine, preferencesRepository, caseHistoryRepository)
    }
    val historyViewModel = remember { HistoryViewModel(caseHistoryRepository) }
    val interstitialAdController = remember { InterstitialAdController(context) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        interstitialAdController.preload()
    }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Screen.Intro.route -> interstitialAdController.showForNewCaseIfReady()
            Screen.VoirDire.route,
            Screen.JurySelected.route,
            Screen.Trial.route,
            Screen.Deliberation.route -> interstitialAdController.maybeShowDuringCase()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        OnboardingViewModel(
                            application = app,
                            modelManager = modelManager,
                            modelDownloader = modelDownloader,
                            llmEngineProvider = llmEngineProvider,
                            preferencesRepository = preferencesRepository
                        )
                    }
                }
            )
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onReady = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNewCase = {
                    simulationViewModel.initialize()
                    navController.navigate(Screen.Intro.route)
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
                },
                onModelDeleted = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
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
            if (!simulationViewModel.state.value.isJurySelected) {
                navController.navigate(Screen.VoirDire.route) {
                    popUpTo(Screen.VoirDire.route) { inclusive = false }
                }
                return@composable
            }

            JurySelectionResultScreen(
                reason = simulationViewModel.state.value.juryAcceptanceReason,
                onContinue = {
                    simulationViewModel.generateAIJurors()
                    simulationViewModel.startOpeningStatements()
                    navController.navigate(Screen.Trial.route)
                }
            )
        }

        composable(Screen.JuryDismissed.route) {
            if (!simulationViewModel.state.value.isJuryDismissed) {
                navController.navigate(Screen.VoirDire.route) {
                    popUpTo(Screen.VoirDire.route) { inclusive = false }
                }
                return@composable
            }

            JuryDismissedScreen(
                reason = simulationViewModel.state.value.juryDismissalReason,
                onRetry = {
                    simulationViewModel.goToMainMenu()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Trial.route) {
            if (!simulationViewModel.state.value.isJurySelected || simulationViewModel.state.value.isCaseClosed) {
                navController.navigate(
                    if (simulationViewModel.state.value.isJuryDismissed) Screen.JuryDismissed.route else Screen.VoirDire.route
                ) {
                    popUpTo(Screen.Intro.route) { inclusive = false }
                }
                return@composable
            }

            TrialScreen(
                viewModel = simulationViewModel,
                onTrialComplete = {
                    simulationViewModel.storeTrialMessages()
                    navController.navigate(Screen.Deliberation.route)
                }
            )
        }

        composable(Screen.Deliberation.route) {
            if (!simulationViewModel.state.value.isJurySelected || simulationViewModel.state.value.isCaseClosed) {
                navController.navigate(
                    if (simulationViewModel.state.value.isJuryDismissed) Screen.JuryDismissed.route else Screen.VoirDire.route
                ) {
                    popUpTo(Screen.Intro.route) { inclusive = false }
                }
                return@composable
            }

            DeliberationScreen(
                viewModel = simulationViewModel,
                onVerdictReady = {
                    navController.navigate(Screen.Verdict.route)
                }
            )
        }

        composable(Screen.Verdict.route) {
            if (!simulationViewModel.state.value.isJurySelected || simulationViewModel.state.value.isCaseClosed) {
                navController.navigate(
                    if (simulationViewModel.state.value.isJuryDismissed) Screen.JuryDismissed.route else Screen.VoirDire.route
                ) {
                    popUpTo(Screen.Intro.route) { inclusive = false }
                }
                return@composable
            }

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
