package com.oralvis.oralvisclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oralvis.oralvisclient.di.AppGraph
import com.oralvis.oralvisclient.di.OralVisViewModelFactory
import com.oralvis.oralvisclient.ui.components.FloatingBottomNav
import com.oralvis.oralvisclient.ui.navigation.NavRoutes
import com.oralvis.oralvisclient.ui.screens.AppointmentDetailsScreen
import com.oralvis.oralvisclient.ui.screens.AppointmentFormScreen
import com.oralvis.oralvisclient.ui.screens.CalendarScreen
import com.oralvis.oralvisclient.ui.screens.HomeScreen
import com.oralvis.oralvisclient.ui.screens.PatientDetailsScreen
import com.oralvis.oralvisclient.ui.screens.PatientsListScreen
import com.oralvis.oralvisclient.ui.screens.AddBillScreen
import com.oralvis.oralvisclient.ui.screens.AddFileScreen
import com.oralvis.oralvisclient.ui.screens.AddPrescriptionScreen
import com.oralvis.oralvisclient.ui.screens.LoginScreen
import com.oralvis.oralvisclient.ui.screens.SelectBookingScreen
import com.oralvis.oralvisclient.ui.screens.SummaryScreen
import com.oralvis.oralvisclient.ui.theme.OralVisOnPrimary
import com.oralvis.oralvisclient.ui.theme.OralVisPrimary
import com.oralvis.oralvisclient.ui.theme.OralvisClientTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            OralvisClientTheme {
                val factory = remember { OralVisViewModelFactory(AppGraph) }
                OralVisApp(viewModelFactory = factory)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var clinicId by remember { mutableStateOf<String?>(AppGraph.sessionManager().getClinicId()) }
    val scope = rememberCoroutineScope()
    val isLoggedIn = currentRoute != null && currentRoute != NavRoutes.Login
    LaunchedEffect(Unit) {
        if (currentRoute != null && currentRoute != NavRoutes.Login) {
            withContext(Dispatchers.IO) { AppGraph.initSession() }
            clinicId = AppGraph.sessionManager().getClinicId()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isLoggedIn) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = OralVisPrimary,
                            titleContentColor = OralVisOnPrimary,
                            navigationIconContentColor = OralVisOnPrimary
                        ),
                        windowInsets = WindowInsets.statusBars,
                        title = {},
                        navigationIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "O",
                                    color = OralVisOnPrimary,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    )

                    TabRow(
                        selectedTabIndex = topBarTabIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        containerColor = OralVisPrimary,
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty() && topBarTabIndex in tabPositions.indices) {
                                val pos = tabPositions[topBarTabIndex]
                                Box(
                                    Modifier
                                        .offset(x = pos.left, y = 42.dp)
                                        .width(pos.width)
                                        .height(2.dp)
                                        .background(OralVisOnPrimary)
                                )
                            }
                        },
                        divider = {}
                    ) {
                        val tabs = listOf("APPS", "SUMMARY")
                        tabs.forEachIndexed { index, title ->
                            val selected = index == topBarTabIndex
                            Tab(
                                selected = selected,
                                onClick = {
                                    topBarTabIndex = index
                                    when (index) {
                                        0 -> if (currentRoute != NavRoutes.Home) navController.navigate(NavRoutes.Home) { popUpTo(0) }
                                        1 -> if (currentRoute != NavRoutes.Summary) navController.navigate(NavRoutes.Summary) { popUpTo(0) }
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (selected) OralVisOnPrimary else OralVisOnPrimary.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isLoggedIn) {
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Login,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavRoutes.Login) {
                    LoginScreen(
                        viewModel = viewModel<com.oralvis.oralvisclient.ui.viewmodel.AuthViewModel>(factory = viewModelFactory),
                        onLoginSuccess = {
                            scope.launch {
                                withContext(Dispatchers.IO) { AppGraph.initSession() }
                                clinicId = AppGraph.sessionManager().getClinicId()
                                navController.navigate(NavRoutes.Home) {
                                    popUpTo(NavRoutes.Login) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable(NavRoutes.Home) {
                    HomeScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        appointmentViewModel = viewModel(factory = viewModelFactory),
                        clinicId = clinicId,
                        userName = AppGraph.sessionManager().getCurrentUser()?.name ?: "",
                        onNavigateToCalendar = { navController.navigate(NavRoutes.Calendar) },
                        onNavigateToPatients = { navController.navigate(NavRoutes.Patients) },
                        onNavigateToAppointmentDetails = { id -> navController.navigate(NavRoutes.appointmentDetails(id)) },
                        onNavigateToAddPatient = { navController.navigate(NavRoutes.AppointmentForm) }
                    )
                }
                composable(NavRoutes.Patients) {
                    PatientsListScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        clinicId = clinicId,
                        onPatientClick = { id -> navController.navigate(NavRoutes.patientDetails(id)) },
                        onMoreClick = { },
                        onAppointmentDetailsClick = { id -> navController.navigate(NavRoutes.appointmentDetails(id)) },
                        onAddAppointment = { navController.navigate(NavRoutes.AppointmentForm) },
                        onAddFile = { navController.navigate(NavRoutes.selectBooking("file")) },
                        onAddBill = { navController.navigate(NavRoutes.selectBooking("bill")) },
                        onAddPrescription = { navController.navigate(NavRoutes.selectBooking("prescription")) }
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
                composable(
                    route = "${NavRoutes.SelectBooking}/{action}",
                    arguments = listOf(navArgument("action") { type = NavType.StringType })
                ) { backStackEntry ->
                    val action = backStackEntry.arguments?.getString("action") ?: ""
                    SelectBookingScreen(
                        clinicId = clinicId,
                        actionTitle = when (action) {
                            "prescription" -> "Select appointment for prescription"
                            "file" -> "Select appointment for file"
                            "bill" -> "Select appointment for bill"
                            else -> "Select appointment"
                        },
                        onBookingSelected = { bookingId ->
                            when (action) {
                                "prescription" -> navController.navigate(NavRoutes.addPrescription(bookingId))
                                "file" -> navController.navigate(NavRoutes.addFile(bookingId))
                                "bill" -> navController.navigate(NavRoutes.addBill(bookingId))
                                else -> { }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${NavRoutes.AddPrescription}/{bookingId}",
                    arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    AddPrescriptionScreen(
                        bookingId = bookingId,
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${NavRoutes.AddFile}/{bookingId}",
                    arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    AddFileScreen(
                        bookingId = bookingId,
                        onFileUploaded = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${NavRoutes.AddBill}/{bookingId}",
                    arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    AddBillScreen(
                        bookingId = bookingId,
                        onMarkedPaid = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
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
