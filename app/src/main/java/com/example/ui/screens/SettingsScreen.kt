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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val recurringExpenses = allExpenses.filter { it.isRecurring }
    val recurringSum = recurringExpenses.sumOf { it.amount }

    var showClearConfirm by remember { mutableStateOf(false) }

    val currencies = listOf("$", "€", "£", "CA$", "A$", "¥", "₹")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings & Subscriptions",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Preferences, recurring audit & data controls",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Active Recurring Subscriptions Audit Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, InfoCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("subscriptions_audit_card")
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
                                    .background(InfoCyan.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Recurring",
                                    tint = InfoCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Subscription Auditor",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${recurringExpenses.size} recurring services detected",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "${userSettings.defaultCurrency}${String.format(Locale.US, "%.2f", recurringSum)}/mo",
                            color = InfoCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (recurringExpenses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (sub in recurringExpenses.take(5)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sub.notes.ifBlank { sub.category },
                                        color = TextWhite,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${sub.currency}${String.format(Locale.US, "%.2f", sub.amount)}",
                                        color = TextMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Spending Budget Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("settings_daily_budget_card")
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Daily Budget",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Spending Budget",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Current daily target: ${userSettings.defaultCurrency}${userSettings.dailyBudget.toInt()}/day",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(40.0, 60.0, 80.0, 100.0, 150.0).forEach { limit ->
                            val isSelected = userSettings.dailyBudget == limit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) EmeraldPrimary else DarkElevatedSurface)
                                    .clickable {
                                        viewModel.updateDailyBudget(limit)
                                        Toast.makeText(context, "Daily budget set to ${userSettings.defaultCurrency}${limit.toInt()}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("daily_budget_option_${limit.toInt()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${userSettings.defaultCurrency}${limit.toInt()}",
                                    color = if (isSelected) Color.Black else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Currency Selector Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Currency",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Display Currency",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in currencies) {
                            val isSelected = userSettings.defaultCurrency == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) EmeraldPrimary else DarkElevatedSurface)
                                    .clickable {
                                        viewModel.updateCurrency(c)
                                        Toast.makeText(context, "Currency set to $c", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("currency_option_$c"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = c,
                                    color = if (isSelected) Color.Black else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Alert Threshold Selector Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert Threshold",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Budget Alert Warning Point",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Show warning banner when category spending hits this %",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(70, 80, 90).forEach { threshold ->
                            val isSelected = userSettings.alertThresholdPercent == threshold
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) EmeraldPrimary else DarkElevatedSurface)
                                    .clickable {
                                        viewModel.updateAlertThreshold(threshold)
                                        Toast.makeText(context, "Alert set to $threshold%", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("threshold_option_$threshold"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$threshold%",
                                    color = if (isSelected) Color.Black else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Data Management Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Data Management",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Seed Sample Data Button
                    Button(
                        onClick = {
                            viewModel.seedSampleData()
                            Toast.makeText(context, "Sample transactions added", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkElevatedSurface,
                            contentColor = EmeraldLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seed_sample_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Seed")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Sample Transactions & Budgets")
                    }

                    // Clear Database Button
                    Button(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerCoral.copy(alpha = 0.16f),
                            contentColor = DangerCoral
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset & Clear All Transactions")
                    }
                }
            }
        }

        // App Info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SpendWise v1.0",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Private, on-device SQLite storage • Zero friction",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Transactions?", color = TextWhite) },
            text = {
                Text(
                    "This will delete all logged expenses from your device. Budgets will remain.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearAllData()
                        Toast.makeText(context, "All expenses cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text("Clear All", color = DangerCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkElevatedSurface
        )
    }
}
