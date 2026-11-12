package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DaySpend
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftBlueLight
import com.example.ui.theme.SoftBlueSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun WeeklyBarChart(
    days: List<DaySpend>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val maxSpend = (days.maxOfOrNull { it.amount } ?: 10.0).coerceAtLeast(10.0)

    val animatedHeightRatio by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "weekly_bars"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            for (day in days) {
                val ratio = ((day.amount / maxSpend).toFloat() * animatedHeightRatio).coerceIn(0.05f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (day.amount > 0) {
                        Text(
                            text = String.format(Locale.US, "%.0f", day.amount),
                            color = if (day.isToday) EmeraldLight else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(86.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(DarkElevatedSurface),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val barBrush = if (day.isToday) {
                            Brush.verticalGradient(listOf(EmeraldLight, EmeraldPrimary))
                        } else if (day.amount > 0) {
                            Brush.verticalGradient(listOf(SoftBlueLight, SoftBlueSecondary))
                        } else {
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(ratio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(barBrush)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = day.dayLabel,
                        color = if (day.isToday) EmeraldPrimary else TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
