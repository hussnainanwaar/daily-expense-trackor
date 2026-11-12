package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AddExpenseSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    ANALYTICS("Analytics", Icons.Filled.Insights, Icons.Outlined.Insights, "tab_analytics"),
    BUDGETS("Budgets", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "tab_budgets"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showAddSheet by remember { mutableStateOf(false) }
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    // Back handling: return to HOME tab if on another tab
    BackHandler(enabled = currentTab != AppTab.HOME) {
        currentTab = AppTab.HOME
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        bottomBar = {
            NavigationBar(
                containerColor = DarkCardSurface,
                contentColor = TextWhite,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_navigation_bar")
            ) {
                for (tab in AppTab.entries) {
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = EmeraldLight,
                            indicatorColor = EmeraldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                when (tab) {
                    AppTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenAddExpense = { showAddSheet = true }
                    )
                    AppTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                    AppTab.BUDGETS -> BudgetsScreen(viewModel = viewModel)
                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        if (showAddSheet) {
            AddExpenseSheet(
                defaultCurrency = userSettings.defaultCurrency,
                onDismiss = { showAddSheet = false },
                onSave = { amount, currency, category, dateMillis, notes, isRecurring, frequency ->
                    viewModel.addManualExpense(
                        amount = amount,
                        currency = currency,
                        category = category,
                        dateMillis = dateMillis,
                        notes = notes,
                        isRecurring = isRecurring,
                        recurringFrequency = frequency
                    )
                }
            )
        }
    }
}
