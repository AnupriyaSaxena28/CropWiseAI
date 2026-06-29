package com.cropwise.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level destinations, mirroring the website's sidebar. */
enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard", Icons.Filled.Dashboard),
    Chat("chat", "AI Advisor", Icons.Filled.Chat),
    Pest("pest", "Pest Diagnosis", Icons.Filled.BugReport),
    Crop("crop", "Crop Advisor", Icons.Filled.Agriculture),
    Weather("weather", "Weather", Icons.Filled.WbSunny),
    Market("market", "Market Prices", Icons.Filled.TrendingUp),
    Activity("activity", "Activity Log", Icons.Filled.ListAlt),
    Schemes("schemes", "Govt Schemes", Icons.Filled.AccountBalance),
    Profile("profile", "Profile", Icons.Filled.Person),
    Settings("settings", "Settings", Icons.Filled.Settings);
}
