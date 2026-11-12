package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun CircularBudgetRing(
    spent: Double,
    budget: Double,
    currency: String,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 14.dp
) {
    val rawRatio = if (budget > 0) (spent / budget).toFloat() else 0f
    val displayPercent = (rawRatio * 100).toInt()
    val sweepRatio = rawRatio.coerceIn(0f, 1f)

    val animatedSweep by animateFloatAsState(
        targetValue = sweepRatio * 360f,
        animationSpec = tween(durationMillis = 900),
        label = "budget_sweep"
    )

    val (startColor, endColor) = when {
        rawRatio >= 1.0f -> DangerCoral to Color(0xFFF87171)
        rawRatio >= 0.8f -> WarningAmber to Color(0xFFFBBF24)
        else -> EmeraldPrimary to EmeraldLight
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("circular_budget_ring")
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()

            // Background track
            drawArc(
                color = DarkElevatedSurface,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedSweep > 0) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to startColor,
                        0.5f to endColor,
                        1.0f to startColor
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currency${String.format(Locale.US, "%.0f", spent)}",
                color = TextWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("budget_spent_amount")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "of $currency${String.format(Locale.US, "%.0f", budget)} limit",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$displayPercent% used",
                color = startColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("budget_percent_label")
            )
        }
    }
}
