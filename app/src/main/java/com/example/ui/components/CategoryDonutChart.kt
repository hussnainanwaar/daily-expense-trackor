package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseCategory
import com.example.ui.CategoryBudgetProgress
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    categoryProgress: List<CategoryBudgetProgress>,
    currency: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 180.dp,
    strokeWidth: Dp = 22.dp
) {
    val activeItems = categoryProgress.filter { it.spent > 0 }
    val totalSpent = activeItems.sumOf { it.spent }

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "donut_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_donut_chart"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (totalSpent <= 0.0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(chartSize)
                    .clip(CircleShape)
                    .background(DarkElevatedSurface)
            ) {
                Text("No data yet", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(chartSize)
            ) {
                Canvas(modifier = Modifier.size(chartSize)) {
                    val strokePx = strokeWidth.toPx()
                    var startAngle = -90f

                    for (item in activeItems) {
                        val fraction = (item.spent / totalSpent).toFloat()
                        val sweepAngle = fraction * 360f * animatedProgress
                        val categoryObj = ExpenseCategory.fromDisplayName(item.category)
                        val isSelected = selectedCategory == null || selectedCategory == item.category
                        val drawColor = if (isSelected) categoryObj.color else categoryObj.color.copy(alpha = 0.35f)

                        // Draw arc with tiny gap
                        drawArc(
                            color = drawColor,
                            startAngle = startAngle + 1f,
                            sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f),
                            useCenter = false,
                            style = Stroke(
                                width = if (selectedCategory == item.category) strokePx + 4f else strokePx,
                                cap = StrokeCap.Butt
                            )
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val currentSelection = selectedCategory?.let { sel -> activeItems.find { it.category == sel } }
                    if (currentSelection != null) {
                        Text(
                            text = currentSelection.category,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = "$currency${String.format(Locale.US, "%.1f", currentSelection.spent)}",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val pct = ((currentSelection.spent / totalSpent) * 100).toInt()
                        Text(
                            text = "$pct%",
                            color = ExpenseCategory.fromDisplayName(currentSelection.category).color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Total Spend",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$currency${String.format(Locale.US, "%.0f", totalSpent)}",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive category tags / legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (item in activeItems) {
                    val categoryObj = ExpenseCategory.fromDisplayName(item.category)
                    val isSelected = selectedCategory == item.category
                    val pct = ((item.spent / totalSpent) * 100).toInt()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) categoryObj.color.copy(alpha = 0.2f) else DarkCardSurface)
                            .clickable {
                                selectedCategory = if (selectedCategory == item.category) null else item.category
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("legend_item_${item.category}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(categoryObj.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.category} ($pct%)",
                            color = if (isSelected) categoryObj.color else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
