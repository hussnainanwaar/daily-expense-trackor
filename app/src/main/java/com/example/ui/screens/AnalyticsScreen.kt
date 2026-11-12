package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SpendingInsight
import com.example.ui.ExpensePeriod
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.WeeklyBarChart
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
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    val lastMonthTotal by viewModel.lastMonthTotal.collectAsStateWithLifecycle()
    val monthComparison by viewModel.monthComparison.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val categoryProgress by viewModel.categoryProgressList.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklySpending.collectAsStateWithLifecycle()
    val smartInsights by viewModel.smartInsights.collectAsStateWithLifecycle()

    val currency = userSettings.defaultCurrency
    val activeCategories = categoryProgress.filter { it.spent > 0 }
    val topCategory = activeCategories.maxByOrNull { it.spent }
    val avgDailySpend = if (weeklySpending.isNotEmpty()) weeklySpending.map { it.amount }.average() else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Spending Analytics",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Historical comparisons, charts & intelligent advice",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Period Selector Tabs (This Month / Last Month / All Time)
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

        // Month-over-Month Comparison Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("month_comparison_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Comparison",
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Comparison",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val isSavings = monthComparison.isSavings
                        val pct = Math.abs(monthComparison.percentChange)
                        Text(
                            text = if (isSavings) "${String.format(Locale.US, "%.1f", pct)}% Savings" else "+${String.format(Locale.US, "%.1f", pct)}% Increase",
                            color = if (isSavings) EmeraldLight else DangerCoral,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // This Month Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "This Month (${viewModel.thisMonthName})",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$currency${String.format(Locale.US, "%.2f", monthTotal)}",
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Last Month Column
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Last Month (${viewModel.lastMonthName})",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$currency${String.format(Locale.US, "%.2f", lastMonthTotal)}",
                                color = SoftBlueLight,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val diff = monthComparison.difference
                    val statusText = if (diff <= 0) {
                        "You've spent $currency${String.format(Locale.US, "%.2f", Math.abs(diff))} less than last month so far."
                    } else {
                        "You've spent $currency${String.format(Locale.US, "%.2f", diff)} more than last month."
                    }

                    Text(
                        text = statusText,
                        color = if (diff <= 0) EmeraldLight else WarningAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val currentPeriodSpend = when (selectedPeriod) {
                    ExpensePeriod.THIS_MONTH -> monthTotal
                    ExpensePeriod.LAST_MONTH -> lastMonthTotal
                    ExpensePeriod.ALL_TIME -> monthTotal + lastMonthTotal
                }

                // Period Spend Metric Card
                MetricCard(
                    title = "${selectedPeriod.displayName} Total",
                    value = "$currency${String.format(Locale.US, "%.0f", currentPeriodSpend)}",
                    subtitle = if (selectedPeriod == ExpensePeriod.LAST_MONTH) viewModel.lastMonthName else "Billing Cycle",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = EmeraldLight,
                    modifier = Modifier.weight(1f)
                )

                // Daily Average Metric Card
                MetricCard(
                    title = "Daily Average",
                    value = "$currency${String.format(Locale.US, "%.1f", avgDailySpend)}",
                    subtitle = "Past 7 Days",
                    icon = Icons.Default.ShowChart,
                    accentColor = SoftBlueLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Breakdown Card (Donut Chart for selected period)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category Distribution (${selectedPeriod.displayName})",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        topCategory?.let {
                            Text(
                                text = "Top: ${it.category}",
                                color = SoftBlueLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryDonutChart(
                        categoryProgress = categoryProgress,
                        currency = currency
                    )
                }
            }
        }

        // Weekly Spending Bar Chart Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Trend (Last 7 Days)",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Highlights Today",
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    WeeklyBarChart(
                        days = weeklySpending,
                        currency = currency
                    )
                }
            }
        }

        // Smart Savings & Advisor Insights Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Insights",
                    tint = EmeraldLight,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Intelligent Insights & Advice",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Smart Insight Cards Feed
        items(smartInsights, key = { it.id }) { insight ->
            InsightCard(insight = insight)
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DarkCardSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun InsightCard(insight: SpendingInsight) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .border(1.dp, insight.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("insight_card_${insight.id}"),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(insight.accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = insight.icon,
                contentDescription = insight.title,
                tint = insight.accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                color = insight.accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = insight.description,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
