package com.oralvis.oralvisclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.components.FloatingBottomNav
import com.oralvis.oralvisclient.ui.components.OralVisTopBar
import com.oralvis.oralvisclient.ui.navigation.NavRoutes
import com.oralvis.oralvisclient.ui.screens.AppointmentDetailsScreen
import com.oralvis.oralvisclient.ui.screens.AppointmentFormScreen
import com.oralvis.oralvisclient.ui.screens.CalendarScreen
import com.oralvis.oralvisclient.ui.screens.HomeScreen
import com.oralvis.oralvisclient.ui.screens.PatientDetailsScreen
import com.oralvis.oralvisclient.ui.screens.PatientsListScreen
import com.oralvis.oralvisclient.ui.screens.SummaryScreen
import com.oralvis.oralvisclient.ui.theme.OralvisClientTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oralvis.oralvisclient.core.util.UiState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Summarize
import com.oralvis.oralvisclient.ui.components.BottomNavItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OralvisClientTheme {
                val factory = remember { OralVisViewModelFactory(AppGraph) }
                OralVisApp(viewModelFactory = factory)
            }
        }
    }
}

@Composable
private fun OralVisApp(viewModelFactory: OralVisViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var topBarTabIndex by remember { mutableStateOf(0) }

    val bottomNavItems = listOf(
        BottomNavItem(NavRoutes.Home, "Home", Icons.Outlined.Home, Icons.Filled.Home),
        BottomNavItem(NavRoutes.Calendar, "Calendar", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
        BottomNavItem(NavRoutes.Patients, "Patients", Icons.Outlined.Person, Icons.Filled.Person),
        BottomNavItem(NavRoutes.Summary, "Summary", Icons.Outlined.Summarize, Icons.Filled.Summarize)
    )

    val clinicId = remember { AppGraph.sessionManager().getClinicId() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            OralVisTopBar(
                showTabs = true,
                activeTabIndex = topBarTabIndex,
                tabTitles = listOf("APPS", "SUMMARY"),
                onTabSelected = { index ->
                    topBarTabIndex = index
                    when (index) {
                        0 -> if (currentRoute != NavRoutes.Home) navController.navigate(NavRoutes.Home) { popUpTo(0) }
                        1 -> if (currentRoute != NavRoutes.Summary) navController.navigate(NavRoutes.Summary) { popUpTo(0) }
                    }
                }
            )
        },
        bottomBar = {
            FloatingBottomNav(
                items = bottomNavItems,
                currentRoute = resolveBottomNavRoute(currentRoute),
                onItemClick = { route ->
                    when (route) {
                        NavRoutes.Home -> topBarTabIndex = 0
                        NavRoutes.Summary -> topBarTabIndex = 1
                        else -> { }
                    }
                    if (resolveBottomNavRoute(currentRoute) != route) {
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Home) { inclusive = (route == NavRoutes.Home); saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Home,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavRoutes.Home) {
                    HomeScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        appointmentViewModel = viewModel(factory = viewModelFactory),
                        clinicId = clinicId,
                        onNavigateToCalendar = { navController.navigate(NavRoutes.Calendar) },
                        onNavigateToPatients = { navController.navigate(NavRoutes.Patients) },
                        onNavigateToAppointmentDetails = { id -> navController.navigate(NavRoutes.appointmentDetails(id)) }
                    )
                }
                composable(NavRoutes.Patients) {
                    PatientsListScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        clinicId = clinicId,
                        onPatientClick = { id -> navController.navigate(NavRoutes.patientDetails(id)) },
                        onMoreClick = { }
                    )
                }
                composable(
                    route = "${NavRoutes.PatientDetails}/{patientId}",
                    arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
                    PatientDetailsScreen(
                        patientName = "Patient",
                        dateRange = "—",
                        doctorName = null,
                        appointmentDate = null,
                        timeSlot = null,
                        onBook = { },
                        onMoreClick = { }
                    )
                }
                composable(NavRoutes.Calendar) {
                    var selectedDate by remember { mutableStateOf<String?>(null) }
                    CalendarScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        clinicId = clinicId,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        onAppointmentClick = { id -> navController.navigate(NavRoutes.appointmentDetails(id)) }
                    )
                }
                composable(
                    route = "${NavRoutes.AppointmentDetails}/{bookingId}",
                    arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    AppointmentDetailsRoute(
                        bookingId = bookingId,
                        viewModelFactory = viewModelFactory,
                        onCheckIn = { },
                        onMoreClick = { },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.AppointmentForm) {
                    AppointmentFormScreen(
                        onSaveNext = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.Summary) {
                    SummaryScreen(
                        onCompleteProfile = { },
                        onSaveNext = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentDetailsRoute(
    bookingId: String,
    viewModelFactory: OralVisViewModelFactory,
    onCheckIn: () -> Unit,
    onMoreClick: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel =
        viewModel(factory = viewModelFactory)
    val state by viewModel.appointmentsState.collectAsState()
    val booking = (state as? UiState.Success)?.data?.find { it.id == bookingId }

    when {
        booking != null -> AppointmentDetailsScreen(
            patientName = booking.patientName,
            gender = null,
            dateRange = booking.appointmentDate,
            doctorName = null,
            timeSlot = booking.slotTime,
            onCheckIn = onCheckIn,
            onMoreClick = onMoreClick
        )
        state is UiState.Loading -> {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = com.oralvis.oralvisclient.ui.theme.OralVisPrimary)
            }
        }
        else -> AppointmentDetailsScreen(
            patientName = "—",
            gender = null,
            dateRange = "—",
            doctorName = null,
            timeSlot = null,
            onCheckIn = onCheckIn,
            onMoreClick = onMoreClick
        )
    }
}

private fun resolveBottomNavRoute(route: String?): String? {
    if (route == null) return null
    return when {
        route == NavRoutes.Home -> NavRoutes.Home
        route.startsWith(NavRoutes.Calendar) -> NavRoutes.Calendar
        route == NavRoutes.Patients || route.startsWith("${NavRoutes.PatientDetails}/") -> NavRoutes.Patients
        route == NavRoutes.Summary -> NavRoutes.Summary
        route.startsWith("${NavRoutes.AppointmentDetails}/") -> null
        route == NavRoutes.AppointmentForm -> null
        else -> null
    }
}
