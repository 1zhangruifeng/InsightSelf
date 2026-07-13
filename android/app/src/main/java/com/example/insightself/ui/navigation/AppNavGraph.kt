package com.example.insightself.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.insightself.R
import com.example.insightself.ui.components.AppBottomNavItem
import com.example.insightself.ui.components.AppBottomNavigation
import com.example.insightself.ui.components.AppPasswordField
import com.example.insightself.ui.components.AppTextField
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.EmptyView
import com.example.insightself.ui.components.InfoRow
import com.example.insightself.ui.components.InsightBadge
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.ScoreBar
import com.example.insightself.ui.components.ScoreTone
import com.example.insightself.ui.components.SecondaryActionButton
import com.example.insightself.ui.components.SectionTitle
import com.example.insightself.ui.theme.InsightBackground
import com.example.insightself.ui.theme.InsightBackground2
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.ui.screens.auth.LoginScreen
import com.example.insightself.ui.screens.auth.RegisterScreen
import com.example.insightself.ui.screens.auth.StartupScreen
import com.example.insightself.ui.screens.assessment.AssessmentHubScreen
import com.example.insightself.ui.screens.assessment.AssessmentQuestionScreen
import com.example.insightself.ui.screens.assessment.AssessmentResultScreen
import com.example.insightself.ui.screens.bazi.BaziScreen
import com.example.insightself.ui.screens.home.HomeDashboardScreen
import com.example.insightself.ui.screens.profile.OnboardingProfileScreen
import com.example.insightself.ui.screens.profile.ProfileScreen
import com.example.insightself.ui.screens.report.AiReportScreen
import com.example.insightself.ui.screens.zodiac.MatchScreen
import com.example.insightself.ui.screens.zodiac.ZodiacScreen
import com.example.insightself.viewmodel.AssessmentViewModel
import com.example.insightself.viewmodel.AiReportViewModel
import com.example.insightself.viewmodel.AuthDestination
import com.example.insightself.viewmodel.AuthViewModel
import com.example.insightself.viewmodel.BaziViewModel
import com.example.insightself.viewmodel.DashboardViewModel
import com.example.insightself.viewmodel.LanguageViewModel
import com.example.insightself.viewmodel.ProfileViewModel
import com.example.insightself.viewmodel.ZodiacViewModel
import kotlinx.coroutines.launch
import com.example.insightself.data.local.LanguageManager

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val appContext = LocalContext.current.applicationContext
    val sessionDataStore = remember { UserSessionDataStore(appContext) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(sessionDataStore) { languageCode ->
            LanguageManager.saveLanguage(appContext, languageCode)
        }
    )
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(sessionDataStore))
    val zodiacViewModel: ZodiacViewModel = viewModel(factory = ZodiacViewModel.factory(sessionDataStore))
    val assessmentViewModel: AssessmentViewModel = viewModel(factory = AssessmentViewModel.factory(sessionDataStore))
    val aiReportViewModel: AiReportViewModel = viewModel(factory = AiReportViewModel.factory(sessionDataStore))
    val rootScope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = Routes.Startup
    ) {
        composable(Routes.Startup) {
            StartupScreen(
                authViewModel = authViewModel,
                onRoute = { destination ->
                    navController.navigateToAuthDestination(destination, popFrom = Routes.Startup)
                }
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginComplete = { destination ->
                    navController.navigateToAuthDestination(destination, popFrom = Routes.Login)
                },
                onRegisterClick = { navController.navigate(Routes.Register) }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterComplete = { destination ->
                    navController.navigateToAuthDestination(destination, popFrom = Routes.Register)
                },
                onLoginClick = { navController.navigate(Routes.Login) }
            )
        }
        composable(Routes.OnboardingProfile) {
            val scope = rememberCoroutineScope()
            com.example.insightself.data.auth.SessionAuth.init(appContext)
            OnboardingProfileScreen(
                profileViewModel = profileViewModel,
                onComplete = {
                    scope.launch {
                        com.example.insightself.data.auth.SessionAuth.hydrateAccessToken()
                        navController.navigate(Routes.Main) {
                            popUpTo(Routes.OnboardingProfile) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onBackToLogin = {
                    scope.launch {
                        com.example.insightself.data.auth.SessionAuth.clearSession()
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Startup) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.Main) {
            MainScreen(
                rootNavController = navController,
                sessionDataStore = sessionDataStore,
                authViewModel = authViewModel,
                zodiacViewModel = zodiacViewModel,
                assessmentViewModel = assessmentViewModel,
                aiReportViewModel = aiReportViewModel
            )
        }
        composable(Routes.Match) {
            ScreenBackground {
                MatchScreen(
                    zodiacViewModel = zodiacViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.AssessmentQuestions) { entry ->
            val type = entry.arguments?.getString("type").orEmpty()
            ScreenBackground {
                AssessmentQuestionScreen(
                    type = type.ifBlank { "BFI10" },
                    assessmentViewModel = assessmentViewModel,
                    onBack = { navController.popBackStack() },
                    onSubmitted = { resultId ->
                        navController.navigate(Routes.assessmentResult(resultId))
                    }
                )
            }
        }
        composable(Routes.AssessmentResult) { entry ->
            val resultId = entry.arguments?.getString("resultId")?.toLongOrNull()
            if (resultId == null) {
                SimplePage(title = "Assessment Result", subtitle = "Result not found")
            } else {
                ScreenBackground {
                    AssessmentResultScreen(
                        resultId = resultId,
                        assessmentViewModel = assessmentViewModel,
                        onBackToAssessments = {
                            navController.popBackStack(Routes.Main, inclusive = false)
                        },
                        onBackHome = {
                            navController.navigate(Routes.Main) {
                                popUpTo(Routes.Main) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
        composable(Routes.Report) {
            ScreenBackground {
                AiReportScreen(
                    aiReportViewModel = aiReportViewModel,
                    onSessionExpired = {
                        rootScope.launch {
                            com.example.insightself.data.auth.SessionAuth.clearSession()
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Main) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun NavHostController.navigateToAuthDestination(
    destination: AuthDestination,
    popFrom: String
) {
    val route = when (destination) {
        AuthDestination.Login -> Routes.Login
        AuthDestination.Onboarding -> Routes.OnboardingProfile
        AuthDestination.Main -> Routes.Main
    }
    navigate(route) {
        popUpTo(popFrom) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun MainScreen(
    rootNavController: NavController,
    sessionDataStore: UserSessionDataStore,
    authViewModel: AuthViewModel,
    zodiacViewModel: ZodiacViewModel,
    assessmentViewModel: AssessmentViewModel,
    aiReportViewModel: AiReportViewModel
) {
    val tabNavController = rememberNavController()
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(sessionDataStore))
    val baziViewModel: BaziViewModel = viewModel(factory = BaziViewModel.factory(sessionDataStore))
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext
    var currentLanguage by remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        com.example.insightself.data.auth.SessionAuth.init(appContext)
        com.example.insightself.data.auth.SessionAuth.hydrateAccessToken()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    com.example.insightself.data.auth.SessionAuth.hydrateAccessToken()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 监听语言变化
    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(appContext).collect { lang ->
            currentLanguage = lang
        }
    }

    val logout: () -> Unit = {
        scope.launch {
            com.example.insightself.data.auth.SessionAuth.clearSession()
            rootNavController.navigate(Routes.Login) {
                popUpTo(Routes.Main) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val tabs = listOf(
        AppBottomNavItem(Routes.Home, stringResource(R.string.nav_home), "H"),
        AppBottomNavItem(Routes.Bazi, stringResource(R.string.nav_bazi), "B"),
        AppBottomNavItem(Routes.Zodiac, stringResource(R.string.nav_zodiac), "Z"),
        AppBottomNavItem(Routes.Assessments, stringResource(R.string.nav_tests), "T"),
        AppBottomNavItem(Routes.Profile, stringResource(R.string.nav_profile), "P")
    )

    ScreenBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val selectedRoute = navBackStackEntry?.destination?.route
                AppBottomNavigation(
                    items = tabs,
                    selectedRoute = selectedRoute,
                    onItemClick = { tab ->
                        println("=== DEBUG: Tab clicked: ${tab.route}")
                        tabNavController.navigate(tab.route) {
                            popUpTo(tabNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = tabNavController,
                startDestination = Routes.Home,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Routes.Home) {
                    HomeDashboardScreen(
                        dashboardViewModel = dashboardViewModel,
                        zodiacViewModel = zodiacViewModel,
                        onMissingSession = logout,
                        onProfileMissing = {
                            rootNavController.navigate(Routes.OnboardingProfile) {
                                popUpTo(Routes.Main) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onOpenReport = {
                            tabNavController.navigate(Routes.Report) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToAssessments = {
                            // 跳转到测评 Tab
                            tabNavController.navigate(Routes.Assessments) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToAssessmentResult = { resultId ->
                            // 跳转到测评结果详情页
                            rootNavController.navigate(Routes.assessmentResult(resultId))
                        }
                    )
                }
                composable(Routes.Bazi) {
                    BaziScreen(baziViewModel = baziViewModel)
                }
                composable(Routes.Zodiac) {
                    ZodiacScreen(
                        zodiacViewModel = zodiacViewModel,
                        onOpenMatch = { rootNavController.navigate(Routes.Match) }
                    )
                }
                composable(Routes.Assessments) {
                    AssessmentHubScreen(
                        assessmentViewModel = assessmentViewModel,
                        onOpenQuestions = { type ->
                            rootNavController.navigate(Routes.assessmentQuestions(type))
                        },
                        onOpenResult = { resultId ->
                            rootNavController.navigate(Routes.assessmentResult(resultId))
                        }
                    )
                }
                composable(Routes.Report) {
                    AiReportScreen(
                        aiReportViewModel = aiReportViewModel,
                        onSessionExpired = logout
                    )
                }
                composable(Routes.Profile) {
                    println("=== DEBUG: Profile composable reached")
                    val languageViewModel = remember(appContext) {
                        LanguageViewModel(
                            languageFlow = LanguageManager.getLanguageFlow(appContext),
                            saveLanguagePreference = { languageCode ->
                                LanguageManager.saveLanguage(appContext, languageCode)
                            }
                        )
                    }
                    ProfileScreen(
                        languageViewModel = languageViewModel,
                        authViewModel = authViewModel,
                        onDemoLoaded = {
                            rootNavController.navigate(Routes.Main) {
                                popUpTo(Routes.Main) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onLogout = logout
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginDesignPlaceholder(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = InsightSpacing.ScreenHorizontal, vertical = 34.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionTitle(title = stringResource(R.string.app_name), subtitle = stringResource(R.string.profile_subtitle))
            InsightCard(title = stringResource(R.string.login), subtitle = stringResource(R.string.existing_demo_account)) {
                AppTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.username),
                    placeholder = stringResource(R.string.letters_and_numbers)
                )
                AppPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.password),
                    placeholder = "pass1234"
                )
                PrimaryActionButton(text = stringResource(R.string.login), onClick = onLogin)
                SecondaryActionButton(text = stringResource(R.string.no_account), onClick = onRegister)
            }
            InsightCard(title = "Design check", badge = "Mockup") {
                Text(
                    text = "Rounded translucent cards, pill controls, soft shadows, and blue gradient actions match the prototype language.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            }
        }
    }
}

@Composable
private fun RegisterDesignPlaceholder(onContinue: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(InsightSpacing.ScreenHorizontal)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppTopBar(title = stringResource(R.string.register), subtitle = stringResource(R.string.new_demo_account))
            InsightCard {
                AppTextField(value = username, onValueChange = { username = it }, label = stringResource(R.string.username), placeholder = "demo")
                AppPasswordField(value = password, onValueChange = { password = it }, label = stringResource(R.string.password), placeholder = "pass1234")
                PrimaryActionButton(text = stringResource(R.string.register_and_fill), onClick = onContinue)
            }
        }
    }
}

@Composable
private fun OnboardingDesignPlaceholder(onContinue: () -> Unit) {
    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(InsightSpacing.ScreenHorizontal)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppTopBar(title = stringResource(R.string.create_your_profile), subtitle = stringResource(R.string.profile_subtitle))
            InsightCard {
                AppTextField(value = "", onValueChange = {}, label = stringResource(R.string.nickname), placeholder = stringResource(R.string.nickname_placeholder))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.birth_date),
                        placeholder = stringResource(R.string.birth_date_placeholder)
                    )
                    AppTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.birth_time),
                        placeholder = stringResource(R.string.birth_time_placeholder)
                    )
                }
                AppTextField(value = "", onValueChange = {}, label = stringResource(R.string.birthplace), placeholder = stringResource(R.string.birthplace_placeholder))
                Text(text = stringResource(R.string.preference_lens), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InsightBadge(stringResource(R.string.preference_eastern))
                    InsightBadge(stringResource(R.string.preference_western))
                    InsightBadge(stringResource(R.string.preference_balanced))
                }
            }
            PrimaryActionButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
        }
    }
}

@Composable
private fun DashboardDesignPlaceholder(onOpenReport: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(title = stringResource(R.string.home_greeting, "Harper"), subtitle = stringResource(R.string.integrated_insight))
        InsightCard(title = stringResource(R.string.today_astrology), badge = "Daily") {
            ScoreBar(label = stringResource(R.string.emotion), score = 78, tone = ScoreTone.Success, showValue = false)
            ScoreBar(label = stringResource(R.string.communication), score = 63, showValue = false)
            ScoreBar(label = stringResource(R.string.action), score = 48, tone = ScoreTone.Warning, showValue = false)
        }
        InsightCard(title = stringResource(R.string.bazi_highlights), badge = "Explainable") {
            InfoRow(label = "Day Master", value = "Jia Wood")
            InfoRow(label = "Strong", value = "Wood · Water")
            InfoRow(label = "Balance", value = "Add Metal routines")
        }
        InsightCard(title = stringResource(R.string.latest_assessments), badge = "IPIP") {
            ScoreBar(label = "Openness", score = 68)
            ScoreBar(label = "Conscientious", score = 74, tone = ScoreTone.Success)
            ScoreBar(label = "Extraversion", score = 42, tone = ScoreTone.Warning)
        }
        InsightCard(title = stringResource(R.string.ai_report_title), badge = "Template") {
            Text(
                text = "Today is best for planning and gentle communication. Choose one key goal, then align actions with routines.",
                color = InsightText,
                style = MaterialTheme.typography.bodyLarge
            )
            PrimaryActionButton(text = stringResource(R.string.open_report), onClick = onOpenReport)
        }
    }
}

@Composable
private fun ScoreDesignPlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(title = title, subtitle = subtitle, actionText = "...")
        InsightCard(title = "Score placeholder", badge = "Explainable") {
            ScoreBar(label = "Wood", score = 68)
            ScoreBar(label = "Fire", score = 54, tone = ScoreTone.Success)
            ScoreBar(label = "Earth", score = 46, tone = ScoreTone.Warning)
            ScoreBar(label = "Metal", score = 31)
            ScoreBar(label = "Water", score = 52)
        }
        InsightCard(title = "Recent suggestion") {
            Text(
                text = "Use a calm routine to turn reflection into one small, observable action.",
                color = InsightMuted,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ZodiacDesignPlaceholder(onOpenMatch: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(title = stringResource(R.string.zodiac_title), subtitle = stringResource(R.string.daily_board))
        InsightCard(title = stringResource(R.string.today_astrology), badge = "Leo") {
            ScoreBar(label = stringResource(R.string.emotion), score = 72)
            ScoreBar(label = stringResource(R.string.communication), score = 58, tone = ScoreTone.Success)
            ScoreBar(label = stringResource(R.string.action), score = 44, tone = ScoreTone.Warning)
            PrimaryActionButton(text = stringResource(R.string.open_match), onClick = onOpenMatch)
        }
    }
}

@Composable
private fun AssessmentsDesignPlaceholder(
    onOpenQuestions: () -> Unit,
    onOpenResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(title = stringResource(R.string.assessments_title), subtitle = stringResource(R.string.assessments_subtitle))
        InsightCard(title = "IPIP Big Five-20", subtitle = "Public-domain style reflection", badge = "Ready") {
            PrimaryActionButton(text = stringResource(R.string.start), onClick = onOpenQuestions)
            SecondaryActionButton(text = stringResource(R.string.view), onClick = onOpenResult)
        }
        EmptyView(
            message = "More assessment cards will reuse this empty-state style.",
            actionLabel = stringResource(R.string.start),
            onAction = onOpenQuestions,
            modifier = Modifier.height(220.dp)
        )
    }
}

@Composable
private fun ProfileDesignPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(title = stringResource(R.string.nav_profile), subtitle = "Single source of truth", actionText = stringResource(R.string.edit))
        InsightCard {
            InfoRow(label = stringResource(R.string.nickname), value = "Harper")
            InfoRow(label = stringResource(R.string.birth_date), value = "2001-08-18")
            InfoRow(label = stringResource(R.string.birthplace), value = "Hong Kong")
            InfoRow(label = stringResource(R.string.preference_lens), value = stringResource(R.string.preference_balanced))
        }
    }
}

@Composable
private fun SimplePage(title: String, subtitle: String? = null) {
    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(InsightSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            AppTopBar(title = title, subtitle = subtitle)
            InsightCard(title = "Visual placeholder") {
                Text(
                    text = "This screen is ready for business logic while preserving the shared InsightSelf visual system.",
                    color = InsightMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ScreenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(InsightBackground, InsightBackground2)))
    ) {
        content()
    }
}
