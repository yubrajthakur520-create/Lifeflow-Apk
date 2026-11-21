package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.Habit
import com.example.ui.components.HabitEditorBottomSheet
import com.example.ui.components.HabitHistoryDialog
import com.example.ui.components.LifeflowConfirmDialog
import com.example.ui.components.ProfileEditorDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LifeflowTheme

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_HABITS = "habits"
const val ROUTE_CALENDAR = "calendar"
const val ROUTE_ANALYTICS = "analytics"
const val ROUTE_SETTINGS = "settings"

private enum class LifeflowDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(ROUTE_DASHBOARD, "Dashboard", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    HABITS(ROUTE_HABITS, "My Habits", Icons.Rounded.CheckCircle, Icons.Outlined.CheckCircle),
    CALENDAR(ROUTE_CALENDAR, "Calendar", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    ANALYTICS(ROUTE_ANALYTICS, "Analytics", Icons.Rounded.Analytics, Icons.Outlined.Analytics),
    SETTINGS(ROUTE_SETTINGS, "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
}

@Composable
fun LifeflowApp(viewModel: LifeflowViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showHabitEditor by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var habitPendingDelete by remember { mutableStateOf<Habit?>(null) }
    var habitForHistory by remember { mutableStateOf<Habit?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var sidebarExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ExportReady -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "Lifeflow Habit Export")
                        putExtra(Intent.EXTRA_TEXT, event.jsonPayload)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Lifeflow Data"))
                }
            }
        }
    }

    LifeflowTheme(themeMode = state.preferences.themeMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

        val navigateTo: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 720.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            LifeflowDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (currentRoute == ROUTE_DASHBOARD || currentRoute == ROUTE_HABITS) {
                        if (isExpandedScreen) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    editingHabit = null
                                    showHabitEditor = true
                                },
                                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Habit") },
                                text = { Text("Add Habit") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("add_habit_fab")
                            )
                        } else {
                            FloatingActionButton(
                                onClick = {
                                    editingHabit = null
                                    showHabitEditor = true
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("add_habit_fab")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add Habit")
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            if (sidebarExpanded) {
                                Column(
                                    modifier = Modifier
                                        .width(236.dp)
                                        .fillMaxHeight()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "LIFEFLOW",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Small Habits. Better Days.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { sidebarExpanded = false }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.MenuOpen,
                                                contentDescription = "Collapse sidebar"
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    LifeflowDestination.entries.forEach { dest ->
                                        val selected = currentRoute == dest.route
                                        NavigationDrawerItem(
                                            selected = selected,
                                            onClick = { navigateTo(dest.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = dest.label
                                                )
                                            },
                                            label = { Text(dest.label) },
                                            modifier = Modifier.testTag("sidebar_${dest.route}")
                                        )
                                    }
                                }
                            } else {
                                NavigationRail(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    IconButton(
                                        onClick = { sidebarExpanded = true },
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Menu, contentDescription = "Expand sidebar")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LifeflowDestination.entries.forEach { dest ->
                                        val selected = currentRoute == dest.route
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = { navigateTo(dest.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = dest.label
                                                )
                                            },
                                            label = { Text(dest.label) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_DASHBOARD,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    state = state,
                                    onToggleCompletion = { habit, done ->
                                        viewModel.toggleHabitCompletion(habit.id, state.today, done)
                                    },
                                    onEditHabit = { habit ->
                                        editingHabit = habit
                                        showHabitEditor = true
                                    },
                                    onDeleteHabit = { habit -> habitPendingDelete = habit },
                                    onToggleActive = { habit -> viewModel.toggleHabitActive(habit) },
                                    onSelectRoutineFilter = { viewModel.selectRoutineFilter(it) },
                                    onAddHabitClick = {
                                        editingHabit = null
                                        showHabitEditor = true
                                    },
                                    onOpenSettings = { navigateTo(ROUTE_SETTINGS) }
                                )
                            }

                            composable(ROUTE_HABITS) {
                                BackHandler { navigateTo(ROUTE_DASHBOARD) }
                                HabitsScreen(
                                    state = state,
                                    onSearchChange = { viewModel.updateSearchQuery(it) },
                                    onCategorySelect = { viewModel.selectCategoryFilter(it) },
                                    onStatusSelect = { viewModel.selectStatusFilter(it) },
                                    onAddHabit = {
                                        editingHabit = null
                                        showHabitEditor = true
                                    },
                                    onEditHabit = { habit ->
                                        editingHabit = habit
                                        showHabitEditor = true
                                    },
                                    onDeleteHabit = { habit -> habitPendingDelete = habit },
                                    onToggleActive = { habit -> viewModel.toggleHabitActive(habit) },
                                    onOpenHistory = { habit -> habitForHistory = habit },
                                    onRestoreStarters = { showRestoreConfirm = true }
                                )
                            }

                            composable(ROUTE_CALENDAR) {
                                BackHandler { navigateTo(ROUTE_DASHBOARD) }
                                CalendarScreen(
                                    state = state,
                                    onSelectDate = { viewModel.selectCalendarDate(it) },
                                    onNavigateMonth = { viewModel.navigateMonth(it) },
                                    onJumpToToday = { viewModel.jumpCalendarToToday() },
                                    onToggleCompletion = { habit, date, done ->
                                        viewModel.toggleHabitCompletion(habit.id, date, done)
                                    },
                                    onEditHabit = { habit ->
                                        editingHabit = habit
                                        showHabitEditor = true
                                    },
                                    onDeleteHabit = { habit -> habitPendingDelete = habit },
                                    onToggleActive = { habit -> viewModel.toggleHabitActive(habit) }
                                )
                            }

                            composable(ROUTE_ANALYTICS) {
                                BackHandler { navigateTo(ROUTE_DASHBOARD) }
                                AnalyticsScreen(state = state)
                            }

                            composable(ROUTE_SETTINGS) {
                                BackHandler { navigateTo(ROUTE_DASHBOARD) }
                                SettingsScreen(
                                    state = state,
                                    onEditProfile = { showProfileDialog = true },
                                    onThemeModeChange = { viewModel.setThemeMode(it) },
                                    onWeekStartChange = { viewModel.setWeekStartDay(it) },
                                    onUpdateNotifications = { viewModel.updateNotificationPreferences(it) },
                                    onSendTestNotification = { viewModel.sendTestNotification() },
                                    onExportData = { viewModel.exportData() },
                                    onRestoreStarters = { showRestoreConfirm = true },
                                    onResetLocalData = { showResetConfirm = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showHabitEditor) {
            HabitEditorBottomSheet(
                editingHabit = editingHabit,
                onSave = { habit, isEdit -> viewModel.saveHabit(habit, isEdit) },
                onDismiss = {
                    showHabitEditor = false
                    editingHabit = null
                }
            )
        }

        habitForHistory?.let { habit ->
            HabitHistoryDialog(
                habit = habit,
                completions = state.completions,
                today = state.today,
                onToggleDate = { date, done ->
                    viewModel.toggleHabitCompletion(habit.id, date, done)
                },
                onDismiss = { habitForHistory = null }
            )
        }

        if (showProfileDialog) {
            ProfileEditorDialog(
                currentProfile = state.preferences.profile,
                onSave = { name, email, uri -> viewModel.updateProfile(name, email, uri) },
                onDismiss = { showProfileDialog = false }
            )
        }

        habitPendingDelete?.let { habit ->
            LifeflowConfirmDialog(
                title = "Delete Habit?",
                message = "Are you sure you want to delete \"${habit.title}\"? All completion records for this habit will also be removed.",
                confirmLabel = "Delete",
                isDestructive = true,
                onConfirm = { viewModel.deleteHabit(habit) },
                onDismiss = { habitPendingDelete = null }
            )
        }

        if (showRestoreConfirm) {
            LifeflowConfirmDialog(
                title = "Restore Starter Routines?",
                message = "This will add any missing Morning, Afternoon, Evening, and Night starter activities without deleting your existing habits.",
                confirmLabel = "Restore",
                onConfirm = { viewModel.restoreStarterRoutines() },
                onDismiss = { showRestoreConfirm = false }
            )
        }

        if (showResetConfirm) {
            LifeflowConfirmDialog(
                title = "Reset Local Data?",
                message = "This will clear your completion history, reset your profile, and restore default starter routines.",
                confirmLabel = "Reset Data",
                isDestructive = true,
                onConfirm = { viewModel.resetLocalData() },
                onDismiss = { showResetConfirm = false }
            )
        }
    }
}
