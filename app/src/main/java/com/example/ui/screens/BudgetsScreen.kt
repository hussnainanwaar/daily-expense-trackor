package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExpenseCategory
import com.example.ui.CategoryBudgetProgress
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftBlueLight
import com.example.ui.theme.SoftBlueSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun BudgetsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val dailyBudget by viewModel.dailyBudget.collectAsStateWithLifecycle()
    val todayRemaining by viewModel.todayRemaining.collectAsStateWithLifecycle()
    val todayBudgetRatio by viewModel.todayBudgetRatio.collectAsStateWithLifecycle()

    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    val categoryProgressList by viewModel.categoryProgressList.collectAsStateWithLifecycle()

    val currency = userSettings.defaultCurrency
    val overallBudget = userSettings.monthlyOverallBudget

    var editingCategory by remember { mutableStateOf<CategoryBudgetProgress?>(null) }
    var editingOverallBudget by remember { mutableStateOf(false) }
    var editingDailyBudget by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("budgets_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Budgets & Limits",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set daily & monthly ceilings to prevent overspending",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- 1. DAILY BUDGET CARD (Requested Feature) ---
        item {
            val isDailyExceeded = todayTotal > dailyBudget
            val dailyRatio = todayBudgetRatio.coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(
                        1.dp,
                        if (isDailyExceeded) DangerCoral.copy(alpha = 0.5f) else EmeraldPrimary.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
                    .testTag("budgets_daily_card")
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
                                    contentDescription = "Daily Limit",
                                    tint = if (isDailyExceeded) DangerCoral else EmeraldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Spending Budget",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Max target limit per day",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { editingDailyBudget = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("edit_daily_budget_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit daily budget",
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "$currency${String.format(Locale.US, "%.2f", todayTotal)}",
                                color = if (isDailyExceeded) DangerCoral else EmeraldLight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "spent today of $currency${String.format(Locale.US, "%.0f", dailyBudget)} daily budget",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = if (isDailyExceeded) "Over by $currency${String.format(Locale.US, "%.2f", todayTotal - dailyBudget)}" else "$currency${String.format(Locale.US, "%.2f", todayRemaining)} left today",
                            color = if (isDailyExceeded) DangerCoral else EmeraldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { dailyRatio },
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

        // --- 2. MONTHLY TOTAL BUDGET CARD ---
        item {
            val overallRatio = if (overallBudget > 0) (monthTotal / overallBudget).toFloat().coerceIn(0f, 1f) else 0f
            val remaining = overallBudget - monthTotal

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("overall_budget_card")
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
                                    .background(SoftBlueSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Monthly Target",
                                    tint = SoftBlueLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Monthly Spending Target",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Overall cap across all categories",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { editingOverallBudget = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("edit_overall_budget_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit overall budget",
                                tint = SoftBlueLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "$currency${String.format(Locale.US, "%.0f", monthTotal)}",
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "spent of $currency${String.format(Locale.US, "%.0f", overallBudget)} limit",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = if (remaining >= 0) "$currency${String.format(Locale.US, "%.0f", remaining)} left" else "Over by $currency${String.format(Locale.US, "%.0f", Math.abs(remaining))}",
                            color = if (remaining >= 0) SoftBlueLight else DangerCoral,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { overallRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (monthTotal > overallBudget) DangerCoral else SoftBlueSecondary,
                        trackColor = DarkElevatedSurface
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Category Budgets",
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Category Budgets List
        items(categoryProgressList, key = { it.category }) { item ->
            CategoryBudgetItem(
                item = item,
                currency = currency,
                onEdit = { editingCategory = item }
            )
        }
    }

    // Edit Daily Budget Dialog
    if (editingDailyBudget) {
        var newDailyText by remember { mutableStateOf(dailyBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editingDailyBudget = false },
            title = { Text("Set Daily Spending Budget", color = TextWhite) },
            text = {
                Column {
                    Text("Enter maximum target spend limit per day:", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newDailyText,
                        onValueChange = { newDailyText = it },
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
                            .testTag("edit_daily_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newDailyText.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.updateDailyBudget(amount)
                            Toast.makeText(context, "Daily budget updated to $currency${amount.toInt()}", Toast.LENGTH_SHORT).show()
                            editingDailyBudget = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_daily_budget_confirm")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDailyBudget = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkElevatedSurface
        )
    }

    // Edit Category Budget Dialog
    editingCategory?.let { catProgress ->
        var newLimitText by remember { mutableStateOf(catProgress.limit.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Set ${catProgress.category} Budget", color = TextWhite) },
            text = {
                Column {
                    Text(
                        "Enter monthly budget ceiling for ${catProgress.category}:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newLimitText,
                        onValueChange = { newLimitText = it },
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
                            .testTag("budget_limit_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = newLimitText.toDoubleOrNull()
                        if (limit != null && limit >= 0) {
                            viewModel.updateCategoryBudget(catProgress.category, limit)
                            Toast.makeText(context, "Updated ${catProgress.category} budget", Toast.LENGTH_SHORT).show()
                            editingCategory = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_budget_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkElevatedSurface
        )
    }

    // Edit Overall Budget Dialog
    if (editingOverallBudget) {
        var newLimitText by remember { mutableStateOf(overallBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editingOverallBudget = false },
            title = { Text("Set Monthly Overall Budget", color = TextWhite) },
            text = {
                Column {
                    Text("Enter total monthly spending target:", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newLimitText,
                        onValueChange = { newLimitText = it },
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
                            .testTag("overall_budget_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = newLimitText.toDoubleOrNull()
                        if (limit != null && limit > 0) {
                            viewModel.updateOverallBudget(limit)
                            Toast.makeText(context, "Monthly target updated", Toast.LENGTH_SHORT).show()
                            editingOverallBudget = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_overall_budget_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingOverallBudget = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkElevatedSurface
        )
    }
}

@Composable
private fun CategoryBudgetItem(
    item: CategoryBudgetProgress,
    currency: String,
    onEdit: () -> Unit
) {
    val category = ExpenseCategory.fromDisplayName(item.category)
    val displayPercent = (item.percent * 100).toInt()
    val progressColor = when {
        item.isExceeded -> DangerCoral
        item.isWarning -> WarningAmber
        else -> category.color
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .padding(14.dp)
            .testTag("budget_item_${item.category}")
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = item.category,
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.category,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$currency${String.format(Locale.US, "%.0f", item.spent)} of $currency${String.format(Locale.US, "%.0f", item.limit)}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isExceeded) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DangerCoral.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Exceeded ($displayPercent%)",
                                color = DangerCoral,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (item.isWarning) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarningAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$displayPercent%",
                                color = WarningAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "$displayPercent%",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("edit_budget_${item.category}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit limit",
                            tint = TextSubtle,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { item.percent.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = DarkElevatedSurface
            )
        }
    }
}
