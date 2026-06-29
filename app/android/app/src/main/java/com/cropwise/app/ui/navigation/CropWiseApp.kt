package com.cropwise.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import androidx.navigation.NavGraphBuilder
import com.cropwise.app.MainViewModel
import com.cropwise.app.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropWiseApp(vm: MainViewModel) {
    val loggedIn by vm.loggedIn.collectAsStateWithLifecycle()
    val nav = rememberNavController()

    if (!loggedIn) {
        LoginScreen(vm)
        return
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Dest.Dashboard.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "🌾 CropWise AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Dest.entries.forEach { dest ->
                    NavigationDrawerItem(
                        icon = { Icon(dest.icon, null) },
                        label = { Text(dest.label) },
                        selected = current == dest.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate(dest.route) {
                                popUpTo(Dest.Dashboard.route)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        val title = Dest.entries.find { it.route == current }?.label ?: "CropWise AI"
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, "Menu")
                        }
                    },
                )
            },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Dest.Dashboard.route,
                modifier = Modifier.padding(padding),
            ) {
                screen(Dest.Dashboard.route) { DashboardScreen(vm, nav) }
                screen(Dest.Chat.route) { ChatScreen(vm) }
                screen(Dest.Pest.route) { PestDiagnosisScreen(vm) }
                screen(Dest.Crop.route) { CropAdvisorScreen(vm) }
                screen(Dest.Weather.route) { WeatherScreen(vm) }
                screen(Dest.Market.route) { MarketScreen(vm) }
                screen(Dest.Activity.route) { ActivityLogScreen(vm) }
                screen(Dest.Schemes.route) { SchemesScreen(vm) }
                screen(Dest.Profile.route) { ProfileScreen(vm) }
                screen(Dest.Settings.route) { SettingsScreen(vm) }
            }
        }
    }
}

private fun NavGraphBuilder.screen(route: String, content: @Composable () -> Unit) =
    composable(route) { content() }
