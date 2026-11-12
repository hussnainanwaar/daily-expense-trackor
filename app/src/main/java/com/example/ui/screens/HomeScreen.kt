package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExpenseEntity
import com.example.model.ExpenseCategory
import com.example.ui.ExpensePeriod
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CircularBudgetRing
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.QuickInputBar
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.SoftBlueLight
import com.example.ui.theme.SoftBlueSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onOpenAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val dailyBudget by viewModel.dailyBudget.collectAsStateWithLifecycle()
    val todayRemaining by viewModel.todayRemaining.collectAsStateWithLifecycle()
    val todayBudgetRatio by viewModel.todayBudgetRatio.collectAsStateWithLifecycle()

    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    val lastMonthTotal by viewModel.lastMonthTotal.collectAsStateWithLifecycle()
    val monthComparison by viewModel.monthComparison.collectAsStateWithLifecycle()

    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val naturalLanguageInput by viewModel.naturalLanguageInput.collectAsStateWithLifecycle()
    val parsedPreview by viewModel.parsedPreview.collectAsStateWithLifecycle()
    val activeFilterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val categoryProgressList by viewModel.categoryProgressList.collectAsStateWithLifecycle()

    val currency = userSettings.defaultCurrency
    val overallBudget = userSettings.monthlyOverallBudget

    var showEditDailyBudgetDialog by remember { mutableStateOf(false) }

    // Check for high-alert categories
    val alertCategory = categoryProgressList.firstOrNull { it.isExceeded || it.isWarning }

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: App Title, Date, & Currency Tag
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SpendWise",
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("app_title")
                        )
                        Text(
                            text = currentDateStr,
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Currency: $currency",
                            color = EmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- 1. DAILY BUDGET CARD (Requested Feature) ---
            item {
                val isDailyExceeded = todayTotal > dailyBudget
                val dailyProgressRatio = (todayBudgetRatio).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                if (isDailyExceeded) {
                                    listOf(DarkCardSurface, DangerCoral.copy(alpha = 0.15f))
                                } else {
                                    listOf(DarkCardSurface, DarkElevatedSurface)
                                }
                            )
                        )
                        .border(
                            1.dp,
                            if (isDailyExceeded) DangerCoral.copy(alpha = 0.5f) else EmeraldPrimary.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(18.dp)
                        .testTag("daily_budget_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDailyExceeded) DangerCoral.copy(alpha = 0.2f) else EmeraldDark.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = "Daily Budget",
                                        tint = if (isDailyExceeded) DangerCoral else EmeraldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Daily Budget",
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isDailyExceeded) "Exceeded by $currency${String.format(Locale.US, "%.2f", todayTotal - dailyBudget)}" else "Spending limit for today",
                                        color = if (isDailyExceeded) DangerCoral else TextSubtle,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDailyExceeded) DangerCoral.copy(alpha = 0.2f) else EmeraldDark.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isDailyExceeded) "Over Limit" else "$currency${String.format(Locale.US, "%.2f", todayRemaining)} left",
                                        color = if (isDailyExceeded) DangerCoral else EmeraldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { showEditDailyBudgetDialog = true },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("edit_daily_budget_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit daily budget",
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$currency${String.format(Locale.US, "%.2f", todayTotal)}",
                                color = if (isDailyExceeded) DangerCoral else EmeraldLight,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("today_spend_text")
                            )

                            Text(
                                text = "Daily Target: $currency${String.format(Locale.US, "%.0f", dailyBudget)}",
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { dailyProgressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isDailyExceeded) DangerCoral else EmeraldPrimary,
                            trackColor = DarkElevatedSurface
                        )
                    }
                }
            }

            // --- 2. MONTHLY SPENDING HERO & COMPARISON CARD ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DarkCardSurface, DarkElevatedSurface)
                            )
                        )
                        .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                        .testTag("spending_hero_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "This Month (${viewModel.thisMonthName})",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$currency${String.format(Locale.US, "%.2f", monthTotal)}",
                                    color = TextWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Comparison Badge vs Last Month
                            if (lastMonthTotal > 0) {
                                val isSavings = monthComparison.isSavings
                                val pct = Math.abs(monthComparison.percentChange)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSavings) EmeraldDark.copy(alpha = 0.25f) else WarningAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSavings) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                            contentDescription = "Vs Last Month",
                                            tint = if (isSavings) EmeraldLight else WarningAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${String.format(Locale.US, "%.0f", pct)}% vs Last Mo",
                                            color = if (isSavings) EmeraldLight else WarningAmber,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Circular Budget Progress Ring & Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CircularBudgetRing(
                                spent = monthTotal,
                                budget = overallBudget,
                                currency = currency,
                                size = 145.dp,
                                strokeWidth = 12.dp
                            )

                            Column(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "Last Month Total",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$currency${String.format(Locale.US, "%.2f", lastMonthTotal)}",
                                    color = SoftBlueLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("last_month_total_text")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val remaining = overallBudget - monthTotal
                                Text(
                                    text = if (remaining >= 0) "Remaining Balance" else "Over Target By",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$currency${String.format(Locale.US, "%.2f", Math.abs(remaining))}",
                                    color = if (remaining >= 0) EmeraldLight else DangerCoral,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Budget Alert Banner
            if (alertCategory != null) {
                item {
                    val isExceeded = alertCategory.isExceeded
                    val alertColor = if (isExceeded) DangerCoral else WarningAmber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(alertColor.copy(alpha = 0.14f))
                            .border(1.dp, alertColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("budget_alert_banner"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            tint = alertColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isExceeded) {
                                "${alertCategory.category} is at ${(alertCategory.percent * 100).toInt()}% of budget!"
                            } else {
                                "${alertCategory.category} is approaching limit ($currency${String.format(Locale.US, "%.0f", alertCategory.spent)} / $currency${alertCategory.limit.toInt()})"
                            },
                            color = alertColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Natural Language & Voice Quick Input Bar
            item {
                QuickInputBar(
                    inputText = naturalLanguageInput,
                    onInputChange = { viewModel.onNaturalLanguageChange(it) },
                    parsedPreview = parsedPreview,
                    onSubmit = {
                        viewModel.submitNaturalLanguageInput(
                            onSuccess = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onOpenFullForm = onOpenAddExpense
                )
            }

            // --- 3. PERIOD SELECTOR TABS: This Month, Last Month, All Time ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (period in ExpensePeriod.entries) {
                        val isSelected = selectedPeriod == period
                        val label = when (period) {
                            ExpensePeriod.THIS_MONTH -> "This Month"
                            ExpensePeriod.LAST_MONTH -> "Last Month"
                            ExpensePeriod.ALL_TIME -> "All Time"
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SoftBlueSecondary else DarkCardSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) SoftBlueSecondary else DarkBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectedPeriod.value = period }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Active period banner if looking at Last Month
            if (selectedPeriod == ExpensePeriod.LAST_MONTH) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InfoCyan.copy(alpha = 0.15f))
                            .border(1.dp, InfoCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Last Month Records",
                                tint = InfoCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Viewing Last Month's stored data (${viewModel.lastMonthName})",
                                color = InfoCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Back to This Month",
                            color = SoftBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.selectedPeriod.value = ExpensePeriod.THIS_MONTH }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Category Filter Horizontal Row
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "All Categories",
                        isSelected = activeFilterCategory == null,
                        onClick = { viewModel.filterCategory.value = null },
                        testTag = "filter_chip_all"
                    )

                    for (cat in ExpenseCategory.entries) {
                        FilterChip(
                            label = cat.displayName,
                            isSelected = activeFilterCategory == cat.displayName,
                            onClick = {
                                viewModel.filterCategory.value =
                                    if (activeFilterCategory == cat.displayName) null else cat.displayName
                            },
                            testTag = "filter_chip_${cat.name}"
                        )
                    }
                }
            }

            // Section Header: Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedPeriod == ExpensePeriod.LAST_MONTH) "Last Month's Expenses" else "Transactions (${selectedPeriod.displayName})",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${filteredExpenses.size} items",
                        color = TextSubtle,
                        fontSize = 12.sp
                    )
                }
            }

            // Expenses Feed or Empty State
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardSurface)
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "No transactions",
                                tint = TextSubtle,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions in ${selectedPeriod.displayName}",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Speak into the mic or type 'Spent $12 on lunch' to log an expense!",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(it) }
                    )
                }
            }
        }

        // Floating Action Button (+)
        FloatingActionButton(
            onClick = onOpenAddExpense,
            containerColor = EmeraldPrimary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("fab_add_expense")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense",
                modifier = Modifier.size(26.dp)
            )
        }
    }

    // Edit Daily Budget Dialog
    if (showEditDailyBudgetDialog) {
        var dailyBudgetText by remember { mutableStateOf(dailyBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showEditDailyBudgetDialog = false },
            title = { Text("Set Daily Spending Budget", color = TextWhite) },
            text = {
                Column {
                    Text(
                        "Set your target maximum daily spend limit:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dailyBudgetText,
                        onValueChange = { dailyBudgetText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        prefix = { Text(currency, color = EmeraldLight) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_budget_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = dailyBudgetText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.updateDailyBudget(amount)
                            Toast.makeText(context, "Daily budget updated to $currency${amount.toInt()}", Toast.LENGTH_SHORT).show()
                            showEditDailyBudgetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_daily_budget_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDailyBudgetDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkElevatedSurface
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) EmeraldPrimary else DarkCardSurface)
            .border(
                1.dp,
                if (isSelected) EmeraldPrimary else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextWhite,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
